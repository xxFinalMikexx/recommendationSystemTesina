package com.example.xxfin.recommendationsystemtesina.objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by xxfin on 01/05/2017.
 */

public class PlacesSQLiteHelper extends SQLiteOpenHelper{
    String sqlCreate = "CREATE TABLE places (placeId TEXT, nombre TEXT)";

    public PlacesSQLiteHelper(Context contexto) {
        super(contexto, "LocalPlaces", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE places(placeId VARCHAR PRIMARY KEY, nombre VARCHAR);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST places");
        onCreate(db);
    }

    public boolean insertPlace(String nombre, String placeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("placeId", placeId);
        contentValues.put("nombre", nombre);

        db.insert("places", null, contentValues);
        return true;
    }

    public Cursor getData(String placeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM places WHERE placeId = "+placeId, null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, "places");
        return numRows;
    }

    public ArrayList<String> getAllContacts() {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM places", null);

        while(res.isAfterLast() == false) {
            array_list.add(res.getString(res.getColumnIndex("placeId")));
            res.moveToNext();
        }

        return array_list;
    }
}
