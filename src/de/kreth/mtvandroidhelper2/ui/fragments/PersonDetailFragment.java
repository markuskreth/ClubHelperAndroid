package de.kreth.mtvandroidhelper2.ui.fragments;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import de.kreth.mtvandroidhelper2.Factory;
import de.kreth.mtvandroidhelper2.R;
import de.kreth.mtvandroidhelper2.data.ContactType;
import de.kreth.mtvandroidhelper2.data.DataChangeHandler;
import de.kreth.mtvandroidhelper2.data.Person;
import de.kreth.mtvandroidhelper2.data.PersonContact;
import de.kreth.mtvandroidhelper2.ui.PersonDetailActivity;
import de.kreth.mtvandroidhelper2.ui.PersonListActivity;
import de.kreth.mtvandroidhelper2.ui.PersonDetailActivity.ContactListHolder;

/**
 * A fragment representing a single Person detail screen. This fragment is
 * either contained in a {@link PersonListActivity} in two-pane mode (on
 * tablets) or a {@link PersonDetailActivity} on handsets.
 */
public class PersonDetailFragment extends Fragment implements PropertyChangeListener {
	
	public static final String ARG_PERSON_ID = "item_id";
	public static final int BTN_DEL_CONTACT_ID = 456123;
	
	private Person person = null;

	private TextView txtFullNameView;

	private String mTag;

	private TextView txtbirthday;

	private View rootView;

	private DataChangeHandler<Person> personHandler;

	private ContactListHolder contactHandler;

	private TableLayout table;

	private List<PersonContact> contacts;

	private int contactCount;
	private ViewGroup buttonBarMain;
	private ViewGroup buttonBarDropContact;
	private PersonDetailActivity activity;
	private PhoneNumberUtil phoneNumberUtil;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTag = Factory.getInstance().TAG() + getClass().getSimpleName();
		phoneNumberUtil = PhoneNumberUtil.getInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = (PersonDetailActivity) getActivity();
		
		personHandler = activity.getPersonHandler();
		contactHandler = (ContactListHolder)activity.getContactHandler();

		initViewComponents(inflater, container);

		activity.addPropertyChangeListener(this);
		setupPerson();
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
		
		contacts = contactHandler.getData();
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
		int contactIdToDelete = (Integer) contactRow.getTag();
		contactHandler.deleteData(contactIdToDelete);
	}
	
	private int findIndexOfRowWithId(int idOfRow) {
		
		for(int i=0; i<table.getChildCount(); i++){
			if(table.getChildAt(i).getId() == idOfRow){
				return i+1;
			}				
		}

		return table.getChildCount();
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

	private void setupPerson() {

		person = personHandler.getData();		
		Log.i(mTag, "Details von " + person);
		txtFullNameView.setText(getNameOfPerson(person));
		updateBirthdayValues();

		
	}

	public void updateBirthdayValues() {
        txtbirthday.setText(DateFormat.getDateInstance().format(person.getBirthdate().getTime()));		
	}

	private void initViewComponents(LayoutInflater inflater, ViewGroup container) {

		rootView = inflater.inflate(R.layout.fragment_person_detail,
				container, false);
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

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getPropertyName().matches(PersonContact.class.getSimpleName())){
			setupContacts();
		} else if(event.getPropertyName().matches(Person.class.getSimpleName())){
			setupPerson();
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
					PersonContact contact = contactHandler.findContactWithId(contactId);
					
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
			PersonContact contact = contactHandler.findContactWithId(contactId);
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

}
