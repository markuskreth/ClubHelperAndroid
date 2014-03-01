package de.kreth.mtvandroidhelper2.data;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Person extends PersistentDataObject {
	private String preName;
	private String surName;
	private Person father;
	private Person mother;
	private Calendar birthdate;
	
	public Person(int id) {
		super(id);
	}
	
	public Person() {
		super();
	}
	
	public String getPreName() {
		return preName;
	}
	public void setPreName(String preName) {
		this.preName = preName;
	}
	public String getSurName() {
		return surName;
	}
	public void setSurName(String surName) {
		this.surName = surName;
	}
	public Person getFather() {
		return father;
	}
	public void setFather(Person father) {
		this.father = father;
	}
	public Person getMother() {
		return mother;
	}
	public void setMother(Person mother) {
		this.mother = mother;
	}
	public Calendar getBirthdate() {
		return birthdate;
	}
	
	public void setBirthdate(Calendar birthdate) {
		this.birthdate = birthdate;
	}
	
	@Override
	public String toString() {
		return preName + " " + surName;
	}
	
	public static Person createNew() {
		Person person = new Person();
		person.setBirthdate(getDefaultGeburtsDatum());
		person.setPreName("");
		person.setSurName("");
		return person;
	}
	
	public static Calendar getDefaultGeburtsDatum() {
		return new GregorianCalendar(1990,Calendar.JANUARY, 1);
	}
	
}
