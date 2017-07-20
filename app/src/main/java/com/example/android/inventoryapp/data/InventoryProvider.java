package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

/**
 * Created by David on 18/07/2017.
 */

public class InventoryProvider extends ContentProvider {

    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();  // String for logcat.

    // Create the root node of the URI tree.
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int PRODUCTS = 0;   // URI matcher code for the content URI for the products table.
    private static final int PRODUCT_ID = 1; // URI matcher code for the content URI for a single product.

    // Build up a tree of UriMatcher objects.
    static {
        // The content URI of the form "content://com.example.android.inventoryapp/products" will
        // map to the integer code PRODUCTS.
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS, PRODUCTS);

        // The content URI of the form "content://com.example.android.inventoryapp/products/#" will
        // map to the integer code PRODUCT_ID. This URI is used to provide access to ONE single row
        // of the products table.
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private InventoryDbHelper inventoryDbHelper;    // Database helper object.

    /**
     * Initialize the content provider on startup.
     *
     * @return true if the provider was successfully loaded, false otherwise.
     */
    @Override
    public boolean onCreate() {
        inventoryDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Handle query requests from clients.
     *
     * @param uri           is URI to query. This will be the full URI sent by the client; if the
     *                      client is requesting a specific record, the URI will end in a record
     *                      number that the implementation should parse and add to a WHERE or HAVING
     *                      clause, specifying that _id value. This value must never be null.
     * @param projection    is the list of columns to put into the cursor. If null all columns are
     *                      included.
     * @param selection     is a selection criteria to apply when filtering rows. If null then all
     *                      rows are included.
     * @param selectionArgs are the selection arguments. You may include ?s in selection, which will
     *                      be replaced by the values from selectionArgs, in order that they appear
     *                      in the selection. The values will be bound as Strings.
     * @param sortOrder     sets how the rows in the cursor should be sorted. If null then the
     *                      provider is free to define the sort order.
     * @return a Cursor or null.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase database = inventoryDbHelper.getReadableDatabase();
        switch (uriMatcher.match(uri)) {
            case PRODUCTS:
                // Query the products table to produce a Cursor containing multiple rows of the
                // products table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PRODUCT_ID:
                // Query the products table selecting a single product. The _ID for that product is
                // taken from selectionArgs.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                // Default return value for this method is null.
                Log.e(LOG_TAG, "Query not supported for " + uri);
                return null;
        }

        // Set notification URI on the Cursor, so we know what content URI the Cursor was created
        // for. If the data at this URI changes, then we know we need to update the Cursor.
        try {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (java.lang.NullPointerException e) {
            // getContentResolver() has thrown an exception.
            Log.e(LOG_TAG, "Error in getContentResolver(): " + e);
        } finally {
            // Return the cursor.
            return cursor;
        }
    }

    /**
     * Handle requests to insert a new row in the database.
     *
     * @param uri           is the content:// URI of the insertion request. This must not be null.
     * @param contentValues is the set of column_name/value pairs to add to the database. This must
     *                      not be null.
     * @return the URI for the newly inserted item. This value may be null.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        long itemId;
        switch (uriMatcher.match(uri)) {
            case PRODUCTS:
                // Insert the new product and get the id for the newly inserted item.
                SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
                itemId = database.insert(ProductEntry.TABLE_NAME, null, contentValues);
                if (itemId == -1) {
                    // Error inserting the new product.
                    Log.e(LOG_TAG, "Error inserting new product: " + uri);
                    return null;
                }
                break;

            default:
                // Default return value for this method is null.
                Log.e(LOG_TAG, "Insertion not supported for " + uri);
                return null;
        }

        // Notify all listeners that the data has changed for the product content URI and return the
        // new URI for the new product inserted into the database.
        try {
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (java.lang.NullPointerException e) {
            // getContentResolver() has thrown an exception.
            Log.e(LOG_TAG, "Error in getContentResolver(): " + e);
        } finally {
            // Return the URI for the newly inserted item.
            return ContentUris.withAppendedId(uri, itemId);
        }
    }

    /**
     * Handle requests to update one or more rows.
     *
     * @param uri           is the URI to query. This can potentially have a record ID if this is an
     *                      update request for a specific record. This value must never be null.
     * @param contentValues is a set of column_name/value pairs to update in the database.
     * @param selection     is an optional filter to match rows to update.
     * @param selectionArgs are the arguments for the optional selection filter.
     * @return the number of rows affected.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        switch (uriMatcher.match(uri)) {

            case PRODUCT_ID: // Update a single product. The product _ID is taken from selectionArgs.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // ... go on into case PRODUCTS.
                //break;

            case PRODUCTS: // Update a group of products.

                // Check data from given ContentValues object.
                if (contentValues.size() == 0) {
                    // There's nothing to update.
                    Log.i(LOG_TAG, "Nothing to update: " + uri);
                    return 0;
                }

                // Perform the update on the database and return the number of rows affected.
                SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
                rowsUpdated = database.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            default:
                // Default return value for this method is 0.
                Log.e(LOG_TAG, "Updating not supported for " + uri);
                return 0;
        }

        // If 1 or more rows were updated, then notify all listeners that the data at the given URI
        // has changed.
        if (rowsUpdated != 0) {
            try {
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (java.lang.NullPointerException e) {
                // getContentResolver() has thrown an exception.
                Log.e(LOG_TAG, "Error in getContentResolver(): " + e);
            } finally {
                // Return the number of rows deleted.
                return rowsUpdated;
            }
        } else return 0;
    }

    /**
     * Handle requests to delete one or more rows.
     *
     * @param uri           is The full URI to query, including a row ID (if a specific record is
     *                      requested). This value must never be null.
     * @param selection     is an optional restriction to apply to rows when deleting.
     * @param selectionArgs are the arguments for the optional selection restriction.
     * @return the number of rows affected.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case PRODUCT_ID: // Delete a single product. The product _ID is taken from selectionArgs.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // ... go on into case PRODUCTS.
                //break;

            case PRODUCTS: // Delete all rows that match the selection and selection args.
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                // Default return value for this method is 0.
                Log.e(LOG_TAG, "Deletion not supported for " + uri);
                return 0;
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI
        // has changed.
        if (rowsDeleted != 0) {
            try {
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (java.lang.NullPointerException e) {
                // getContentResolver() has thrown an exception.
                Log.e(LOG_TAG, "Error in getContentResolver(): " + e);
            } finally {
                // Return the number of rows deleted.
                return rowsDeleted;
            }
        } else return 0;
    }

    /**
     * Handle requests for the MIME type of the data at the given URI.
     *
     * @param uri is the URI to query. This value must never be null.
     * @return a MIME type string, or null if there is no type.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;

            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;

            default:
                return null;
        }
    }
}
