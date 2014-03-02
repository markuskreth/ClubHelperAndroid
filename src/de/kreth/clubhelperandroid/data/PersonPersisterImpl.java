package de.kreth.clubhelperandroid.data;

import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.*;

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
	private static final String[] CONTACT_COLUMNS = {COLUMN_ID, COLUMN_PERSON_FK, COLUMN_CONTACT_TYPE, COLUMN_CONTACT_VALUE};
	private List<String> contactColumnNames = Arrays.asList(CONTACT_COLUMNS);

	private static final String[] PERSON_COLUMNS = {COLUMN_ID, COLUMN_PERSON_PRENAME, COLUMN_PERSON_SURNAME, COLUMN_PERSON_BIRTH};
	private static final String ATTENDANCE_ID = "attendanceID";
	private List<String> personColumnNames = Arrays.asList(PERSON_COLUMNS);

	private SQLiteDatabase db;
	private SQLiteStatement stmInsertPersonContracts;
	private SQLiteStatement stmDeletePersonContracts;
	private SQLiteStatement stmInsertAttendance;
	private SQLiteStatement stmUpdateAttendance;

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
	public Person getPersonById(long personId){
		
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
	 * @param whereClause null oder where-bedingung wie in sql ohne "WHERE" 
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
	 * liefert eine auswahl an Personen auf die die whereClause zutrifft.
	 * @param whereClause null oder where-bedingung wie in sql ohne "WHERE"
	 * @return
	 */
	public List<Attendance> getAttendancesWhere(String whereClause, Calendar forDate) {
		List<Attendance> result = new ArrayList<Attendance>();

		String sql = "select " + TABLE_PERSON + ".*," + COLUMN_ATTENDANCE_DATE + ", " + TABLE_ATTENDANCE + "." + COLUMN_ID + " AS " + ATTENDANCE_ID 
				+ " from " + TABLE_PERSON + " LEFT JOIN " + TABLE_ATTENDANCE 
				+ " ON " + TABLE_PERSON + "." + COLUMN_ID + "=" + TABLE_ATTENDANCE + "." + COLUMN_PERSON_FK + " AND " + COLUMN_ATTENDANCE_DATE + "=" + forDate.getTimeInMillis() + (whereClause != null && ! whereClause.isEmpty() ? ( " WHERE " + whereClause) : "");

		Cursor query = db.rawQuery(sql, null);
		
		while(query.moveToNext()) {
			Person pers = cursorToPerson(query);
			Calendar calendar = null;
			Attendance a;
			
			if( ! query.isNull(query.getColumnIndex(COLUMN_ATTENDANCE_DATE))) {
				calendar = new GregorianCalendar();
				calendar.setTime(new Date(query.getLong(query.getColumnIndex(COLUMN_ATTENDANCE_DATE))));
				int id = query.getInt(query.getColumnIndex(ATTENDANCE_ID));
				a = new Attendance(id, pers, calendar);
			} else {
				a = new Attendance(pers, null);
			}
			
			result.add(a);
		}
		
		query.close();
		return result;
	}

	/**
	 * No data of {@link Person} are stored! Only the join is stored.
	 * @param a
	 * @param attendenceDate 
	 * @return
	 */
	public Attendance storeAttendance(Attendance a, Calendar attendenceDate){
		if(a.getPerson().isPersistent()){
			if(a.isPersistent())
				return updateAttendance(a, attendenceDate);
			else
				return insertAttendance(a, attendenceDate);
		} else
			throw new IllegalStateException("Person " + a.getPerson() + " must be persistent before storeAttendance");
	}
	
	private Attendance updateAttendance(Attendance a, Calendar attendenceDate) {
		if (stmUpdateAttendance == null) {
			String sql = "UPDATE " + TABLE_ATTENDANCE + " SET " + COLUMN_ATTENDANCE_DATE + "=? WHERE " + COLUMN_ID + "=?";
			stmUpdateAttendance = db.compileStatement(sql);
		}
		if (attendenceDate == null && a.getDate() != null){
			String whereClause = COLUMN_PERSON_FK  + "=" + a.getPerson().getId() + " AND " + COLUMN_ATTENDANCE_DATE + "=" + a.getDate().getTimeInMillis();
			db.delete(TABLE_ATTENDANCE, whereClause, null);
			a.getDate().setTimeInMillis(0);
		} else{
			stmUpdateAttendance.bindLong(1, attendenceDate.getTimeInMillis());
			stmUpdateAttendance.bindLong(2, a.getId());
			int executeUpdateDelete = stmUpdateAttendance.executeUpdateDelete();
			
			if(executeUpdateDelete != 1)
				throw new IllegalStateException("While updateing " + TABLE_ATTENDANCE + " with id=" + a.getId() + " instead of 1 there were updates on rowCount=" + executeUpdateDelete);

			a.getDate().setTimeInMillis(attendenceDate.getTimeInMillis());
		}
		
		return a;
	}

	private Attendance insertAttendance(Attendance a, Calendar attendenceDate) {
		if (stmInsertAttendance == null) {
			String sql = "INSERT INTO " + TABLE_ATTENDANCE + " (" + COLUMN_PERSON_FK + "," + COLUMN_ATTENDANCE_DATE + ") VALUES (?,?)";
			stmInsertAttendance = db.compileStatement(sql);
		}

		stmInsertAttendance.bindLong(1, a.getPerson().getId());
		if(attendenceDate != null)
			stmInsertAttendance.bindLong(2, attendenceDate.getTimeInMillis());
		long insertId = stmInsertAttendance.executeInsert();
		
		return new Attendance(insertId, a.getPerson(), attendenceDate);
	}

	/**
	 * Niemals null - leeres Array
	 * @param person
	 * @return
	 */
	public List<PersonContact> getContactsFor(Person person) {
		List<PersonContact> result = new ArrayList<PersonContact>();
		Cursor query = db.query(TABLE_CONTACT, CONTACT_COLUMNS, COLUMN_PERSON_FK + "=" + person.getId(), null, null, null, null);
		
		while(query.moveToNext()){
			PersonContact contact = cursorToContact(query);
			result.add(contact);
		}
		
		query.close();
		return result;
	}

	private PersonContact cursorToContact(Cursor query) {
		int id = query.getInt(contactColumnNames.indexOf(COLUMN_ID));
		int personId = query.getInt(contactColumnNames.indexOf(COLUMN_PERSON_FK));
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
			String sql = "DELETE FROM " + TABLE_CONTACT + " WHERE " + COLUMN_PERSON_FK + "=?";
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
					COLUMN_PERSON_FK + "," +
					COLUMN_CONTACT_TYPE + "," +
					COLUMN_CONTACT_VALUE +
					") VALUES (?,?,?)";
			stmInsertPersonContracts = db.compileStatement(sql);
		}
	}

}
