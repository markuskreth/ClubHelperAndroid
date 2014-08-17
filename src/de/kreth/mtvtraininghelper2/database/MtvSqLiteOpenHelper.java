package de.kreth.mtvtraininghelper2.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.kreth.androiddbmanager.AndroidDatabase;
import de.kreth.clubhelperandroid.FactoryAndroid;
import de.kreth.dbmanager.ColumnDefinition;
import de.kreth.dbmanager.DataType;
import de.kreth.dbmanager.DbManager;
import de.kreth.dbmanager.TableDefinition;
import de.kreth.dbmanager.Version;

public class MtvSqLiteOpenHelper extends SQLiteOpenHelper {

	private String mTag;

	private PackageInfo packageInfo;

	private SQLiteDatabase writable = null;
	
	public static final String TABLE_PERSON = "PERSON";
	public static final String TABLE_ADRESS = "ADRESS";
	public static final String TABLE_CONTACT = "CONTACT";
	public static final String TABLE_ATTENDANCE = "ATTENDANCE";
	public static final String TABLE_PERSONNOTE = "PERSONNOTE";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PERSON_PRENAME = "prename";
	public static final String COLUMN_PERSON_SURNAME = "surname";
	public static final String COLUMN_PERSON_MOTHER = "mother";
	public static final String COLUMN_PERSON_FATHER = "father";
	public static final String COLUMN_PERSON_BIRTH = "birthdate";

	public static final String COLUMN_PERSON_FK = "personid";
	public static final String COLUMN_ADRESS_STREET = "street";
	public static final String COLUMN_ADRESS_ZIP = "zipcode";
	public static final String COLUMN_ADRESS_CITY = "city";
	
	public static final String COLUMN_CONTACT_TYPE = "type";
	public static final String COLUMN_CONTACT_VALUE = "value";

	public static final String COLUMN_ATTENDANCE_DATE = "date";
	
	public static final String COLUMN_PERSONNOTE_NOTE = "note";
	
	private List<TableDefinition> getTables() {
		List<TableDefinition> tables = new ArrayList<TableDefinition>();

		List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
		columns.add(new ColumnDefinition(DataType.TEXT, COLUMN_PERSON_PRENAME, "not null"));
		columns.add(new ColumnDefinition(DataType.TEXT, COLUMN_PERSON_SURNAME, "not null"));
		columns.add(new ColumnDefinition(DataType.INTEGER, COLUMN_PERSON_MOTHER, "null"));
		columns.add(new ColumnDefinition(DataType.INTEGER, COLUMN_PERSON_FATHER, "null"));
		columns.add(new ColumnDefinition(DataType.DATETIME, COLUMN_PERSON_BIRTH, "null"));
		TableDefinition def = new TableDefinition(TABLE_PERSON, columns);
		tables.add(def);

		columns = new ArrayList<ColumnDefinition>();
		columns.add(new ColumnDefinition(DataType.INTEGER, COLUMN_PERSON_FK, "not null"));
		columns.add(new ColumnDefinition(DataType.TEXT, COLUMN_ADRESS_STREET, "not null"));
		columns.add(new ColumnDefinition(DataType.TEXT, COLUMN_ADRESS_ZIP, "not null"));
		columns.add(new ColumnDefinition(DataType.TEXT, COLUMN_ADRESS_CITY, "not null"));
		def = new TableDefinition(TABLE_ADRESS, columns);
		tables.add(def);
		
		columns = new ArrayList<ColumnDefinition>();
		columns.add(new ColumnDefinition(DataType.INTEGER, COLUMN_PERSON_FK, "not null"));
		columns.add(new ColumnDefinition(DataType.TEXT, COLUMN_CONTACT_TYPE, "not null"));
		columns.add(new ColumnDefinition(DataType.TEXT, COLUMN_CONTACT_VALUE, "not null"));
		def = new TableDefinition(TABLE_CONTACT, columns);
		tables.add(def);

		columns = new ArrayList<ColumnDefinition>();
		columns.add(new ColumnDefinition(DataType.INTEGER, COLUMN_PERSON_FK, "not null"));
		columns.add(new ColumnDefinition(DataType.DATETIME, COLUMN_ATTENDANCE_DATE, "not null"));
		def = new TableDefinition(TABLE_ATTENDANCE, columns);
		tables.add(def);

		columns = new ArrayList<ColumnDefinition>();
		columns.add(new ColumnDefinition(DataType.INTEGER, COLUMN_PERSON_FK, "not null"));
		columns.add(new ColumnDefinition(DataType.TEXT, COLUMN_PERSONNOTE_NOTE, "not null"));
		def = new TableDefinition(TABLE_ATTENDANCE, columns);
		tables.add(def);
		
		return tables;
	}

