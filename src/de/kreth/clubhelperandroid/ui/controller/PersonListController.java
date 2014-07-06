package de.kreth.clubhelperandroid.ui.controller;

import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_PERSON_PRENAME;
import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_PERSON_SURNAME;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.support.v4.app.FragmentActivity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import de.kreth.clubhelperandroid.Factory;
import de.kreth.clubhelperandroid.adapter.PersonAttendenceListAdapter;
import de.kreth.clubhelperandroid.adapter.PersonListAdapter;
import de.kreth.clubhelperandroid.data.PersonPersisterImpl;
import de.kreth.clubhelperandroid.ui.fragments.PersonListFragment.ListItemDragOnLongClickListener;
import de.kreth.clubhelperandroid.ui.fragments.PersonListFragment.PERSON_LIST_MODE;
import de.kreth.clubhelperbusiness.attendence.AttendanceManager;
import de.kreth.clubhelperbusiness.data.Attendance;
import de.kreth.clubhelperbusiness.data.Person;

public class PersonListController implements OnCheckedChangeListener {


	private PERSON_LIST_MODE mode = PERSON_LIST_MODE.NORMAL;
	
	private PersonListAdapter adapterNormal;
	private PersonAttendenceListAdapter adapterAttendence;

	private PersonPersisterImpl persister;

	private Calendar attendenceDate;


	private AttendanceManager attManager;

	private List<Person> persons;

	public PersonListController() {

		attendenceDate = new GregorianCalendar();
		attendenceDate.set(Calendar.HOUR_OF_DAY, 0);
		attendenceDate.set(Calendar.MINUTE, 0);
		attendenceDate.set(Calendar.SECOND, 0);
		attendenceDate.set(Calendar.MILLISECOND, 0);

		persister = new PersonPersisterImpl(Factory.getInstance().getDatabase());
		persons = persister.getAllPersons();
	}

	public PERSON_LIST_MODE getMode() {
		return mode;
	}

	public void setMode(PERSON_LIST_MODE mode) {
		this.mode = mode;
	}

	public void filterPerson(String filter) {
		String whereClause;
		
		if(filter == null || filter.trim().isEmpty()){
			whereClause = null;			
		} else {
			whereClause = COLUMN_PERSON_PRENAME + " like '" + filter + "%' OR " + COLUMN_PERSON_SURNAME + " like '" + filter + "%'";
		}
		
		if(mode == PERSON_LIST_MODE.NORMAL) {
			final List<Person> personsWhere = persister.getPersonsWhere(whereClause);

			adapterNormal.clear();
			adapterNormal.addAll(personsWhere);
			
		} else if (mode == PERSON_LIST_MODE.ATTENDANCE) {
			adapterAttendence.clear();
			List<Attendance> allAttendances = persister.getAttendancesWhere(whereClause, this.attendenceDate);
			adapterAttendence.addAll(allAttendances);
			adapterAttendence.notifyDataSetChanged();
			initAttendenceManager(allAttendances);			
		}
	}

	public void setupAdapter(final FragmentActivity fragmentActivity, ListView listView, ListItemDragOnLongClickListener listener) {
		
		switch (mode) {
		case ATTENDANCE:
			List<Attendance> allAttendances = persister.getAttendancesWhere("", this.attendenceDate);
			initAttendenceManager(allAttendances);
			adapterAttendence = new PersonAttendenceListAdapter(fragmentActivity, allAttendances);
			listView.setAdapter(adapterAttendence);
			listView.setLongClickable(false);
			adapterAttendence.setCheckedChangeListener(this);
			break;
		case NORMAL:
		default:
			adapterNormal = new PersonListAdapter(fragmentActivity, persons);
			listView.setAdapter(adapterNormal);
			listView.setLongClickable(true);
			listView.setOnItemLongClickListener(listener);
			break;
		}
	}

	private void initAttendenceManager(List<Attendance> allAttendances) {
		attManager = new AttendanceManager(attendenceDate);
		for(Attendance a: allAttendances){
			if(a.getDate()!= null){
				attManager.add(a);
			}
		}
	}

	public void storeCurrentValues() {
		if(persister != null && attendenceDate != null && attManager != null)
			persister.storeAttendances(attendenceDate, attManager.getAttendences());
	}

	public Person getPersonAt(int position){
		Person p = null;
		switch (mode) {
		case ATTENDANCE:
			p = adapterAttendence.getItem(position).getPerson();
			break;
		case NORMAL:
		default:
			p = adapterNormal.getItem(position);
			break;
		
		}
		return p;
	}

	public Calendar getAttendanceDate() {
		return attendenceDate;
	}

	public void deletePerson(Person pers) {
		persister.deletePerson(pers);
	}	
	
	public void deleteNegativContacts() {
		persister.deleteNegativContacts();
	}

	@Override
	public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		exec.execute(new Runnable() {
			
			@Override
			public void run() {

				long personId = ((Long)buttonView.getTag()).longValue();
				List<Person> snapshot = new ArrayList<Person>();
				
				synchronized (persons) {
					snapshot.addAll(persons);
				}
				
				for(Person p:snapshot){
					if(p.getId()== personId){
						if(isChecked){
							attManager.attendent(p);
						} else {
							attManager.remove(p);
						}
						break;
					}
				}
			}
		});
		
		exec.shutdown();
	}

	public void setDate(int year, int monthOfYear, int dayOfMonth) {
		storeCurrentValues();
		attendenceDate.set(year, monthOfYear, dayOfMonth);
		adapterAttendence.setDate(attendenceDate);
	}

}
