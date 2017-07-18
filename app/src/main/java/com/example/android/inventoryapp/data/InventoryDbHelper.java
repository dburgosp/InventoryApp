package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.ProductsEntry;

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
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + ProductsEntry.TABLE_NAME + " (" +
                ProductsEntry.COLUMN_NAME_ID + " " + ProductsEntry.COLUMN_TYPE_ID + " " + ProductsEntry.COLUMN_CONSTRAINTS_ID + ", " +
                ProductsEntry.COLUMN_NAME_PRODUCT + " " + ProductsEntry.COLUMN_TYPE_PRODUCT + " " + ProductsEntry.COLUMN_CONSTRAINTS_PRODUCT + ", " +
                ProductsEntry.COLUMN_NAME_DESCRIPTION + " " + ProductsEntry.COLUMN_TYPE_DESCRIPTION + " " + ProductsEntry.COLUMN_CONSTRAINTS_DESCRIPTION + ", " +
                ProductsEntry.COLUMN_NAME_IMAGE + " " + ProductsEntry.COLUMN_TYPE_IMAGE + " " + ProductsEntry.COLUMN_CONSTRAINTS_IMAGE + ", " +
                ProductsEntry.COLUMN_NAME_PRICE + " " + ProductsEntry.COLUMN_TYPE_PRICE + " " + ProductsEntry.COLUMN_CONSTRAINTS_PRICE + ", " +
                ProductsEntry.COLUMN_NAME_QUANTITY + " " + ProductsEntry.COLUMN_TYPE_QUANTITY + " " + ProductsEntry.COLUMN_CONSTRAINTS_QUANTITY + ", " +
                ProductsEntry.COLUMN_NAME_PROVIDERCONTACT + " " + ProductsEntry.COLUMN_TYPE_PROVIDERCONTACT + " " + ProductsEntry.COLUMN_CONSTRAINTS_PROVIDERCONTACT + ", " +
                ProductsEntry.COLUMN_NAME_PROVIDEREMAIL + " " + ProductsEntry.COLUMN_TYPE_PROVIDEREMAIL + " " + ProductsEntry.COLUMN_CONSTRAINTS_PROVIDEREMAIL + ")";

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
