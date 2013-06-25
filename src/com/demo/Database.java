package com.demo;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database {
	
	private static final String TAG = "Database";
	private static final boolean DEBUG = true;
	
	/**
	 * 保存发送记录
	 * @param content 发送的信息
	 * @param send 信息是否发送成功了
	 * @return
	 */
    public static boolean saveContent(Context context ,Content content ,boolean send){
		String type = null;
		String data = null;
		if (content.message != null) {
			type = MySQLiteOpenHelper.COLUMN_TYPE_MESSAGE;
			data = content.message;
		} else if (content.image != null) {
			type = MySQLiteOpenHelper.COLUMN_TYPE_IMAGE;
			data = content.image.getAbsolutePath();
		} else if (content.audio != null) {
			type = MySQLiteOpenHelper.COLUMN_TYPE_AUDIO;
			data = content.audio.getAbsolutePath();
		} else if (content.file != null) {
			type = MySQLiteOpenHelper.COLUMN_TYPE_FILE;
			data = content.file.getAbsolutePath();
		}
		if (type != null) {
			SQLiteDatabase db = new MySQLiteOpenHelper(context).getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(MySQLiteOpenHelper.COLUMN_TYPE, type);
			values.put(MySQLiteOpenHelper.COLUMN_CONTENT, data);
			values.put(MySQLiteOpenHelper.COLUMN_SEND ,send);
			values.put(MySQLiteOpenHelper.COLUMN_TIME ,System.currentTimeMillis());
			db.insert(MySQLiteOpenHelper.TABLE_NAME, null, values);
			db.close();
			return true;
		}
		return false;
    }
    
    public static ArrayList<Content> getContent(Context context){
    	SQLiteDatabase db = new MySQLiteOpenHelper(context).getReadableDatabase();
    	Cursor c = db.query(MySQLiteOpenHelper.TABLE_NAME ,null ,null ,null ,null ,null
    			,MySQLiteOpenHelper.COLUMN_TIME + " DESC ", " 0 , " + 20);
    	ArrayList<Content> list = new ArrayList<Content>();
    	if(c != null){
    		if(c.moveToFirst()){
    			String type = null;
    			String value = null;
    			do{
    				Content content = new Content();
    				type = c.getString(c.getColumnIndex(MySQLiteOpenHelper.COLUMN_TYPE));
    				value = c.getString(c.getColumnIndex(MySQLiteOpenHelper.COLUMN_CONTENT));
    				if(type.equalsIgnoreCase(MySQLiteOpenHelper.COLUMN_TYPE_IMAGE)){
    					content.image = new File(value);
    				}else if(type.equalsIgnoreCase(MySQLiteOpenHelper.COLUMN_TYPE_AUDIO)){
    					content.audio = new File(value);
    				}else if(type.equalsIgnoreCase(MySQLiteOpenHelper.COLUMN_TYPE_FILE)){
    					content.file = new File(value);
    				}else if(type.equalsIgnoreCase(MySQLiteOpenHelper.COLUMN_TYPE_MESSAGE)){
    					content.message = value;
    				}
    				list.add(content);
    			}while(c.moveToNext());
    		}
    	}
    	db.close();
    	if(DEBUG){
    		Log.i(TAG ,"getContent. list size is " + list.size());
    	}
    	return list;
    }
    
  	private static class MySQLiteOpenHelper extends SQLiteOpenHelper {
  		
  		private static final String DATABASE_NAME = "history";
  		private static final int DATABASE_VERSION = 12;
  		private static final String TABLE_NAME = "content";
  		
  		private static final String COLUMN_TYPE = "type";
  		private static final String COLUMN_CONTENT = "content";
  		private static final String COLUMN_SEND = "send";
  		private static final String COLUMN_TIME = "time";
  		
  		private static final String COLUMN_TYPE_IMAGE = "image";
  		private static final String COLUMN_TYPE_FILE = "file";
  		private static final String COLUMN_TYPE_AUDIO = "audio";
  		private static final String COLUMN_TYPE_MESSAGE = "message";

  		MySQLiteOpenHelper(Context context) {
  			super(context, DATABASE_NAME, null, DATABASE_VERSION);
  		}

  		@Override
  		public void onCreate(SQLiteDatabase db) {
  			db.execSQL("create table " + TABLE_NAME +  " (_id integer primary key autoincrement, "
  		             + COLUMN_CONTENT + " text not null, "
  		             + COLUMN_TYPE + " text not null, "
  		             + COLUMN_TIME + " long, "
  		             + COLUMN_SEND + " boolean );");

  		}

  		@Override
  		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  		}

  	}

}
