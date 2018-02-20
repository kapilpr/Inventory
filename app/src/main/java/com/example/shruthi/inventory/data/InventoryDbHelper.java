package com.example.shruthi.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.shruthi.inventory.data.InventoryContract.InventoryEntry;

/**
 * Database helper for Inventory app. Manages database creation and version management.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "store.db";

    public static final int DATABASE_VERSION = 1;

    //Constructor is called when the database is called for the first time
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_PICTURE + " TEXT);";

        //Execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
