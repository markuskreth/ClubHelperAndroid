package de.kreth.mtvandroidhelper2.data;

import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_CONTACT_ADRESS_FK_PERSON;
import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_CONTACT_TYPE;
import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_CONTACT_VALUE;
import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_ID;
import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_PERSON_BIRTH;
import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_PERSON_PRENAME;
import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_PERSON_SURNAME;
import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.TABLE_CONTACT;
import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.TABLE_PERSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class PersonPersisterImpl {
	private static final String[] CONTACT_COLUMNS = {COLUMN_ID, COLUMN_CONTACT_ADRESS_FK_PERSON, COLUMN_CONTACT_TYPE, COLUMN_CONTACT_VALUE};
	private List<String> contactColumnNames = Arrays.asList(CONTACT_COLUMNS);
	
	private static final String[] PERSON_COLUMNS = {COLUMN_ID, COLUMN_PERSON_PRENAME, COLUMN_PERSON_SURNAME, COLUMN_PERSON_BIRTH};
	private List<String> personColumnNames = Arrays.asList(PERSON_COLUMNS);
	
	private SQLiteDatabase db;
	private SQLiteStatement stmInsertPersonContracts;
	private SQLiteStatement stmDeletePersonContracts;

	public PersonPersisterImpl(SQLiteDatabase db) {
		this.db = db;
	}
	
	public void close(){
		db.close();
	}
	
	/**
	 * Niemals null - leere Person.
	 * @param personId
	 * @return
	 */
	public Person getPersonById(int personId){
		
		if(personId<0)
			return Person.createNew();
		
		Cursor query = db.query(TABLE_PERSON, PERSON_COLUMNS, COLUMN_ID + "=" + personId, null, null, null, null);
		
		if(query.getCount() != 1){
			query.close();
			throw new IllegalStateException("Person mit personId=" + personId + " hat " + query.getCount() + " Datensätze geliefert - nicht oder mehrfach vorhanden?");
		}
		
		if( ! query.moveToFirst()){
			query.close();
			throw new IllegalStateException("Person mit personId=" + personId + " hat " + query.getCount() + " Datensätze geliefert - konnte aber nicht zu erstem Datensatz springen!?");
		}
		Person cursorToPerson = cursorToPerson(query);
		query.close();
		return cursorToPerson;		
	}

	public List<Person> getAllPersons() {
		return getPersonsWhere(null);
	}

	/**
	 * liefert eine auswahl an Personen auf die die whereClause zutrifft.
	 * @param whereClause null oder where-bedingung wie in sql
	 * @return
	 */
	public List<Person> getPersonsWhere(String whereClause) {
		List<Person> result = new ArrayList<Person>();

		Cursor query = db.query(TABLE_PERSON, PERSON_COLUMNS, whereClause, null, null, null, COLUMN_PERSON_PRENAME + "," + COLUMN_PERSON_SURNAME);
		
		while(query.moveToNext())
			result.add(cursorToPerson(query));
		
		query.close();
		return result;
	}

	/**
	 * Niemals null - leeres Array
	 * @param person
	 * @return
	 */
	public List<PersonContact> getContactsFor(Person person) {
		List<PersonContact> result = new ArrayList<PersonContact>();
		Cursor query = db.query(TABLE_CONTACT, CONTACT_COLUMNS, COLUMN_CONTACT_ADRESS_FK_PERSON + "=" + person.getId(), null, null, null, null);
		
		while(query.moveToNext()){
			PersonContact contact = cursorToContact(query);
			result.add(contact);
		}
		
		query.close();
		return result;
	}

	private PersonContact cursorToContact(Cursor query) {
		int id = query.getInt(contactColumnNames.indexOf(COLUMN_ID));
		int personId = query.getInt(contactColumnNames.indexOf(COLUMN_CONTACT_ADRESS_FK_PERSON));
		ContactType type = ContactType.valueOf(query.getString(contactColumnNames.indexOf(COLUMN_CONTACT_TYPE)));
		String value = query.getString(contactColumnNames.indexOf(COLUMN_CONTACT_VALUE));
		return new PersonContact(id, personId, type, value);
	}

	private Person cursorToPerson(Cursor query) {
		
		Person p = new Person(query.getInt(personColumnNames.indexOf(COLUMN_ID)));
		p.setPreName(query.getString(personColumnNames.indexOf(COLUMN_PERSON_PRENAME)));
		p.setSurName(query.getString(personColumnNames.indexOf(COLUMN_PERSON_SURNAME)));
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date(query.getLong(personColumnNames.indexOf(COLUMN_PERSON_BIRTH))));
		p.setBirthdate(calendar);
		return p;
	}

	public void storePerson(Person person){
		if(person.isPersistent())
			updatePerson(person);
		else
			insertPerson(person);
	}
	
	private void updatePerson(Person person) {
		String whereClause = COLUMN_ID + "=?";
		String[] whereArgs = {String.valueOf(person.getId())};
		ContentValues contentVal = personToContent(person);
		int update = db.update(TABLE_PERSON, contentVal, whereClause, whereArgs);
		if(update != 1)
			throw new IllegalStateException("Beim Update wurde keine Zeile geändert! Anzahl zeilen=" + update);
	}

	private void insertPerson(Person person) {
		long insert = db.insert(TABLE_PERSON, null, personToContent(person));
		person.setId((int)insert);
	}

	private ContentValues personToContent(Person person){

		ContentValues values = new ContentValues();
		values.put(COLUMN_PERSON_SURNAME, person.getSurName());
		values.put(COLUMN_PERSON_PRENAME, person.getPreName());
		values.put(COLUMN_PERSON_BIRTH, person.getBirthdate().getTimeInMillis());
		
		return values;
	}

	public void deletePerson(Person data) {
		if(data.getId() >=0){
			int delete = db.delete(TABLE_PERSON, COLUMN_ID + "=" + data.getId(), null);
			if(delete != 1)
				throw new IllegalStateException("Der Versuch " + data + " zu löschen ist fehlgeschlagen!");
		}
	}

	public void storePersonContacts(List<PersonContact> data) {
		
		deletePersonContacts(data);
		
		for(PersonContact con: data){
			insertPersonContacts(con);
		}
	}

	public void deletePersonContacts(List<PersonContact> data) {
		secureStmDeletePersonContractsExists();

		for(PersonContact con: data){
			stmDeletePersonContracts.bindLong(1, con.getPersonId());
			stmDeletePersonContracts.executeUpdateDelete();
		}
	}

	private void secureStmDeletePersonContractsExists() {
		if(stmDeletePersonContracts == null){
			String sql = "DELETE FROM " + TABLE_CONTACT + " WHERE " + COLUMN_CONTACT_ADRESS_FK_PERSON + "=?";
			stmDeletePersonContracts = db.compileStatement(sql);
		}
		
	}

	private void insertPersonContacts(PersonContact con) {
		secureStmInsertPersonContractsExists();
		stmInsertPersonContracts.bindLong(1, con.getPersonId());
		stmInsertPersonContracts.bindString(2, con.getType().name());
		stmInsertPersonContracts.bindString(3, con.getValue());
		int executeInsert = (int) stmInsertPersonContracts.executeInsert();
		con.setId(executeInsert);
	}

	private void secureStmInsertPersonContractsExists() {
		if(stmInsertPersonContracts == null) {

			String sql = "INSERT INTO " + TABLE_CONTACT + "(" +
					COLUMN_CONTACT_ADRESS_FK_PERSON + "," +
					COLUMN_CONTACT_TYPE + "," +
					COLUMN_CONTACT_VALUE +
					") VALUES (?,?,?)";
			stmInsertPersonContracts = db.compileStatement(sql);
		}
	}

}
