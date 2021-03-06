package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import java.text.DecimalFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;              // Identifier for the loader.
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
    @BindView(R.id.product_quantity_text_view)
    TextView productQuantityTextView;
    @BindView(R.id.product_supplier_edit_text)
    EditText supplierNameEditText;
    @BindView(R.id.product_email_supplier_edit_text)
    EditText supplierEmailEditText;
    @BindView(R.id.product_image)
    ImageView productImageView;
    @BindView(R.id.product_quantity_layout)
    LinearLayout productStockLayout;
    @BindView(R.id.product_supplier_order_layout)
    LinearLayout supplierOrderLayout;
    @BindView(R.id.product_supplier_order_quantity)
    EditText supplierOrderQuantityEditText;
    @BindView(R.id.product_supplier_order_button)
    Button supplierOrderButton;
    @BindView(R.id.product_quantity_increase)
    Button quantityIncreaseButton;
    @BindView(R.id.product_quantity_decrease)
    Button quantityDecreaseButton;
    private Uri currentProductUri = null;                       // URI of the current product, if exists one.
    private boolean unsavedChanges = false;                     // true if we are editing or creating a product.
    private String imageType = ProductEntry.IMAGE_TYPE_NONE;    // Current image type selected on spinner.
    private String productName = "";                            // Name of the product.
    private String productPrice = "";                           // Unit price for the product, formatted in euros and 2 decimals.
    private int productQuantity = 0;                            // Current number of units in the database.
    private int orderQuantity = 1;                              // Number of units to order.
    private String providerName = "";                           // Provider name.
    private String providerEmail = "";                          // Provider e-mail.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        ButterKnife.bind(this);

        getOperationType();     // Determine if we are trying to insert or to update a product.
        setOnChangeListeners(); // Setup OnTouchListeners and OnKeyListeners to determine if there is unsaved data.
        setImageSpinner();      // Setup image selection spinner.
        setupButtons();         // Setup buttons from managing quantity and emailing suppliers.
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
            case R.id.product_menu_save:
                // Save current product.
                if (saveProduct()) finish();
                return true;

            case R.id.product_menu_delete:
                // Delete current product.
                warnForDeletion();
                return true;

            case android.R.id.home:
                // "Back" arrow on the action bar has been clicked.
                if (!unsavedChanges)
                    NavUtils.navigateUpFromSameTask(ProductActivity.this); // There are no unsaved changes. Go back.
                else
                    warnForUnsavedChanges(); // There are unsaved changes. Show a dialog to warn the user.
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the activity has detected the user's press of the back key.
     */
    @Override
    public void onBackPressed() {
        if (!unsavedChanges) super.onBackPressed(); // There are no unsaved changes. Go back.
        else warnForUnsavedChanges(); // There are unsaved changes. Show a dialog to warn the user.
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
        // Define a projection to show the columns of the table "products" that we need.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME_PRODUCT,                  // Name of the product.
                ProductEntry.COLUMN_NAME_IMAGE,                    // Product image.
                ProductEntry.COLUMN_NAME_PRICE,                    // Price of the product.
                ProductEntry.COLUMN_NAME_DESCRIPTION,              // Description of the product.
                ProductEntry.COLUMN_NAME_QUANTITY,                 // Current units in stock.
                ProductEntry.COLUMN_NAME_SUPPLIER_CONTACT,         // Name of the supplier.
                ProductEntry.COLUMN_NAME_SUPPLIER_EMAIL,           // E-mail of the supplier.
                ProductEntry.COLUMN_NAME_SUPPLIER_ORDER_QUANTITY}; // Quantity for orders.

        // Execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,   // Parent activity context.
                currentProductUri,      // URI for retrieving data of the current product..
                projection,             // Columns to include in the resulting Cursor.
                null,                   // No selection clause.
                null,                   // No selection arguments.
                null);                  // Default sort order.
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Move to the first row of the cursor.
        if (data.moveToFirst()) {
            // Show layouts for orders to supplier and for displaying the product quantity.
            productStockLayout.setVisibility(View.VISIBLE);
            supplierOrderLayout.setVisibility(View.VISIBLE);

            // Show product name.
            productName = data.getString(data.getColumnIndex(ProductEntry.COLUMN_NAME_PRODUCT));
            productNameEditText.setText(productName);

            // Show price for the product. Prices are stored in euro cents, but displayed as euros
            // (2 decimals).
            DecimalFormat form = new DecimalFormat("0.00");
            productPrice = form.format(data.getFloat(data.getColumnIndex(ProductEntry.COLUMN_NAME_PRICE)) / 100);
            productPriceEditText.setText(productPrice);

            // Show product description.
            productDescriptionEditText.setText(data.getString(data.getColumnIndex(ProductEntry.COLUMN_NAME_DESCRIPTION)));

            // Show image and set the correct selection in the image type spinner.
            String image = data.getString(data.getColumnIndex(ProductEntry.COLUMN_NAME_IMAGE));
            productImageView.setImageDrawable(getDrawable(getResources().getIdentifier(image, "drawable", getPackageName())));
            switch (image) {
                case ProductEntry.IMAGE_TYPE_CULTURE:
                    productImageSpinner.setSelection(1);
                    break;

                case ProductEntry.IMAGE_TYPE_HOTELS:
                    productImageSpinner.setSelection(2);
                    break;

                case ProductEntry.IMAGE_TYPE_LEISURE:
                    productImageSpinner.setSelection(3);
                    break;

                case ProductEntry.IMAGE_TYPE_NIGHT:
                    productImageSpinner.setSelection(4);
                    break;

                case ProductEntry.IMAGE_TYPE_RESTAURANTS:
                    productImageSpinner.setSelection(5);
                    break;

                case ProductEntry.IMAGE_TYPE_SHOPPING:
                    productImageSpinner.setSelection(6);
                    break;

                case ProductEntry.IMAGE_TYPE_SHOWS:
                    productImageSpinner.setSelection(7);
                    break;

                case ProductEntry.IMAGE_TYPE_TRANSPORT:
                    productImageSpinner.setSelection(8);
                    break;

                case ProductEntry.IMAGE_TYPE_VISITS:
                    productImageSpinner.setSelection(9);
                    break;

                default:
                    productImageSpinner.setSelection(0);
                    break;
            }

            // Show current quantity for the product.
            productQuantity = data.getInt(data.getColumnIndex(ProductEntry.COLUMN_NAME_QUANTITY));
            productQuantityTextView.setText(String.format(Locale.getDefault(), "%s", productQuantity));

            // Show product provider name.
            providerName = data.getString(data.getColumnIndex(ProductEntry.COLUMN_NAME_SUPPLIER_CONTACT));
            supplierNameEditText.setText(providerName);

            // Show product provider e-mail.
            providerEmail = data.getString(data.getColumnIndex(ProductEntry.COLUMN_NAME_SUPPLIER_EMAIL));
            supplierEmailEditText.setText(providerEmail);

            // Show number of units for ordering to the supplier.
            orderQuantity = data.getInt(data.getColumnIndex(ProductEntry.COLUMN_NAME_SUPPLIER_ORDER_QUANTITY));
            supplierOrderQuantityEditText.setText(String.format(Locale.getDefault(), "%s", orderQuantity));
            if (orderQuantity > 1)
                supplierOrderButton.setText(getResources().getString(R.string.supplier_order_button, orderQuantity, "(s)"));
            else
                supplierOrderButton.setText(getResources().getString(R.string.supplier_order_button, 1, ""));
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Reset product name.
        productNameEditText.setText("");

        // Reset price for the product.
        productPriceEditText.setText("");

        // Reset product description.
        productDescriptionEditText.setText("");

        // Reset image and image type spinner.
        productImageView.setImageDrawable(getDrawable(R.drawable.image_type_none));
        productImageSpinner.setSelection(0);

        // Reset product provider name.
        supplierNameEditText.setText("");

        // Reset product provider e-mail.
        supplierEmailEditText.setText("");

        // Hide unwanted layout elements.
        productStockLayout.setVisibility(View.GONE);
        supplierOrderLayout.setVisibility(View.GONE);
    }

    /**
     * Helper method to determine if we are inserting a new product or updating an existing one.
     */
    private void getOperationType() {
        currentProductUri = getIntent().getData();
        if (currentProductUri == null) {
            // Data is null, so there's no URI to edit a product. We are inserting a new one.
            setTitle(getString(R.string.product_activity_insert_title));
            productStockLayout.setVisibility(View.GONE);
            supplierOrderLayout.setVisibility(View.GONE);
        } else {
            // The intent was created when clicking a list item, so we are editing an exiting
            // product.
            setTitle(getString(R.string.product_activity_update_title));
            productStockLayout.setVisibility(View.VISIBLE);
            supplierOrderLayout.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
        }
    }

    /**
     * Helper method to setup OnKeyListeners and OnTouchListeners on all the input fields, in order
     * to determine if there is currently unsaved data.
     */
    private void setOnChangeListeners() {
        // Create an OnKeyListener for setting unsavedChanges to true.
        View.OnKeyListener OnKeyListener = new View.OnKeyListener() {
            /**
             * Called when a hardware key is dispatched to a view.
             *
             * @param v       is the view the key has been dispatched to.
             * @param keyCode is the code for the physical key that was pressed.
             * @param event   is the KeyEvent object containing full information about the event.
             * @return true if the listener has consumed the event, false otherwise.
             */
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                unsavedChanges = true;
                return false;
            }
        };

        // Setup OnKeyListeners on all the input fields.
        productNameEditText.setOnKeyListener(OnKeyListener);
        productPriceEditText.setOnKeyListener(OnKeyListener);
        productDescriptionEditText.setOnKeyListener(OnKeyListener);
        supplierNameEditText.setOnKeyListener(OnKeyListener);
        supplierEmailEditText.setOnKeyListener(OnKeyListener);
        supplierOrderQuantityEditText.setOnKeyListener(new View.OnKeyListener() {
            /**
             * Called when a hardware key is dispatched to a view.
             *
             * @param v       is the view the key has been dispatched to.
             * @param keyCode is the code for the physical key that was pressed.
             * @param event   is the KeyEvent object containing full information about the event.
             * @return true if the listener has consumed the event, false otherwise.
             */
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                unsavedChanges = true;
                if (!supplierOrderQuantityEditText.getText().toString().isEmpty()) {
                    orderQuantity = Integer.parseInt(supplierOrderQuantityEditText.getText().toString());
                    if (orderQuantity <= 100) {
                        if (orderQuantity > 1)
                            supplierOrderButton.setText(getResources().getString(R.string.supplier_order_button, orderQuantity, "(s)"));
                        else {
                            // Order quantity must be at least 1.
                            orderQuantity = 1;
                            supplierOrderQuantityEditText.setText(String.valueOf(orderQuantity));
                            Toast.makeText(ProductActivity.this, getString(R.string.toast_order_quantity_min), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Order quantity can be 100 at most.
                        orderQuantity = 100;
                        supplierOrderQuantityEditText.setText(String.valueOf(orderQuantity));
                        Toast.makeText(ProductActivity.this, getString(R.string.toast_order_quantity_max), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Order quantity can't be empty.
                    orderQuantity = 1;
                    supplierOrderQuantityEditText.setText(String.valueOf(orderQuantity));
                    Toast.makeText(ProductActivity.this, getString(R.string.toast_order_quantity_empty), Toast.LENGTH_SHORT).show();
                }

                // Set text for order button.
                if (orderQuantity == 1)
                    supplierOrderButton.setText(getResources().getString(R.string.supplier_order_button, orderQuantity, ""));
                else
                    supplierOrderButton.setText(getResources().getString(R.string.supplier_order_button, orderQuantity, "(s)"));

                return false;
            }
        });

        // Setup OnTouchListeners on the image spinner.
        productImageSpinner.setOnTouchListener(new View.OnTouchListener() {
            /**
             * Called when a touch event is dispatched to a view. This allows listeners to get a
             * chance to respond before the target view.
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
        });
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
                if (!TextUtils.isEmpty(selection)) {
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
        values.put(ProductEntry.COLUMN_NAME_SUPPLIER_CONTACT, supplierNameEditText.getText().toString().trim());
        values.put(ProductEntry.COLUMN_NAME_SUPPLIER_EMAIL, supplierEmailEditText.getText().toString().trim());

        // Set product quantity to 0 if is negative.
        if (!productQuantityTextView.getText().toString().trim().isEmpty())
            productQuantity = Integer.parseInt(productQuantityTextView.getText().toString().trim());
        if (productQuantity < 0) values.put(ProductEntry.COLUMN_NAME_QUANTITY, 0);
        else values.put(ProductEntry.COLUMN_NAME_QUANTITY, productQuantity);

        // Set order quantity to 1 if is lower than 1 or empty.
        if (!supplierOrderQuantityEditText.getText().toString().trim().isEmpty())
            orderQuantity = Integer.parseInt(supplierOrderQuantityEditText.getText().toString().trim());
        if (orderQuantity < 1) orderQuantity = 1;
        values.put(ProductEntry.COLUMN_NAME_SUPPLIER_ORDER_QUANTITY, orderQuantity);

        // Set price to 0 if it is empty. Otherwise, store price in cents of euro.
        if (!productPriceEditText.getText().toString().trim().isEmpty())
            productPrice = productPriceEditText.getText().toString().trim();
        if (TextUtils.isEmpty(productPrice)) values.put(ProductEntry.COLUMN_NAME_PRICE, 0);
        else values.put(ProductEntry.COLUMN_NAME_PRICE, Double.parseDouble(productPrice) * 100);

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

            case ProductEntry.SQL_ERROR_SUPPLIER_ORDER_QUANTITY: // Supplier order quantity is missing.
                Toast.makeText(this, getString(R.string.toast_missing_supplier_order_quantity), Toast.LENGTH_SHORT).show();
                supplierOrderQuantityEditText.requestFocus();
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
                Toast.makeText(this, getString(R.string.toast_product_insertion_ok) + ": \n\n" + newProductURI, Toast.LENGTH_LONG).show();
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
     */
    private void warnForUnsavedChanges() {
        // Show a dialog that notifies the user they have unsaved changes.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_unsaved_changes);
        builder.setPositiveButton(R.string.dialog_unsaved_changes_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Discard changes and close this activity.
                finish();
            }
        });
        builder.setNegativeButton(R.string.dialog_unsaved_changes_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Close this dialog and keep on editing.
                if (dialog != null) dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Helper method to display an alert dialog to warn the user about deletion.
     */
    private void warnForDeletion() {
        // Show a dialog that asks user for confirmation on deletion.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_product_deletion);
        builder.setPositiveButton(R.string.dialog_product_deletion_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Deletion confirmed.
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(R.string.dialog_product_deletion_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Deletion cancelled.
                if (dialog != null) dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Helper method to delete the current product.
     */
    private void deleteProduct() {
        if (currentProductUri != null) {
            // Ask the ContentResolver for deleting the current product.
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0) {
                // Deletion failed
                Toast.makeText(this, getString(R.string.toast_product_deletion_error), Toast.LENGTH_SHORT).show();
            } else {
                // Deletion ok.
                Toast.makeText(this, getString(R.string.toast_product_deletion_ok), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Helper method to define the behaviour of every button in activity_product.xml.
     */
    private void setupButtons() {
        // Order button.
        supplierOrderButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String[] email = {providerEmail};
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, email);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.email_text, providerName, productName, productPrice, orderQuantity));
                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "Email "));
            }
        });

        // Increase quantity button.
        quantityIncreaseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Increase quantity and update product into database.
                productQuantityTextView.setText(String.format(Locale.getDefault(), "%s", ++productQuantity));
                if (!saveProduct())
                    // If updating has failed, restore productQuantity.
                    if (productQuantity > 0) {
                        productQuantityTextView.setText(String.format(Locale.getDefault(), "%s", --productQuantity));
                    }
            }
        });

        // Decrease quantity button.
        quantityDecreaseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (productQuantity > 0) {
                    // Decrease quantity and update product into database.
                    productQuantityTextView.setText(String.format(Locale.getDefault(), "%s", --productQuantity));
                    if (!saveProduct()) {
                        // If updating has failed, restore productQuantity.
                        productQuantityTextView.setText(String.format(Locale.getDefault(), "%s", ++productQuantity));
                    }
                }
            }
        });
    }
}