	public MtvSqLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		mTag = FactoryAndroid.getInstance().TAG() + "_" + getClass().getSimpleName();
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			FactoryAndroid.getInstance().handleException(Log.WARN, mTag, e);
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		DbManager manager = new DbManager(getTables(), getVersions(), new AndroidDatabase(db), getCurrentVersion());
		try {
			manager.execute();
		} catch (SQLException e) {
			FactoryAndroid.getInstance().handleException(Log.WARN, mTag, e);
		}
		
	}

	@Override
	public SQLiteDatabase getWritableDatabase() {
		if(writable == null)
			writable= super.getWritableDatabase();
		return writable; 
	}
	
	private int getCurrentVersion() {
		
		if(packageInfo != null)
			return 1;
		
		return packageInfo.versionCode;
	}

	private List<Version> getVersions() {
		List<Version> versionen = new ArrayList<Version>();
		versionen.add(new Version(1, Arrays.asList(new String[]{CREATE_PERSON, CREATE_ADRESS, CREATE_CONTACT})));
		versionen.add(new Version(4, Arrays.asList(v4)));
		versionen.add(new Version(5, Arrays.asList(v5_attendance)));
		return versionen;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(mTag + "_onUpgrade", "onUpgrade: oldVersion=" + oldVersion + ", newVersion=" + newVersion);
		DbManager manager = new DbManager(getTables(), getVersions(), new AndroidDatabase(db), newVersion);
		try {
			manager.execute();
		} catch (SQLException e) {
			FactoryAndroid.getInstance().handleException(Log.WARN, mTag, e);
		}
	}

	private static final String CREATE_PERSON = "create table " + TABLE_PERSON + " (" +
			COLUMN_ID + " integer primary key autoincrement, " +
			COLUMN_PERSON_PRENAME + " text not null, " +
			COLUMN_PERSON_SURNAME + " text not null, " +
			COLUMN_PERSON_MOTHER + " integer null, " +
			COLUMN_PERSON_FATHER + " integer null, " +
			COLUMN_PERSON_BIRTH + " integer null" + ")";

	private static final String CREATE_ADRESS = "create table " + TABLE_ADRESS + "(" +
			COLUMN_ID + " integer primary key autoincrement, " +
			COLUMN_PERSON_FK + " integer not null, " +
			COLUMN_ADRESS_STREET + " text not null, " +
			COLUMN_ADRESS_ZIP + " text not null, " +
			COLUMN_ADRESS_CITY + " text not null, " +
			"FOREIGN KEY(" + COLUMN_PERSON_FK + ") REFERENCES " + TABLE_PERSON + "(" + COLUMN_ID + "))";

	private static final String CREATE_CONTACT = "create table " + TABLE_CONTACT + "(" +
			COLUMN_ID + " integer primary key autoincrement," +
			COLUMN_PERSON_FK + " integer not null," +
			COLUMN_CONTACT_TYPE + " text not null," +
			COLUMN_CONTACT_VALUE + " text not null," +
			"FOREIGN KEY(" + COLUMN_PERSON_FK + ") REFERENCES " + TABLE_PERSON + "(" + COLUMN_ID + "))";

	private static final String[] v4 = {
		"DROP TABLE IF EXISTS " + TABLE_ADRESS,
		CREATE_ADRESS,
		"CREATE TABLE CONTACT_TMP (_id  integer primary key autoincrement,personid  integer not null,type  text not null,value text not null,FOREIGN KEY(personid) REFERENCES PERSON(_id))",
		"insert into CONTACT_TMP  (_id, personid, type, value) select _id, personid, type, value from CONTACT",
		"DROP TABLE CONTACT",
		"ALTER TABLE CONTACT_TMP RENAME TO CONTACT"
	};
	
	private static final String[] v5_attendance = {
		"CREATE TABLE ATTENDANCE (_id  integer primary key autoincrement, personid  integer not null, date  integer not null, " +
		"FOREIGN KEY(personid) REFERENCES PERSON(_id), " +
		"UNIQUE(personid, date) ON CONFLICT REPLACE)",
		"CREATE TABLE PERSONNOTE (_id  integer primary key autoincrement,personid  integer not null,note  text not null,FOREIGN KEY(personid) REFERENCES PERSON(_id))"
	};
}
