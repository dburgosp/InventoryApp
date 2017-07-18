package com.example.android.inventoryapp.data;

/**
 * Created by David on 18/07/2017.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    // Name for the content provider.
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    // Base content URI to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Available paths, appended to base content URI for possible URI's. In this project we are
    // considering a very simple inventory database with a single table, "products", so it will be
    // only available the path "content://om.example.android.inventoryapp/products" for accessing
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
     * image (INTEGER NOT NULL DEFAULT 1) is the index of the image, relative to a drawable resource.
     * price (INTEGER NOT NULL DEFAULT 0) is the price of the product, stored in cents of euro.
     * current_quantity (INTEGER NOT NULL DEFAULT 0) is the current amount of units of the product in the inventory.
     * provider_name (TEXT NOT NULL) is the name of the provider of the product.
     * provider_email (TEXT NOT NULL) is the email address of the provider of the product.
     */
    public static final class ProductsEntry implements BaseColumns {

        // Content URI to access the product data in the provider.
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
        public final static String COLUMN_NAME_PROVIDERCONTACT = "provider_name";
        public final static String COLUMN_NAME_PROVIDEREMAIL = "provider_address";

        // Data types of the columns.
        public final static String COLUMN_TYPE_ID = "INTEGER";
        public final static String COLUMN_TYPE_PRODUCT = "TEXT";
        public final static String COLUMN_TYPE_DESCRIPTION = "TEXT";
        public final static String COLUMN_TYPE_IMAGE = "INTEGER";
        public final static String COLUMN_TYPE_PRICE = "INTEGER";
        public final static String COLUMN_TYPE_QUANTITY = "INTEGER";
        public final static String COLUMN_TYPE_PROVIDERCONTACT = "TEXT";
        public final static String COLUMN_TYPE_PROVIDEREMAIL = "TEXT";

        // Constraints of the columns.
        public final static String COLUMN_CONSTRAINTS_ID = "PRIMARY KEY AUTOINCREMENT";
        public final static String COLUMN_CONSTRAINTS_PRODUCT = "NOT NULL";
        public final static String COLUMN_CONSTRAINTS_DESCRIPTION = "";
        public final static String COLUMN_CONSTRAINTS_IMAGE = "NOT NULL DEFAULT 1";
        public final static String COLUMN_CONSTRAINTS_PRICE = "NOT NULL DEFAULT 0";
        public final static String COLUMN_CONSTRAINTS_QUANTITY = "NOT NULL DEFAULT 0";
        public final static String COLUMN_CONSTRAINTS_PROVIDERCONTACT = "NOT NULL";
        public final static String COLUMN_CONSTRAINTS_PROVIDEREMAIL = "NOT NULL";

        // Possible image types for the tourist product.
        public static final int IMAGE_TYPE_HOTELS = 1;
        public static final int IMAGE_TYPE_NIGHT = 2;
        public static final int IMAGE_TYPE_SHOPPING = 3;
        public static final int IMAGE_TYPE_VISITS = 4;
        public static final int IMAGE_TYPE_SHOWS = 5;
        public static final int IMAGE_TYPE_RESTAURANTS = 6;
        public static final int IMAGE_TYPE_LEISURE = 7;
        public static final int IMAGE_TYPE_TRANSPORT = 8;
        public static final int IMAGE_TYPE_CULTURE = 9;

        /**
         * Determines whether an image type is valid or not.
         *
         * @param imageType is the type of the image.
         * @return true if the image type represents a valid tourist product, false otherwise.
         */
        public static boolean isValidImage(int imageType) {
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
    }
}