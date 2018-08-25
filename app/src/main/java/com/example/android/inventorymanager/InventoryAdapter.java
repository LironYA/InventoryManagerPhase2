package com.example.android.inventorymanager;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorymanager.Inventory.InventoryContract.InventoryEntry;

public class InventoryAdapter extends CursorAdapter {
    public InventoryAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //Display the product name, price and quantity
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        //Buttons
        final Button sale_button = (Button) view.findViewById(R.id.button_sale);
        // Find the columns of item attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
        int productIdIndex = cursor.getColumnIndex(InventoryEntry._ID);
        // Read the item attributes from the Cursor for the current item
        final String productId = cursor.getString(productIdIndex);
        String itemName = cursor.getString(nameColumnIndex);
        final double price = cursor.getDouble(priceColumnIndex);
        final int itemQuantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current item
        nameTextView.setText("Item name: " + itemName);
        priceTextView.setText("Item price: " + price);
        quantityTextView.setText(("Item quantity: " + Integer.toString(itemQuantity)));
        sale_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity;
                if(itemQuantity > 0 ) {
                    currentQuantity = itemQuantity - 1;
                    quantityTextView.setText(String.valueOf(currentQuantity));
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_QUANTITY, currentQuantity);
                Uri updateQuantity = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, Integer.valueOf(productId));
                // update database
                int rowsUpdated = context.getContentResolver().update(updateQuantity,
                        values, null, null);
            }
            }
        });
    }
}
