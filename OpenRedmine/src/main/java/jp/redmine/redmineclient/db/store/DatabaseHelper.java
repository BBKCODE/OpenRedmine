package jp.redmine.redmineclient.db.store;

import java.sql.SQLException;

import jp.redmine.redmineclient.entity.RedmineConnection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static String DB_NAME="OpenRedmine.db";
	private static int DB_VERSION=1;

    public DatabaseHelper(Context context) {
    	super(context, DB_NAME, null, DB_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		try {
			TableUtils.createTable(arg1, RedmineConnection.class);

		} catch (SQLException e) {
			Log.e("DatabaseHelper","onCreate",e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2,
			int arg3) {

	}

}
