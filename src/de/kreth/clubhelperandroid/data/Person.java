package de.kreth.clubhelperandroid.data;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Person extends PersistentDataObject {
	private String preName;
	private String surName;
	private Person father;
	private Person mother;
	private Calendar birthdate;
	
	public Person(Person toClone){
		super(toClone.getId());
		this.preName = toClone.preName;
		this.surName = toClone.surName;
		this.father = toClone.getFather();
		this.mother = toClone.getMother();
		this.birthdate = toClone.getBirthdate();
	}
	
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
	
	/**
	 * Delivers Clone of Father
	 * @return Clone of Father
	 */
	public Person getFather() {
		if(father != null)
			return new Person(father);
		return father;
	}
	
	/**
	 * @param father
	 */
	public void setFather(Person father) {
		this.father = new Person(father);
	}
	
	/**
	 * Delivers Clone of Mother
	 * @return	Clone of Mother
	 */
	public Person getMother() {
		if(mother != null)
			return new Person(mother);
		return mother;
	}
	
	public void setMother(Person mother) {
		this.mother = new Person(mother);
	}
	
	public Calendar getBirthdate() {
		return (Calendar) birthdate;
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
