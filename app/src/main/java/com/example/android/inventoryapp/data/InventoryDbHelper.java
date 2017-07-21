package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

/**
 * Created by David on 18/07/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "inventory.db";  // Database filename.
    public static final int DATABASE_VERSION = 1;               // Current version of the database.

    /**
     * Constructor for this class.
     *
     * @param context is the context to open or create the database.
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of tables
     * and the initial population of the tables should happen.
     *
     * @param db is the database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement for creating the "products" table.
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                ProductEntry.COLUMN_NAME_ID + " " + ProductEntry.COLUMN_TYPE_ID + " " + ProductEntry.COLUMN_CONSTRAINTS_ID + ", " +
                ProductEntry.COLUMN_NAME_PRODUCT + " " + ProductEntry.COLUMN_TYPE_PRODUCT + " " + ProductEntry.COLUMN_CONSTRAINTS_PRODUCT + ", " +
                ProductEntry.COLUMN_NAME_DESCRIPTION + " " + ProductEntry.COLUMN_TYPE_DESCRIPTION + " " + ProductEntry.COLUMN_CONSTRAINTS_DESCRIPTION + ", " +
                ProductEntry.COLUMN_NAME_IMAGE + " " + ProductEntry.COLUMN_TYPE_IMAGE + " " + ProductEntry.COLUMN_CONSTRAINTS_IMAGE + ", " +
                ProductEntry.COLUMN_NAME_PRICE + " " + ProductEntry.COLUMN_TYPE_PRICE + " " + ProductEntry.COLUMN_CONSTRAINTS_PRICE + ", " +
                ProductEntry.COLUMN_NAME_QUANTITY + " " + ProductEntry.COLUMN_TYPE_QUANTITY + " " + ProductEntry.COLUMN_CONSTRAINTS_QUANTITY + ", " +
                ProductEntry.COLUMN_NAME_SUPPLIER_CONTACT + " " + ProductEntry.COLUMN_TYPE_SUPPLIER_CONTACT + " " + ProductEntry.COLUMN_CONSTRAINTS_SUPPLIER_CONTACT + ", " +
                ProductEntry.COLUMN_NAME_SUPPLIER_EMAIL + " " + ProductEntry.COLUMN_TYPE_SUPPLIER_EMAIL + " " + ProductEntry.COLUMN_CONSTRAINTS_SUPPLIER_EMAIL + ", " +
                ProductEntry.COLUMN_NAME_SUPPLIER_ORDER_QUANTITY + " " + ProductEntry.COLUMN_TYPE_SUPPLIER_ORDER_QUANTITY + " " + ProductEntry.COLUMN_CONSTRAINTS_SUPPLIER_ORDER_QUANTITY + ")";

        // Execute the SQL statement.
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * Called when the database needs to be upgraded. The implementation should use this method to
     * drop tables, add tables, or do anything else it needs to upgrade to the new schema version.
     *
     * @param db         is the database.
     * @param oldVersion is the old database version.
     * @param newVersion is the new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
