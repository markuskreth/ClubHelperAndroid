package de.kreth.mtvandroidhelper2.ui.fragments;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import de.kreth.mtvandroidhelper2.Factory;
import de.kreth.mtvandroidhelper2.R;
import de.kreth.mtvandroidhelper2.data.ContactType;
import de.kreth.mtvandroidhelper2.data.Person;
import de.kreth.mtvandroidhelper2.data.PersonContact;
import de.kreth.mtvandroidhelper2.data.PersonPersisterImpl;
import de.kreth.mtvandroidhelper2.ui.PersonDetailActivity;
import de.kreth.mtvandroidhelper2.ui.PersonListActivity;

/**
 * A fragment representing a single Person detail screen. This fragment is
 * either contained in a {@link PersonListActivity} in two-pane mode (on
 * tablets) or a {@link PersonDetailActivity} on handsets.
 */
public class PersonDetailFragment extends Fragment implements OnClickListener {
	
	public static final String ARG_PERSON_ID = "item_id";
	public static final int BTN_DEL_CONTACT_ID = 456123;
	
	private Person person = null;

	private TextView txtFullNameView;

	private String mTag;

	private TextView txtbirthday;

	private View rootView;

//	private DataChangeHandler<Person> personHandler;
//
//	private ContactListHolder contactHandler;

	private TableLayout table;

	private List<PersonContact> contacts;

	private int contactCount;
	private ViewGroup buttonBarMain;
	private ViewGroup buttonBarDropContact;
	private PhoneNumberUtil phoneNumberUtil;
	private PersonPersisterImpl persister;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTag = Factory.getInstance().TAG() + getClass().getSimpleName();
		phoneNumberUtil = PhoneNumberUtil.getInstance();
		persister = new PersonPersisterImpl(Factory.getInstance().getDatabase());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		int personId = getArguments().getInt(PersonDetailFragment.ARG_PERSON_ID, -1);
//		personHandler = activity.getPersonHandler();
//		contactHandler = (ContactListHolder)activity.getContactHandler();

		rootView = inflater.inflate(R.layout.fragment_person_detail,
				container, false);
		initViewComponents();

//		activity.addPropertyChangeListener(this);
		setupPerson(personId);
		setupContacts();
		setupDragAndDrop();
		
