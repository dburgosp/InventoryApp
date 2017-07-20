package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;            // Identifier for the loader.
    // Annotate fields with @BindView and views ID for Butter Knife to find and automatically cast
    // the corresponding views.
    @BindView(R.id.product_name_edit_text)
    EditText productNameEditText;
    @BindView(R.id.product_price_edit_text)
    EditText productPriceEditText;
    @BindView(R.id.product_description_edit_text)
    EditText productDescriptionEditText;
    @BindView(R.id.product_image_spinner)
    Spinner productImageSpinner;
    @BindView(R.id.product_supplier_edit_text)
    EditText supplierNameEditText;
    @BindView(R.id.product_email_supplier_edit_text)
    EditText supplierEmailEditText;
    @BindView(R.id.product_image)
    ImageView productImageView;
    @BindView(R.id.product_stock_layout)
    LinearLayout productStockLayout;
    @BindView(R.id.product_supplier_order_layout)
    LinearLayout productSupplierOrderLayout;
    private Uri currentProductUri = null;                     // URI of the current product, if exists one.
    private boolean unsavedChanges = false;                   // true if we are editing or creating a product.
    private String imageType = ProductEntry.IMAGE_TYPE_NONE;  // Current image type selected on spinner.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        ButterKnife.bind(this);

        getOperationType();     // Determine if we are trying to insert or to update a product.
        setOnTouchListeners();  // Setup OnTouchListeners to determine if there is unsaved data.
        setImageSpinner();      // Setup image selection spinner.
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
        inflater.inflate(R.menu.product_menu, menu);
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
            case R.id.product_menu_save:    // Save current product.
                if (saveProduct()) finish();
                return true;

            case R.id.product_menu_delete:  // Delete current product.
                //showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:         // "Back" arrow on the action bar has been clicked.
                if (!unsavedChanges) {
                    // There are no unsaved changes. Go back.
                    NavUtils.navigateUpFromSameTask(ProductActivity.this);
                } else {
                    // There are unsaved changes. Show a dialog to warn the user.
                    warnForUnsavedChanges(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Discard changes and navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(ProductActivity.this);
                        }
                    });
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the activity has detected the user's press of the back key.
     */
    @Override
    public void onBackPressed() {
        if (!unsavedChanges) {
            // There are no unsaved changes. Go back.
            super.onBackPressed();
        } else {
            // There are unsaved changes. Show a dialog to warn the user.
            warnForUnsavedChanges(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Discard changes and close this activity.
                    finish();
                }
            });
        }
    }

    /**
     * Prepare the Screen's standard options menu to be displayed.
     *
     * @param menu is the options menu as last shown or first initialized by onCreateOptionsMenu().
     * @return true for the menu to be displayed; if we return false it will not be shown.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If we are creating a new product, hide the "Delete" menu item.
        MenuItem menuItem = menu.findItem(R.id.product_menu_delete);
        if (currentProductUri == null) menuItem.setVisible(false);
        else menuItem.setVisible(true);

        // Return true to display the menu.
        return true;
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
        return null;
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
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
    }

    /**
     * Helper method to determine if we are inserting a new product or updating an existing one.
     */
    private void getOperationType() {
        Intent intent = getIntent();
        currentProductUri = intent.getData();
        if (currentProductUri == null) {
            // Data is null, so there's no URI to edit a product. We are inserting a new one.
            setTitle(getString(R.string.product_activity_insert_title));
            productStockLayout.setVisibility(View.GONE);
            productSupplierOrderLayout.setVisibility(View.GONE);
        } else {
            // The intent was created when clicking a list item, so we are editing an exiting
            // product.
            setTitle(getString(R.string.product_activity_update_title));
            productStockLayout.setVisibility(View.VISIBLE);
            productSupplierOrderLayout.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
        }
    }

    /**
     * Helper method to setup OnTouchListeners on all the input fields, in order to determine if
     * there is currently unsaved data.
     */
    private void setOnTouchListeners() {
        // Create an OnTouchListener for setting unsavedChanges to true.
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            /**
             * Called when a touch event is dispatched to a view. This allows listeners to get a chance
             * to respond before the target view.
             *
             * @param view        is the view the touch event has been dispatched to.
             * @param motionEvent is the MotionEvent object containing full information about the event.
             * @return true if the listener has consumed the event, false otherwise.
             */
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                unsavedChanges = true;
                return false;
            }
        };

        // Setup OnTouchListeners on all the input fields.
        productNameEditText.setOnTouchListener(onTouchListener);
        productPriceEditText.setOnTouchListener(onTouchListener);
        productDescriptionEditText.setOnTouchListener(onTouchListener);
        productImageSpinner.setOnTouchListener(onTouchListener);
        supplierNameEditText.setOnTouchListener(onTouchListener);
        supplierEmailEditText.setOnTouchListener(onTouchListener);
    }

    private void setImageSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout, specify the
        // layout to use when the list of choices appears and apply the adapter to the spinner.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.array_image_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productImageSpinner.setAdapter(adapter);

        // Set the integer mSelected to the constant values
        productImageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Callback method to be invoked when an item in this view has been selected.
             *
             * @param parent is the AdapterView where the selection happened.
             * @param view is the view within the AdapterView that was clicked.
             * @param position is the position of the view in the adapter.
             * @param id is the row id of the item that is selected.
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (TextUtils.isEmpty(selection)) {
                    productImageSpinner.setPrompt("PROMPT");
                } else {
                    if (selection.equals(getString(R.string.image_type_culture))) {
                        // The tourist product is a culture product.
                        imageType = ProductEntry.IMAGE_TYPE_CULTURE;
                        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_culture));
                    } else if (selection.equals(getString(R.string.image_type_hotels))) {
                        // The tourist product is a hotel.
                        imageType = ProductEntry.IMAGE_TYPE_HOTELS;
                        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_hotels));
                    } else if (selection.equals(getString(R.string.image_type_leisure))) {
                        // The tourist product is a leisure product.
                        imageType = ProductEntry.IMAGE_TYPE_LEISURE;
                        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_leisure));
                    } else if (selection.equals(getString(R.string.image_type_night))) {
                        // The tourist product is a night product.
                        imageType = ProductEntry.IMAGE_TYPE_NIGHT;
                        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_night));
                    } else if (selection.equals(getString(R.string.image_type_restaurants))) {
                        // The tourist product is a restaurant.
                        imageType = ProductEntry.IMAGE_TYPE_RESTAURANTS;
                        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_restaurants));
                    } else if (selection.equals(getString(R.string.image_type_shopping))) {
                        // The tourist product is a shopping product.
                        imageType = ProductEntry.IMAGE_TYPE_SHOPPING;
                        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_shopping));
                    } else if (selection.equals(getString(R.string.image_type_shows))) {
                        // The tourist product is a show.
                        imageType = ProductEntry.IMAGE_TYPE_SHOWS;
                        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_shows));
                    } else if (selection.equals(getString(R.string.image_type_transport))) {
                        // The tourist product is a transport product.
                        imageType = ProductEntry.IMAGE_TYPE_TRANSPORT;
                        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_transport));
                    } else if (selection.equals(getString(R.string.image_type_visits))) {
                        // The tourist product is a visit.
                        imageType = ProductEntry.IMAGE_TYPE_VISITS;
                        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_visits));
                    } else {
                        // Default.
                        imageType = ProductEntry.IMAGE_TYPE_NONE;
                        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_none));
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined.
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                imageType = ProductEntry.IMAGE_TYPE_NONE;
                productImageView.setImageDrawable(getDrawable(R.drawable.image_type_none));
            }
        });
    }

    /**
     * Helper method for saving the current product into the database.
     *
     * @return true if the changes have been correctly saved, false otherwise.
     */
    private boolean saveProduct() {
        // Create a ContentValues object to insert or update the product into the database.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_NAME_PRODUCT, productNameEditText.getText().toString().trim());
        values.put(ProductEntry.COLUMN_NAME_DESCRIPTION, productDescriptionEditText.getText().toString().trim());
        values.put(ProductEntry.COLUMN_NAME_IMAGE, imageType);
        values.put(ProductEntry.COLUMN_NAME_SUPPLIERCONTACT, supplierNameEditText.getText().toString().trim());
        values.put(ProductEntry.COLUMN_NAME_SUPPLIEREMAIL, supplierEmailEditText.getText().toString().trim());
        String productPrice = productPriceEditText.getText().toString().trim();
        if (TextUtils.isEmpty(productPrice))
            // Set price to 0 if it is empty.
            values.put(ProductEntry.COLUMN_NAME_PRICE, 0);
        else
            // Store price in cents of euro.
            values.put(ProductEntry.COLUMN_NAME_PRICE, Double.parseDouble(productPrice) * 100);

        // Check data from given ContentValues object.
        int res = ProductEntry.checkContentValues(values);
        switch (res) {
            case ProductEntry.SQL_ERROR_PRODUCT_NAME: // Product name is missing.
                Toast.makeText(this, getString(R.string.toast_missing_product_name), Toast.LENGTH_SHORT).show();
                productNameEditText.requestFocus();
                return false;

            case ProductEntry.SQL_ERROR_PRODUCT_IMAGE: // Product image is missing.
                Toast.makeText(this, getString(R.string.toast_missing_product_image), Toast.LENGTH_SHORT).show();
                productImageSpinner.requestFocus();
                return false;

            case ProductEntry.SQL_ERROR_PRODUCT_IMAGE_INVALID: // Product image is not valid.
                Toast.makeText(this, getString(R.string.toast_invalid_product_image), Toast.LENGTH_SHORT).show();
                productImageSpinner.requestFocus();
                return false;

            case ProductEntry.SQL_ERROR_PRODUCT_PRICE: // Product price is missing.
                Toast.makeText(this, getString(R.string.toast_missing_product_price), Toast.LENGTH_SHORT).show();
                productPriceEditText.requestFocus();
                return false;

            case ProductEntry.SQL_ERROR_SUPPLIER_NAME: // Supplier name is missing.
                Toast.makeText(this, getString(R.string.toast_missing_supplier_name), Toast.LENGTH_SHORT).show();
                supplierNameEditText.requestFocus();
                return false;

            case ProductEntry.SQL_ERROR_SUPPLIER_EMAIL: // Supplier email is missing.
                Toast.makeText(this, getString(R.string.toast_missing_supplier_email), Toast.LENGTH_SHORT).show();
                supplierEmailEditText.requestFocus();
                return false;
        }

        // Determine whether we are inserting a new product or editing an existing one.
        if (currentProductUri == null) {
            // We are adding a new product.
            Uri newProductURI = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newProductURI == null) {
                // Insertion failed.
                Toast.makeText(this, getString(R.string.toast_product_insertion_error), Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Insertion ok.
                Toast.makeText(this, getString(R.string.toast_product_insertion_ok) + ": " + newProductURI, Toast.LENGTH_LONG).show();
                return true;
            }
        } else {
            // We are updating an existing product.
            int rows = getContentResolver().update(currentProductUri, values, null, null);
            if (rows == 0) {
                // Updating error.
                Toast.makeText(this, getString(R.string.toast_product_update_error), Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Updating ok.
                Toast.makeText(this, getString(R.string.toast_product_update_ok), Toast.LENGTH_SHORT).show();
                return true;
            }
        }
    }

    /**
     * Helper method to display an alert dialog to warn the user about unsaved changes.
     *
     * @param positiveButtonClickListener is the ClickListener triggered when positive button is
     *                                    clicked on the alert dialog.
     */
    private void warnForUnsavedChanges(DialogInterface.OnClickListener positiveButtonClickListener) {
        // Show a dialog that notifies the user they have unsaved changes.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_unsaved_changes);
        builder.setPositiveButton(R.string.dialog_unsaved_changes_positive, positiveButtonClickListener);
        builder.setNegativeButton(R.string.dialog_unsaved_changes_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Close this dialog and keep on editing.
                if (dialog != null) dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
