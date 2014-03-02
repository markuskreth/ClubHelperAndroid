package de.kreth.clubhelperandroid;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.config.PropertyConfigurator;

import de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper;

public class Factory {
	
	protected static Factory instance = null;
	private Logger logger = null;
	private static final String DB_NAME = "database.sqlite";
	private static final String TAG = "MTV";
	
	private MtvSqLiteOpenHelper sqliteHelper;
	private SQLiteDatabase db;
	private Resources resources;
	
	protected Factory() {
	}
	
	public static Factory getInstance() {
		if(instance == null)
			instance = new Factory();			
		return instance;
	}
	
	void init(Context context) throws NameNotFoundException {

		PropertyConfigurator.getConfigurator(context).configure();
		
		logger  = LoggerFactory.getLogger(getClass());
		PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		int versionCode = packageInfo.versionCode;
		logger.info("Initialisiere " + MtvSqLiteOpenHelper.class.getSimpleName() + " for Version " + versionCode + " - '" + packageInfo.versionName + "'");
		sqliteHelper = new MtvSqLiteOpenHelper(context, getDbName(), null , versionCode);
		resources = context.getResources();
	}
	
	public String getString(int id, Object... formatArgs) throws IllegalArgumentException {
		return resources.getString(id, formatArgs);
	}

	public String getString(int id) throws IllegalArgumentException {
		return resources.getString(id);
	}

	public String getDbName() {
		return DB_NAME;
	}

	public SQLiteDatabase getDatabase() {
		if(db == null)
			db = sqliteHelper.getWritableDatabase();

		return db;
	}
	
	
	public String TAG(){
		return TAG;
	}
	
	public void handleException(int priority, String tag, Exception e){
		switch (priority) {
		case Log.DEBUG:
			Log.d(tag, "Exception aufgetreten: ", e);
			break;
		case Log.ERROR:
			Log.e(tag, "Exception aufgetreten: ", e);
			break;
		case Log.INFO:
			Log.i(tag, "Exception aufgetreten: ", e);
			break;
		case Log.VERBOSE:
			Log.v(tag, "Exception aufgetreten: ", e);
			break;
		case Log.WARN:
			Log.w(tag, "Exception aufgetreten: ", e);
			break;
		default:
			Log.e(tag, "Exception aufgetreten: ", e);
			break;
		}
	}

	public void shutdown() {
		sqliteHelper.close();
	}
}