		return rootView;
	}
	
	private void setupDragAndDrop() {
		buttonBarMain = (ViewGroup) rootView.findViewById(R.id.buttonBarMain);
		buttonBarDropContact = (ViewGroup) rootView.findViewById(R.id.buttonBarDropArea);
		buttonBarDropContact.setVisibility(View.GONE);
		ObjectDragListener listener = new ObjectDragListener();
		buttonBarDropContact.findViewById(R.id.btnItemDelete).setOnDragListener(listener);
		buttonBarDropContact.findViewById(R.id.btnItemEdit).setOnDragListener(listener);

	}

	private void setupContacts() {
		int index = findIndexOfRowWithId(R.id.tableRowContacts);
		
		if(contacts != null){
			table.removeViews(index, contactCount);
		}
		
		contacts = persister.getContactsFor(person);
		contactCount = contacts.size();
		
		for(PersonContact con: contacts){
			TableRow row = createNewTableRow();
			View view1 = createNewContactTypeViewForTableRow(con.getType());
			view1.setFocusable(false);
			
			row.addView(view1);
			String text;
			if(con.getType()==ContactType.MOBILE || con.getType()==ContactType.TELEPHONE){

				try {
					PhoneNumber phone = phoneNumberUtil.parse(con.getValue(), Locale.getDefault().getCountry());
					text = phoneNumberUtil.format(phone, PhoneNumberFormat.NATIONAL);
				} catch (NumberParseException e) {
					text = con.getValue();
				}
			}
			else 
				text = con.getValue();
			TextView txtValue = createNewContactValueTextViewForTableRow(text);
			row.addView(txtValue);
			
			row.setTag(con.getId());
			
			TableRowDragListener trdl = new TableRowDragListener();
			row.setOnLongClickListener(trdl);
			table.addView(row, index);
			index++;
		}

	    View redLine = new View(getActivity());
	    ColorDrawable red = new ColorDrawable();
	    red.setColor(Color.RED);
	    redLine.setBackground(red);
	    redLine.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 3));
	    table.addView(redLine);
	}

	public void deleteTableRowContact(TableRow contactRow) {
		Object tag = contactRow.getTag();
//		int contactIdToDelete = (Integer) tag;
		throw new UnsupportedOperationException("Delete noch nicht implementiert! contactIdToDelete=" + tag);
//		contactHandler.deleteData(contactIdToDelete);
	}
	
	private int findIndexOfRowWithId(int idOfRow) {
		
		for(int i=0; i<table.getChildCount(); i++){
			if(table.getChildAt(i).getId() == idOfRow){
				return i+1;
			}				
		}

		return table.getChildCount();
	}

	private PersonContact findContactWithId(int contactId) {
		for(PersonContact cont : contacts){
			if(cont.getId() == contactId)
				return cont;
		}
		throw new IllegalArgumentException("Kein " + PersonContact.class.getSimpleName() + " gefunden in Liste " + contacts);
	}

	private TextView createNewContactValueTextViewForTableRow(String value) {
		TextView t = new TextView(getActivity());
		t.setText(value);
		TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
		t.setLayoutParams(layoutParams);
		return t;
	}

	private View createNewContactTypeViewForTableRow(ContactType selected) {
		TextView view = new TextView(getActivity());
		CharSequence text;
		switch (selected) {
		case EMAIL:
			text = getActivity().getString(R.string.label_email);
			break;
		case MOBILE:
			text = getActivity().getString(R.string.label_mobil);
			break;
		case TELEPHONE:
			text = getActivity().getString(R.string.label_telephone);
			break;
		default:
			text = getActivity().getString(R.string.label_value);
			break;
		}

		view.setText(text + ": ");
		TableRow.LayoutParams para = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
		view.setLayoutParams(para);
		return view;
	}

	private TableRow createNewTableRow() {
		TableRow row = new TableRow(getActivity()){
			@Override
		    public boolean dispatchDragEvent(DragEvent ev){
		        boolean r = super.dispatchDragEvent(ev);
		        if (r && (ev.getAction() == DragEvent.ACTION_DRAG_STARTED 
		                    || ev.getAction() == DragEvent.ACTION_DRAG_ENDED)){
		            // If we got a start or end and the return value is true, our
		            // onDragEvent wasn't called by ViewGroup.dispatchDragEvent
		            // So we do it here.
		            onDragEvent(ev);
		        }
		        return r;
		    }
		};
		row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
		return row;
	}

	private void setupPerson(int personId) {

		person = persister.getPersonById(personId);		
		Log.i(mTag, "Details von " + person);
		txtFullNameView.setText(getNameOfPerson(person));
		updateBirthdayValues();
		
	}

	public void updateBirthdayValues() {
        txtbirthday.setText(DateFormat.getDateInstance().format(person.getBirthdate().getTime()));		
	}

	private void initViewComponents() {


		rootView.findViewById(R.id.buttonOk).setOnClickListener(this);
		rootView.findViewById(R.id.buttonCancel).setOnClickListener(this);
		rootView.findViewById(R.id.btnEditBirthday).setOnClickListener(this);
		
		txtFullNameView = (TextView) rootView.findViewById(R.id.textFullName);
		txtbirthday = (TextView) rootView.findViewById(R.id.txtBirthday);
		
		txtFullNameView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String fullName = txtFullNameView.getText().toString();
				setNameOfPerson(fullName , person);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {				
			}
		});
		
		table = (TableLayout) rootView.findViewById(R.id.table1);
	}

	private String getNameOfPerson(Person p){
		StringBuilder builder = new StringBuilder();
		if(p.getPreName() != null){
			builder.append(p.getPreName().trim()).append(" ");
		}
		if(p.getSurName() != null) 
			builder.append(p.getSurName());
		
		if(builder.toString().trim().isEmpty())
			return null;
		return builder.toString();
	}
	
	private void setNameOfPerson(String fullName, Person p){
		String[] split = fullName.split(" ");
		p.setPreName(split[0]);
		
		if(split.length==1)
			p.setSurName("");
		if(split.length==2)
			p.setSurName(split[1]);
		else if(split.length>2){
			String surName = split[split.length-1];
			p.setSurName(surName);
			StringBuilder preNames = new StringBuilder(fullName.length()-surName.length()+split.length+1);
			for(int i=0; i<split.length-1;i++){
				preNames.append(split[i]).append(" ");
			}
			p.setPreName(preNames.toString().trim());
		}
	}

	private class ObjectDragListener implements OnDragListener {

		Drawable normalShape = getResources().getDrawable(R.drawable.normal_shape);
		Drawable targetShape = getResources().getDrawable(R.drawable.target_shape);
		long lastDrop = -1;
		
		@Override
		public boolean onDrag(final View v, final DragEvent event) {
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_ENTERED:
				v.setBackground(targetShape);
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				v.setBackground(normalShape);
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				if(isLastDropIsLongAgo()){
					getActivity().runOnUiThread(new Runnable() {
						
						@Override
						public void run() {

							txtFullNameView.setEnabled(true);
							v.setBackground(normalShape);
							View view = (View)event.getLocalState();
							view.setVisibility(View.VISIBLE);
						}
					});
					getActivity().runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Log.i(mTag, "Setting Visitility on buttonBarMain on VISIBLE: " + System.currentTimeMillis());
							buttonBarMain.setVisibility(View.VISIBLE);
						}
					});
					getActivity().runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Log.i(mTag, "Setting Visitility on buttonBarDropContact on GONE: " + System.currentTimeMillis());
							buttonBarDropContact.setVisibility(View.GONE);
						}
					});
				} else {
					System.out.println("=========Drop Skipped!!!!!");
				}
				break;
			case DragEvent.ACTION_DROP:

				TableRow row = (TableRow)event.getLocalState();
				
				if(v.getId()==R.id.btnItemDelete){
					row.setVisibility(View.GONE);
					deleteTableRowContact(row);
				} else if (v.getId()==R.id.btnItemEdit){

					int contactId = (Integer) row.getTag();
					
					PersonContact contact = findContactWithId(contactId);
					
					String txt = contact.getId() + ": " + contact.getType() + " - " + contact.getValue();
					AlertDialog.Builder bld= new AlertDialog.Builder(getActivity());
					bld.setTitle("Edit " + txt);
					bld.setMessage(txt);
					bld.show();
				}
				break;
			}
			return true;
		}

		private boolean isLastDropIsLongAgo() {
			long now = System.currentTimeMillis();
			long waitTime = now - lastDrop;
			boolean retVal = false;
			Log.v(mTag, "");
			if(lastDrop<0 || waitTime>500){
				retVal = true;
				lastDrop = now;
				Log.v(mTag, "Last Drop ist ok, dauer: " + waitTime + " ms");
			} else {
				Log.v(mTag, "Last Drop zu lange, dauer: " + waitTime + " ms");
			}
			
			return retVal;
		}
		
	}
	
	private class TableRowDragListener implements OnLongClickListener {
		
		@Override
		public boolean onLongClick(View v) {
			int contactId = (Integer) v.getTag();
			PersonContact contact = findContactWithId(contactId);
			String text = contact.getType() + " - " + contact.getValue();
			ClipData.Item item = new ClipData.Item(text);
			String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
			ClipData data = new ClipData(text, mimeTypes, item);
			DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
			
			v.startDrag(data, shadowBuilder, v, 0);
			v.setVisibility(View.INVISIBLE);
			
			buttonBarMain.setVisibility(View.GONE);
			buttonBarDropContact.setVisibility(View.VISIBLE);
			txtFullNameView.setEnabled(false);
			return true;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonOk:
			persister.storePerson(person);
			getActivity().onBackPressed();
			break;

		case R.id.buttonCancel:
			getActivity().onBackPressed();			
			break;

		case R.id.btnEditBirthday:
			showBirthdayDialog();
			break;
		case R.id.btnAddContact:
			showCreateContactDialog(ContactType.TELEPHONE, "");
			break;
		case PersonDetailFragment.BTN_DEL_CONTACT_ID:
			int contactId = ((Integer)v.getTag()).intValue();
			Log.w(mTag, "nicht implementiert, lösche Contakt id=" + contactId);
//			personContactHolder.deleteData(contactId);
			break;
		default:
			break;
		}
		
	}

	private void showBirthdayDialog(){
		Calendar birthdate = person.getBirthdate();
		int year = birthdate.get(Calendar.YEAR);
		int monthOfYear = birthdate.get(Calendar.MONTH);
		int dayOfMonth = birthdate.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), createDateSetListener(), year, monthOfYear, dayOfMonth);
		datePickerDialog.show();
	}

	private OnDateSetListener createDateSetListener(){
		OnDateSetListener l = new OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				
				person.getBirthdate().set(Calendar.YEAR, year);
				person.getBirthdate().set(Calendar.MONTH, monthOfYear);
				person.getBirthdate().set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateBirthdayValues();
			}
		};
		return l;
	}
	private void showCreateContactDialog(ContactType presetType, String presetValue) {
		final Dialog dlg = new Dialog(getActivity());
		dlg.setContentView(R.layout.add_contact_dialog);
		dlg.setTitle(R.string.title_add_contact);
		
		final EditText txt = (EditText) dlg.findViewById(R.id.editText1);
		txt.setText(presetValue);
		final PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
		final ArrayAdapter<ContactType> typeAdapter = new ArrayAdapter<ContactType>(getActivity(), android.R.layout.simple_spinner_item, ContactType.values()){
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
				PersonContact personContact = new PersonContact(person.getId(), selectedType, text);
				contacts.add(personContact);
				persister.storePersonContacts(contacts);
//				personContactHolder.add(personContact);
//				pcs.firePropertyChange(PersonContact.class.getSimpleName(), 0, -1);
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
}
