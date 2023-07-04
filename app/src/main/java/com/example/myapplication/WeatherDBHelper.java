package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDBHelper extends SQLiteOpenHelper {
    // 数据库名称和版本号
    public static final String DATABASE_NAME = "weather.db";
    public static final int DATABASE_VERSION = 1;

    // 表名和字段名称
    public static final String TABLE_NAME = "weatherInfo";
    public static final String COLUMN_ID = "id";

    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_UPDATE_TIME = "update_time";
    public static final String COLUMN_TEMPERATURE = "temperature";
    public static final String COLUMN_HUMIDITY = "humidity";
    public static final String COLUMN_PM25 = "pm25";

    public static final String DATA = "Data";

    public WeatherDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建数据表
        String createTableQuery = "CREATE TABLE " + WeatherDBHelper.TABLE_NAME + " (" +
                WeatherDBHelper.COLUMN_ID + " INTEGER PRIMARY KEY," +
                WeatherDBHelper.COLUMN_CODE + " TEXT," +
                WeatherDBHelper.COLUMN_CITY + " TEXT," +
                WeatherDBHelper.COLUMN_DATE + " TEXT," +
                WeatherDBHelper.COLUMN_UPDATE_TIME + " TEXT," +
                WeatherDBHelper.COLUMN_TEMPERATURE + " REAL," +
                WeatherDBHelper.COLUMN_HUMIDITY + " TEXT," +
                WeatherDBHelper.COLUMN_PM25 + " TEXT," +
                WeatherDBHelper.DATA + " TEXT," +
                "UNIQUE (" +
                WeatherDBHelper.COLUMN_CITY + ", " +
                WeatherDBHelper.COLUMN_DATE +
                ") ON CONFLICT REPLACE" +
                ");";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 在数据库升级时执行必要的操作（如果有）
        // 这里可以处理表结构的修改或数据的迁移
        // 在示例中，我们只是删除旧表并重新创建新表
        String dropTableQuery = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTableQuery);
        onCreate(db);
    }
}