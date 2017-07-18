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

import com.example.android.inventoryapp.data.InventoryContract.ProductsEntry;

/**
 * Created by David on 18/07/2017.
 */

public class InventoryProvider extends ContentProvider {

    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int PRODUCTS = 0;          // URI matcher code for the content URI for the products table.
    private static final int PRODUCT_ID = 1;        // URI matcher code for the content URI for a single product.
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.products/products" will map to the
        // integer code {@link #PRODUCTS}. This URI is used to provide access to MULTIPLE rows
        // of the products table.
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS, PRODUCTS);

        // The content URI of the form "content://com.example.android.products/products/#" will map to the
        // integer code {@link #PRODUCT_ID}. This URI is used to provide access to ONE single row
        // of the products table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.products/products/3" matches, but
        // "content://com.example.android.products/products" (without a number at the end) doesn't match.
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

        // Figure out if the URI matcher can match the URI to a specific code
        SQLiteDatabase database = inventoryDbHelper.getReadableDatabase();
        int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PRODUCTS code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                cursor = database.query(ProductsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.products/products/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the products table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                return null;
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
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
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
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
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);

            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);

            default:
                return 0;
        }
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
        // Get writable database
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Handle requests for the MIME type of the data at the given URI.
     *
     * @param uri is the URI to query. This value must never be null.
     * @return a MIME type string, or null if there is no type.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductsEntry.CONTENT_LIST_TYPE;

            case PRODUCT_ID:
                return ProductsEntry.CONTENT_ITEM_TYPE;

            default:
                return null;
        }
    }

    /**
     * Helper method to check whether the product name is present or not.
     *
     * @param values is the set of column_name/value pairs to add to the database.
     * @return true if product name is present in values.
     * @throws IllegalArgumentException
     */
    private boolean checkProductName(ContentValues values) throws IllegalArgumentException {
        boolean ret = false;
        String name = values.getAsString(ProductsEntry.COLUMN_NAME_PRODUCT);
        if (name == null) throw new IllegalArgumentException("Product name is mandatory");
        else ret = true;
        return ret;
    }

    /**
     * Helper method to check whether the product image is present and valid or not.
     *
     * @param values is the set of column_name/value pairs to add to the database.
     * @return true if product image is present in values and valid.
     * @throws IllegalArgumentException
     */
    private boolean checkProductImage(ContentValues values) throws IllegalArgumentException {
        boolean ret = false;
        Integer image = values.getAsInteger(ProductsEntry.COLUMN_NAME_IMAGE);
        if (image == null) throw new IllegalArgumentException("Product image is mandatory");
        else if (!ProductsEntry.isValidImage(image))
            throw new IllegalArgumentException("Product image is not valid");
        else ret = true;
        return ret;
    }

    /**
     * Helper method to check whether the provider name is present or not.
     *
     * @param values is the set of column_name/value pairs to add to the database.
     * @return true if provider name is present in values.
     * @throws IllegalArgumentException
     */
    private boolean checkProductProvider(ContentValues values) throws IllegalArgumentException {
        boolean ret = false;
        String name = values.getAsString(ProductsEntry.COLUMN_NAME_PROVIDERCONTACT);
        if (name == null) throw new IllegalArgumentException("Provider name is mandatory");
        else ret = true;
        return ret;
    }

    /**
     * Insert a product into the database with the given content values.
     *
     * @param uri    is the URI for inserting a new product.
     * @param values are the values for the insertion.
     * @return the new content URI for the new product, or null if there has been any problem.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        // Check data from given ContentValues object.
        if (!checkProductName(values) || !checkProductImage(values) || !checkProductProvider(values))
            return null;

        // Insert the new product.
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        long id = database.insert(ProductsEntry.TABLE_NAME, null, values);
        if (id == -1) {
            // Error inserting the new product.
            Log.e(LOG_TAG, "Error inserting new product: " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the product content URI and return the
        // new URI for the new product inserted into the database.
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     *
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check data from given ContentValues object.
        if (values.containsKey(ProductsEntry.COLUMN_NAME_PRODUCT))
            if (!checkProductName(values)) return 0;
        if (values.containsKey(ProductsEntry.COLUMN_NAME_PROVIDERCONTACT))
            if (!checkProductProvider(values)) return 0;
        if (values.containsKey(ProductsEntry.COLUMN_NAME_IMAGE))
            if (!checkProductImage(values)) return 0;
        if (values.size() == 0) return 0;

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ProductsEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
