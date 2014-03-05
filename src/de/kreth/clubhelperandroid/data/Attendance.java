package de.kreth.clubhelperandroid.data;

import java.util.Calendar;

public class Attendance extends PersistentDataObject {

	private Person person;
	private Calendar date;

	@Override
	public String toString() {
		return date.getTime() + " - " + person;
	}
	
	public Attendance(long insertId, Person person, Calendar date) {
		super(insertId);
		this.person = person;
		this.date = date;
	}

	public Attendance(Person person, Calendar date) {
		super();
		this.person = person;
		this.date = date;
	}

	public Person getPerson() {
		return person;
	}

	public Calendar getDate() {
		return date;
	}
	
}
