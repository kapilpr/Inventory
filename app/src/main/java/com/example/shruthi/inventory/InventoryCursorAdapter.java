package com.example.shruthi.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shruthi.inventory.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        // Get the index of all required columns from the cursor
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        // Get the values from the column indexes
        int productRowID = cursor.getInt(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);

        // Set the text of the corresponding text views of the list view
        nameTextView.setText(productName);
        priceTextView.setText(Integer.toString(productPrice) + context.getString(R.string.currency_country));
        quantityTextView.setText(Integer.toString(productQuantity));

        final int nProductRowID = productRowID;
        final int nProductQuantity = productQuantity;
        final Context nContext = context;

        Button button = (Button) view.findViewById(R.id.button_sale);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //update the list item view on clicking the sale button
                updateItemList(nProductRowID, nProductQuantity, nContext);
            }
        });
    }

    private void updateItemList(int id, int quantity, Context context) {
        if (quantity != 0) {
            //Reduce the number of products by one until it reaches zero
            quantity--;

            // Update the change in the database
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

            Uri newUri = Uri.withAppendedPath(InventoryEntry.CONTENT_URI, Integer.toString(id));

            int rowsUpdated = context.getContentResolver().update(newUri, values, null, null);

            if (rowsUpdated == 0) {
                Toast.makeText(context, "Sale unsuccessful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Sale successful", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "No more items available for sale", Toast.LENGTH_SHORT).show();
        }
    }
}
