package com.example.android.inventoryapp.data;

/**
 * Created by David on 18/07/2017.
 */

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    // Name for the content supplier.
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    // Base content URI to contact the content supplier.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Available paths, appended to base content URI for possible URI's. In this project we are
    // considering a very simple inventory database with a single table, "products", so it will be
    // only available the path "content://com.example.android.inventoryapp/products" for accessing
    // elements of that table.
    public static final String PATH_PRODUCTS = "products";

    /**
     * To prevent someone from accidentally instantiating the contract class, give it an empty
     * constructor.
     */
    private InventoryContract() {
    }

    /**
     * Class for "products" table, which stores the information for every product in the inventory.
     * It has the following columns:
     * <p>
     * _id (INTEGER PRIMARY KEY AUTOINCREMENT) is the index of the table.
     * name (TEXT NOT NULL) is the name of the product.
     * description (TEXT) is the optional description of the product.
     * image (TEXT NOT NULL DEFAULT 1) is the index of the image, relative to a drawable resource.
     * price (INTEGER NOT NULL DEFAULT 0) is the price of the product, stored in cents of euro.
     * current_quantity (INTEGER NOT NULL DEFAULT 0) is the current amount of units of the product in the inventory.
     * supplier_name (TEXT NOT NULL) is the name of the supplier of the product.
     * supplier_email (TEXT NOT NULL) is the email address of the supplier of the product.
     * supplier_order_quantity (INTEGER DEFAULT 1) is the default number of units for setting orders to the product supplier..
     */
    public static final class ProductEntry implements BaseColumns {

        // Content URI to access the product data in the supplier.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // MIME type of the CONTENT_URI for a single product.
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // MIME type of the CONTENT_URI for a list of products.
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // Name of the table.
        public final static String TABLE_NAME = "products";

        // Names of the columns.
        public final static String COLUMN_NAME_ID = BaseColumns._ID;
        public final static String COLUMN_NAME_PRODUCT = "name";
        public final static String COLUMN_NAME_DESCRIPTION = "description";
        public final static String COLUMN_NAME_IMAGE = "image";
        public final static String COLUMN_NAME_PRICE = "price";
        public final static String COLUMN_NAME_QUANTITY = "current_quantity";
        public final static String COLUMN_NAME_SUPPLIER_CONTACT = "supplier_name";
        public final static String COLUMN_NAME_SUPPLIER_EMAIL = "supplier_address";
        public final static String COLUMN_NAME_SUPPLIER_ORDER_QUANTITY = "supplier_order_quantity";

        // Data types of the columns.
        public final static String COLUMN_TYPE_ID = "INTEGER";
        public final static String COLUMN_TYPE_PRODUCT = "TEXT";
        public final static String COLUMN_TYPE_DESCRIPTION = "TEXT";
        public final static String COLUMN_TYPE_IMAGE = "TEXT";
        public final static String COLUMN_TYPE_PRICE = "INTEGER";
        public final static String COLUMN_TYPE_QUANTITY = "INTEGER";
        public final static String COLUMN_TYPE_SUPPLIER_CONTACT = "TEXT";
        public final static String COLUMN_TYPE_SUPPLIER_EMAIL = "TEXT";
        public final static String COLUMN_TYPE_SUPPLIER_ORDER_QUANTITY = "INTEGER";

        // Constraints of the columns.
        public final static String COLUMN_CONSTRAINTS_ID = "PRIMARY KEY AUTOINCREMENT";
        public final static String COLUMN_CONSTRAINTS_PRODUCT = "NOT NULL";
        public final static String COLUMN_CONSTRAINTS_DESCRIPTION = "";
        public final static String COLUMN_CONSTRAINTS_IMAGE = "NOT NULL DEFAULT \"image_type_none\"";
        public final static String COLUMN_CONSTRAINTS_PRICE = "NOT NULL DEFAULT 0";
        public final static String COLUMN_CONSTRAINTS_QUANTITY = "NOT NULL DEFAULT 0";
        public final static String COLUMN_CONSTRAINTS_SUPPLIER_CONTACT = "NOT NULL";
        public final static String COLUMN_CONSTRAINTS_SUPPLIER_EMAIL = "NOT NULL";
        public final static String COLUMN_CONSTRAINTS_SUPPLIER_ORDER_QUANTITY = "DEFAULT 1";

        // Possible image types for the tourist product.
        public static final String IMAGE_TYPE_NONE = "image_type_none";
        public static final String IMAGE_TYPE_HOTELS = "image_type_hotels";
        public static final String IMAGE_TYPE_NIGHT = "image_type_night";
        public static final String IMAGE_TYPE_SHOPPING = "image_type_shopping";
        public static final String IMAGE_TYPE_VISITS = "image_type_visits";
        public static final String IMAGE_TYPE_SHOWS = "image_type_shows";
        public static final String IMAGE_TYPE_RESTAURANTS = "image_type_restaurants";
        public static final String IMAGE_TYPE_LEISURE = "image_type_leisure";
        public static final String IMAGE_TYPE_TRANSPORT = "image_type_transport";
        public static final String IMAGE_TYPE_CULTURE = "image_type_culture";

        // Possible results when inserting, updating or deleting this table.
        public static final int SQL_OK = 1;
        public static final int SQL_ERROR_PRODUCT_NAME = -1;
        public static final int SQL_ERROR_PRODUCT_IMAGE = -2;
        public static final int SQL_ERROR_PRODUCT_IMAGE_INVALID = -3;
        public static final int SQL_ERROR_PRODUCT_PRICE = -4;
        public static final int SQL_ERROR_SUPPLIER_NAME = -5;
        public static final int SQL_ERROR_SUPPLIER_EMAIL = -6;
        public static final int SQL_ERROR_SUPPLIER_ORDER_QUANTITY = -7;

        /**
         * Determines whether an image type is valid or not.
         *
         * @param imageType is the type of the image.
         * @return true if the image type represents a valid tourist product, false otherwise.
         */
        public static boolean isValidImage(String imageType) {
            switch (imageType) {
                case IMAGE_TYPE_HOTELS:
                case IMAGE_TYPE_NIGHT:
                case IMAGE_TYPE_SHOPPING:
                case IMAGE_TYPE_VISITS:
                case IMAGE_TYPE_SHOWS:
                case IMAGE_TYPE_RESTAURANTS:
                case IMAGE_TYPE_LEISURE:
                case IMAGE_TYPE_TRANSPORT:
                case IMAGE_TYPE_CULTURE:
                    // Valid tourist product types. Return true.
                    return true;

                default:
                    // Return false otherwise.
                    return false;
            }
        }

        /**
         * Helper method to check whether all the required content values are present or not.
         *
         * @param contentValues is the set of column_name/value pairs to add to the database.
         * @return SQL_OK if all the required content values are present and valid
         * SQL_ERROR_PRODUCT_NAME if the product name is missing.
         * SQL_ERROR_PRODUCT_IMAGE if the product image is missing.
         * SQL_ERROR_PRODUCT_IMAGE_INVALID if the product image is not valid.
         * SQL_ERROR_PRODUCT_PRICE if the product price is missing.
         * SQL_ERROR_SUPPLIER_NAME if the supplier name is missing.
         * SQL_ERROR_SUPPLIER_EMAIL if the supplier email is missing.
         * SQL_ERROR_SUPPLIER_ORDER_QUANTITY if the supplier email is missing.
         */
        public static int checkContentValues(ContentValues contentValues) {
            String data;

            // Check product name.
            if (contentValues.containsKey(COLUMN_NAME_PRODUCT)) {
                // Column name COLUMN_NAME_PRODUCT is always present when inserting a new product, but
                // may be not present when updating an existing product.
                data = contentValues.getAsString(COLUMN_NAME_PRODUCT);
                if (data.isEmpty()) {
                    // If present, product name must not be empty.
                    return SQL_ERROR_PRODUCT_NAME;
                }
            }

            // Check product price.
            if (contentValues.containsKey(COLUMN_NAME_PRICE)) {
                // Column name COLUMN_NAME_PRICE is always present when inserting a new product, but may
                // be not present when updating an existing product.
                data = contentValues.getAsString(COLUMN_NAME_PRICE);
                if (data.isEmpty() || (data.equals("0"))) {
                    // If present, product price must not be empty or zero.
                    return SQL_ERROR_PRODUCT_PRICE;
                }
            }

            // Check image.
            if (contentValues.containsKey(COLUMN_NAME_IMAGE)) {
                // Column name COLUMN_NAME_IMAGE is always present when inserting a new product, but may
                // be not present when updating an existing product.
                data = contentValues.getAsString(COLUMN_NAME_IMAGE);
                if (data.isEmpty()) {
                    // If present, image must not be empty.
                    return SQL_ERROR_PRODUCT_IMAGE;
                } else if (!isValidImage(data)) {
                    // If present and has no null value, image must be valid.
                    return SQL_ERROR_PRODUCT_IMAGE_INVALID;
                }
            }

            // Check supplier name.
            if (contentValues.containsKey(COLUMN_NAME_SUPPLIER_CONTACT)) {
                // Column name COLUMN_NAME_SUPPLIER_CONTACT is always present when inserting a new
                // product, but may be not present when updating an existing product.
                data = contentValues.getAsString(COLUMN_NAME_SUPPLIER_CONTACT);
                if (data.isEmpty()) {
                    // If present, supplier name must not be empty.
                    return SQL_ERROR_SUPPLIER_NAME;
                }
            }

            // Check supplier e-mail.
            if (contentValues.containsKey(COLUMN_NAME_SUPPLIER_EMAIL)) {
                // Column name COLUMN_NAME_SUPPLIER_EMAIL is always present when inserting a new
                // product, but may be not present when updating an existing product.
                data = contentValues.getAsString(COLUMN_NAME_SUPPLIER_EMAIL);
                if (data.isEmpty()) {
                    // If present, supplier email must not be empty.
                    return SQL_ERROR_SUPPLIER_EMAIL;
                }
            }

            // Check number of units for new orders to supplier.
            if (contentValues.containsKey(COLUMN_NAME_SUPPLIER_ORDER_QUANTITY)) {
                // Column name COLUMN_NAME_SUPPLIER_ORDER_QUANTITY is always present when inserting a
                // new product, but may be not present when updating an existing product.
                data = contentValues.getAsString(COLUMN_NAME_SUPPLIER_ORDER_QUANTITY);
                if (data.isEmpty()) {
                    // If present, number of units for new orders to supplier must not be empty.
                    return SQL_ERROR_SUPPLIER_ORDER_QUANTITY;
                }
            }

            // Default value.
            return SQL_OK;
        }
    }
}