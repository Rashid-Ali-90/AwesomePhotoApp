package com.awesome.photo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "photodatabase.db";
    public static final String TABLE_NAME = "images";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, image TEXT , author_name TEXT , description TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public int insertData(String imageURL, String authorName, String description)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("image", imageURL);
        contentValues.put("author_name", authorName);
        contentValues.put("description", description);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
        {
            return -1;
        }
        else {
            return 1;
        }
    }

    public List<ImageListModel> getData()
    {
        List<ImageListModel> resultList = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);

        if (cursor.moveToFirst())
        {
            do
            {
                ImageListModel imageListModel = new ImageListModel();
                imageListModel.setImageURL(cursor.getString(cursor.getColumnIndex("image")));
                imageListModel.setAuthorName(cursor.getString(cursor.getColumnIndex("author_name")));
                imageListModel.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                resultList.add(imageListModel);
            }
            while (cursor.moveToNext());
            cursor.close();
            db.close();
        }

        return resultList;
    }

    void deleteData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME); //delete all rows in a table
        db.close();
    }
}