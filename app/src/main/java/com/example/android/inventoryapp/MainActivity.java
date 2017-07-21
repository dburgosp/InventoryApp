package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;  // Identifier for the loader.
    // Annotate fields with @BindView and views ID for Butter Knife to find and automatically cast
    // the corresponding views.
    @BindView(R.id.main_empty_database)
    RelativeLayout emptyDatabaseTextView;
    @BindView(R.id.main_list_view)
    ListView mainListView;
    InventoryCursorAdapter inventoryCursorAdapter;  // Adapter for the ListView.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setMainListView();                                              // Set the List View.
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);    // Init the loader.
    }

    /**
     * Specify the options menu for this activity.
     *
     * @param menu is the options menu in which the items are placed.
     * @return true for the menu to be displayed; if we return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in the options menu is selected.
     *
     * @param item is the menu item that was selected.
     * @return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_insert:
                // Navigate to the edit product activity, for inserting a new product.
                Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                startActivity(intent);
                return true;

            case R.id.main_menu_clear:
                // Delete all products in the database.
                warnForClearDatabase();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection to show the columns of the table "products" that we need for the
        // ListView.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME_PRODUCT,          // Name of the product.
                ProductEntry.COLUMN_NAME_IMAGE,            // Product image.
                ProductEntry.COLUMN_NAME_PRICE,            // Price of the product.
                ProductEntry.COLUMN_NAME_SUPPLIER_CONTACT, // Name of the supplier.
                ProductEntry.COLUMN_NAME_QUANTITY};        // Current units in stock.

        // Execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,       // Parent activity context.
                ProductEntry.CONTENT_URI,   // Provider content URI to query.
                projection,                 // Columns to include in the resulting Cursor.
                null,                       // No selection clause.
                null,                       // No selection arguments.
                null);                      // Default sort order.
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update inventoryCursorAdapter with this new cursor containing updated product data.
        inventoryCursorAdapter.swapCursor(data);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted.
        inventoryCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to set the List View in which the products are going to be shown.
     */
    private void setMainListView() {
        // Set the view to show when the list is empty.
        mainListView.setEmptyView(emptyDatabaseTextView);

        // Setup an Adapter to create a list item for each row of product data in the Cursor.
        inventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        mainListView.setAdapter(inventoryCursorAdapter);

        // Define behaviour of every list item.
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {


                Intent intent = new Intent(MainActivity.this, ProductActivity.class);

                // Create the URI "content://com.example.android.inventoryapp/products/id" and save
                // it into the data field of the intent.
                intent.setData(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id));

                // Navigate to the edit product activity, for editing the current product.
                startActivity(intent);
            }
        });
    }

    /**
     * Helper method to display an alert dialog to warn the user about deletion.
     */
    private void warnForClearDatabase() {
        // Show a dialog that asks user for confirmation on deletion.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_database_deletion);
        builder.setPositiveButton(R.string.dialog_database_deletion_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Deletion confirmed.
                clearDatabase();
            }
        });
        builder.setNegativeButton(R.string.dialog_database_deletion_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Deletion cancelled.
                if (dialog != null) dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Helper method to delete all the products in the database.
     */
    private void clearDatabase() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        if (rowsDeleted == 0) {
            // Deletion failed.
            Toast.makeText(this, getString(R.string.toast_database_deletion_error), Toast.LENGTH_LONG).show();
        } else {
            // Deletion ok.
            Toast.makeText(this, getString(R.string.toast_database_deletion_ok), Toast.LENGTH_LONG).show();
        }
    }
}
