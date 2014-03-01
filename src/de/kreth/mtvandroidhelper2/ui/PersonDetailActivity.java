package de.kreth.mtvandroidhelper2.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import de.kreth.mtvandroidhelper2.Factory;
import de.kreth.mtvandroidhelper2.R;
import de.kreth.mtvandroidhelper2.data.ContactType;
import de.kreth.mtvandroidhelper2.data.DataChangeHandler;
import de.kreth.mtvandroidhelper2.data.Person;
import de.kreth.mtvandroidhelper2.data.PersonContact;
import de.kreth.mtvandroidhelper2.data.PersonPersisterImpl;
import de.kreth.mtvandroidhelper2.ui.fragments.PersonDetailFragment;

/**
 * An activity representing a single Person detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link PersonListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PersonDetailFragment}.
 */
public class PersonDetailActivity extends FragmentActivity implements View.OnClickListener {

	private String mTag;
	private PersonPersisterImpl persister;
	private boolean personMustBeStored;

	private Person personOriginal;
	private PersonDetailFragment fragment;
	private PersonHolder personHolder;
	private List<PersonContact> personContactsOriginal;
	private ContactListHolder personContactHolder;

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private PhoneNumberUtil phoneNumberUtil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person_detail);

		mTag = Factory.getInstance().TAG() + "_" + getClass().getSimpleName();
		
		Intent intent = getIntent();
		int personId = intent.getIntExtra(PersonDetailFragment.ARG_PERSON_ID, -1);
		
		setupActionBar();
		
		persister = new PersonPersisterImpl(Factory.getInstance().getDatabase());
		personOriginal = persister.getPersonById(personId);
		personContactsOriginal = persister.getContactsFor(personOriginal);
		initEditableContact();
		initEditablePerson();
		Locale locale = Locale.getDefault();
		phoneNumberUtil = PhoneNumberUtil.getInstance();
		PhoneNumber exampleNumber = phoneNumberUtil.getExampleNumberForType(locale.getCountry(), PhoneNumberType.FIXED_LINE);
		
		System.out.println(phoneNumberUtil.format(exampleNumber, PhoneNumberFormat.NATIONAL));
		System.out.println(exampleNumber.toString());
		exampleNumber = phoneNumberUtil.getExampleNumberForType(locale.getCountry(), PhoneNumberType.MOBILE);
		System.out.println(phoneNumberUtil.format(exampleNumber, PhoneNumberFormat.NATIONAL));
		System.out.println(exampleNumber.toString());
		
		if (savedInstanceState == null) {

			fragment = new PersonDetailFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.person_detail_container, fragment).commit();
		} else {
			fragment = (PersonDetailFragment) getSupportFragmentManager().findFragmentById(R.id.person_detail_container);
		}

	}

	private void setupActionBar() {

		// Show the Up button in the action bar.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		Tab storeTab = actionBar.newTab();
		storeTab.setText(R.string.label_ok);
		storeTab.setTabListener(new TabListener() {
			
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				personHolder.storeData(personHolder.getData());		// TODO Was macht das denn???		
			}
			
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				personHolder.storeData(personHolder.getData());
			}
		});
		actionBar.addTab(storeTab);
		
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}

	private void initEditableContact() {
		personContactHolder = new ContactListHolder(personContactsOriginal);
	}

	private void initEditablePerson() {
		
		Person p;
		if(personOriginal.getId()>=0)
			p = new Person(personOriginal.getId());
		else
			p = new Person();
		p.setBirthdate((Calendar) personOriginal.getBirthdate().clone());
		p.setFather(personOriginal.getFather());
		p.setMother(personOriginal.getMother());
		p.setPreName(personOriginal.getPreName());
		p.setSurName(personOriginal.getSurName());

		personHolder = new PersonHolder(p, personContactHolder);
	}

	public DataChangeHandler<List<PersonContact>> getContactHandler(){
		return personContactHolder;
	}
	
	public DataChangeHandler<Person> getPersonHandler(){
		return personHolder; 
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.buttonOk:
			personHolder.storeData();
			break;
		case R.id.buttonCancel:
			onBackPressed();
			break;
		case R.id.btnEditBirthday:
			showBirthdayDialog();
			break;
		case R.id.btnAddContact:
			showCreateContactDialog(ContactType.TELEPHONE, "");
			break;
		case PersonDetailFragment.BTN_DEL_CONTACT_ID:
			int contactId = ((Integer)v.getTag()).intValue();
			personContactHolder.deleteData(contactId);
			break;
		default:
			break;
		}
	}
	
	private void showCreateContactDialog(ContactType presetType, String presetValue) {
		final Dialog dlg = new Dialog(this);
		dlg.setContentView(R.layout.add_contact_dialog);
		dlg.setTitle(R.string.title_add_contact);
		
		final EditText txt = (EditText) dlg.findViewById(R.id.editText1);
		txt.setText(presetValue);
		final PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
		final ArrayAdapter<ContactType> typeAdapter = new ArrayAdapter<ContactType>(this, android.R.layout.simple_spinner_item, ContactType.values()){
//			@Override
//			public long getItemId(int position) {
//				return position;
//			}
		};
		final Spinner typeSpinner = (Spinner) dlg.findViewById(R.id.spinner1);
		typeSpinner.setAdapter(typeAdapter);
		typeSpinner.setSelection(presetType.ordinal());
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long itemId) {
				ContactType selected = typeAdapter.getItem(position);
				switch (selected) {
				case EMAIL:
					txt.removeTextChangedListener(phoneNumberFormattingTextWatcher);
					txt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
					txt.setHint(R.string.dummy_email);
					break;
				case MOBILE:
				case TELEPHONE:
					txt.setInputType(InputType.TYPE_CLASS_PHONE);
					PhoneNumberType phoneNumberType;
					
					if(selected == ContactType.MOBILE)
						phoneNumberType = PhoneNumberType.MOBILE;
					else
						phoneNumberType = PhoneNumberType.FIXED_LINE;
					
					PhoneNumber exampleNumber = phoneNumberUtil.getExampleNumberForType(Locale.getDefault().getCountry(), phoneNumberType);
					txt.setHint(phoneNumberUtil.format(exampleNumber, PhoneNumberFormat.NATIONAL));
					txt.addTextChangedListener(phoneNumberFormattingTextWatcher);
					
					try{
						PhoneNumber phone = phoneNumberUtil.parse(txt.getText().toString(), Locale.getDefault().getCountry());
						txt.setText(phoneNumberUtil.format(phone, PhoneNumberFormat.NATIONAL));
						
					} catch (Exception e){
						Log.w(mTag, "Konnte text nicht als Telefonnummer formatieren: " + txt.getText(), e);
					}
					break;
				default:
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		Button btnOk = (Button) dlg.findViewById(R.id.buttonOk);
		Button btnCancel = (Button) dlg.findViewById(R.id.buttonCancel);
		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ContactType selectedType = typeAdapter.getItem(typeSpinner.getSelectedItemPosition());
				
				if(selectedType == ContactType.MOBILE || selectedType == ContactType.TELEPHONE){ 

					PhoneNumber phone;
					try {
						phone = phoneNumberUtil.parse(txt.getText().toString(), Locale.getDefault().getCountry());
						txt.setText(phoneNumberUtil.format(phone, PhoneNumberFormat.NATIONAL));
					} catch (NumberParseException e) {
						Factory.getInstance().handleException(Log.ERROR, mTag, e);
					}
				}
				String text = txt.getText().toString();
				
				personContactHolder.add(new PersonContact(personHolder.person.getId(), selectedType, text));
				pcs.firePropertyChange(PersonContact.class.getSimpleName(), 0, -1);
				dlg.dismiss();
			}
			
		});
		
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dlg.dismiss();
			}
		});
		dlg.show();
		
	}

	@Override
	public void onBackPressed() {
		checkChanges();
		goBack();
	}
	
	public boolean checkChanges() {
		personMustBeStored = personOriginal.getBirthdate().compareTo(personHolder.getData().getBirthdate()) != 0;
		personMustBeStored |= nameHasChanged();
		personMustBeStored |= contactsHaveChanged();
		return personMustBeStored;
	}
	
	private boolean contactsHaveChanged() {
		boolean retval = false;
		List<PersonContact> editedData = personContactHolder.getData();
		if(personContactsOriginal.size() != editedData.size()){
			retval = true;
		} else {
			for(int i=0; i<personContactsOriginal.size(); i++){
				if( ! personContactsOriginal.get(i).equals(editedData.get(i))){
					retval = true;
					break;
				}
			}
		}
		return retval;
	}

	private boolean nameHasChanged() {
		boolean namesAreSame = personHolder.getData().getPreName().contentEquals(personOriginal.getPreName()) 
				&& personHolder.getData().getSurName().contentEquals(personOriginal.getSurName());
		return ! namesAreSame;
	}
	
	private void showBirthdayDialog(){
		Calendar birthdate = personHolder.getData().getBirthdate();
		int year = birthdate.get(Calendar.YEAR);
		int monthOfYear = birthdate.get(Calendar.MONTH);
		int dayOfMonth = birthdate.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog datePickerDialog = new DatePickerDialog(this, createDateSetListener(), year, monthOfYear, dayOfMonth);
		datePickerDialog.show();
	}
	
	private OnDateSetListener createDateSetListener(){
		OnDateSetListener l = new OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				
				personHolder.getData().getBirthdate().set(Calendar.YEAR, year);
				personHolder.getData().getBirthdate().set(Calendar.MONTH, monthOfYear);
				personHolder.getData().getBirthdate().set(Calendar.DAY_OF_MONTH, dayOfMonth);
				fragment.updateBirthdayValues();
			}
		};
		return l;
	}
	
	private void goBack(){
		if(! personMustBeStored)
			NavUtils.navigateUpTo(this, new Intent(this,
					PersonListActivity.class));
		else{
			askUserForDiscard();
		}
	}
	
	private void askUserForDiscard() {
		
		AlertDialog.OnClickListener listener = new AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == AlertDialog.BUTTON_POSITIVE){
					// Änderungen verwerfen
					personMustBeStored = false;
					goBack();
				}
			}
		};
		
		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
		dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		dlgBuilder.setMessage(R.string.confirm_discard);
		dlgBuilder.setPositiveButton(R.string.label_yes, listener);
		dlgBuilder.setNegativeButton(R.string.label_no, listener);
		dlgBuilder.show();
	}

	public class ContactListHolder implements DataChangeHandler<List<PersonContact>> {
		private List<PersonContact> values;
		
		public ContactListHolder(List<PersonContact> values) {
			super();
			deepCopyValues(values);
		}

		public void add(PersonContact personContact) {
			values.add(personContact);
		}

		public PersonContact findContactWithId(int contactId){

			PersonContact toRemove = null;
			for(PersonContact item: values){
				if(item.getId()== contactId){
					toRemove = item;
					break;
				}					
			}
			return toRemove;
		}
		
		public void deleteData(int contactId) {
			PersonContact toRemove = findContactWithId(contactId);
			
			if(toRemove != null){
				values.remove(toRemove);
				pcs.firePropertyChange(PersonContact.class.getSimpleName(), contactId, -1);
			}
		}

		private void deepCopyValues(List<PersonContact> v) {
			this.values = new ArrayList<PersonContact>();
			for (PersonContact contact : v ) {
				PersonContact deepCopy = new PersonContact(contact.getId(), contact.getPersonId(), contact.getType(), contact.getValue());
				this.values.add(deepCopy);
			}
		}

		@Override
		public List<PersonContact> getData() {
			return new ArrayList<PersonContact>(values);
		}

		public void storeData() {
			storeData(values);
		}
		
		@Override
		public void storeData(List<PersonContact> data) {
			persister.storePersonContacts(data);
		}

		@Override
		public void deleteData(List<PersonContact> data) {}

		@Override
		public void dataHasChanged() {
			personMustBeStored = true;			
		}

		@Override
		public void cancelEdit() {
			goBack();
		}

	}
	
	private class PersonHolder implements DataChangeHandler<Person>{

		private Person person;
		private ContactListHolder contactHandler;

		public PersonHolder(Person person, ContactListHolder contactHandler) {
			this.person = person;
			this.contactHandler= contactHandler;
		}
		
		public void storeData() {
			storeData(this.person);
		}
		
		@Override
		public void storeData(Person p) {
			persister.storePerson(p);
			contactHandler.storeData();
			personMustBeStored = false;
			goBack();
		}

		@Override
		public void cancelEdit() {
			goBack();
		}

		@Override
		public void deleteData(Person data) {
			throw new UnsupportedOperationException("Delete Person nicht mehr in " + PersonDetailActivity.class.getSimpleName());
		}

		@Override
		public void dataHasChanged() {
			personMustBeStored = true;
		}

		@Override
		public Person getData() {
			return person;
		}
	}
}
