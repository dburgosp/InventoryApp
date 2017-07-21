package com.example.android.inventoryapp;

/**
 * Created by David on 18/07/2017.
 */

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import java.text.DecimalFormat;

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructor for this class.
     *
     * @param context is the context.
     * @param c       is the cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context is the interface to application's global information.
     * @param cursor  is the cursor from which to get the data. The cursor is already moved to the
     *                correct position.
     * @param parent  is the parent to which the new view is attached to.
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Bind an existing view to the data pointed to by cursor.
     *
     * @param view    is the existing view, returned earlier by newView.
     * @param context is the interface to application's global information.
     * @param cursor  is the cursor from which to get the data. The cursor is already moved to the
     *                correct position.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Read the Cursor and get current product information.
        String product = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_NAME_PRODUCT));
        String image = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_NAME_IMAGE));
        String provider = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_NAME_SUPPLIER_CONTACT));
        Float price = cursor.getFloat(cursor.getColumnIndex(ProductEntry.COLUMN_NAME_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_NAME_QUANTITY));

        // Show product name.
        TextView productTextView = (TextView) view.findViewById(R.id.list_item_product);
        productTextView.setText(product);

        // Show image.
        ImageView productImageView = (ImageView) view.findViewById(R.id.list_item_image);
        productImageView.setImageDrawable(context.getDrawable(context.getResources().getIdentifier(image, "drawable", context.getPackageName())));

        // Show product provider name.
        TextView providerTextView = (TextView) view.findViewById(R.id.list_item_provider);
        providerTextView.setText(provider);

        // Show price for the product. Prices are stored in euro cents, but displayed as euros (2 decimals).
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price);
        DecimalFormat form = new DecimalFormat("0.00");
        priceTextView.setText(context.getResources().getString(R.string.list_item_price) + " " + form.format(price / 100) + " €");

        // Show current quantity for the product.
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_quantity);
        quantityTextView.setText(context.getResources().getString(R.string.list_item_units) + " " + quantity);
    }
}
