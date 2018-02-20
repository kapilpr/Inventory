package com.example.shruthi.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shruthi.inventory.data.InventoryContract.InventoryEntry;

import java.io.FileDescriptor;
import java.io.IOException;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentProductUri;
    private EditText mBulkAddEditText;
    private EditText mBulkSoldEditText;
    private EditText mShipmentEditText;
    private TextView mNameTextView;
    private TextView mPriceTextView;
    private TextView mQuantityTextView;
    private TextView mSupplierTextView;
    private ImageView mImageView;

    private static final int EXISTING_PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Kick off the loader manager
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
    }

    // On click action for quantity plus and minus buttons
    public void quantityButtonOnClick(View view) {

        TextView currentQuantity = (TextView) findViewById(R.id.text_quantity_details_activity);
        String currentQuantityString = currentQuantity.getText().toString();
        int currentQuantityInteger = Integer.parseInt(currentQuantityString);
        int viewId = view.getId();
        switch (viewId) {
            case R.id.button_minus:
                if (currentQuantityInteger != 0) {
                    currentQuantityInteger--;
                }
                break;
            case R.id.button_plus:
                currentQuantityInteger++;
                break;
        }

        currentQuantity.setText(Integer.toString(currentQuantityInteger));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ((item.getItemId())) {
            case R.id.action_save_details:
                //Update the product info when done button is clicked
                updateProductInfo();
                break;
            case R.id.action_delete:
                showDeleteConfirmation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_product_warning);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, "Delete Unsuccessful", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Product Deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateProductInfo() {
        mBulkAddEditText = (EditText) findViewById(R.id.edit_text_bulk_add_details_activity);
        mBulkSoldEditText = (EditText) findViewById(R.id.edit_text_bulk_sold_details_activity);
        mShipmentEditText = (EditText) findViewById(R.id.edit_text_shipment_details_activity);
        mQuantityTextView = (TextView) findViewById(R.id.text_quantity_details_activity);

        int currentQuantity = Integer.parseInt(mQuantityTextView.getText().toString());

        int bulkAddAmount = 0;
        int bulkSaleAmount = 0;
        int shipmentReceived = 0;

        // Get the values entered at the edit text views
        if (mBulkAddEditText.getText().toString().trim().length() != 0) {
            bulkAddAmount = Integer.parseInt(mBulkAddEditText.getText().toString().trim());
        }

        if (mBulkSoldEditText.getText().toString().trim().length() != 0) {
            bulkSaleAmount = Integer.parseInt(mBulkSoldEditText.getText().toString().trim());
        }

        if (mShipmentEditText.getText().toString().trim().length() != 0) {
            shipmentReceived = Integer.parseInt(mShipmentEditText.getText().toString().trim());
        }

        int updatedAddedQuantity = currentQuantity + bulkAddAmount + shipmentReceived;
        // Bulk Sale quantity should not exceed the in-stock quantity
        if (bulkSaleAmount > updatedAddedQuantity) {
            Toast.makeText(this, "Bulk sale can't exceed in-stock quantity", Toast.LENGTH_SHORT).show();

        } else {
            int updatedQuantity = updatedAddedQuantity - bulkSaleAmount;

            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, updatedQuantity);

            int rowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsUpdated == 0) {
                Toast.makeText(this, "Product Update Failed", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER,
                InventoryEntry.COLUMN_PRODUCT_PICTURE};
        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mNameTextView = (TextView) findViewById(R.id.text_name_details_activity);
        mPriceTextView = (TextView) findViewById(R.id.text_price_details_activity);
        mQuantityTextView = (TextView) findViewById(R.id.text_quantity_details_activity);
        mSupplierTextView = (TextView) findViewById(R.id.text_supplier_details_activity);
        mImageView = (ImageView) findViewById(R.id.image_view_details_activity);

        if (cursor.moveToFirst()) {

            // Get the index of all required columns from the cursor
            int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
            int pictureColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PICTURE);

            // Get the values from the column indexes
            int productRowID = cursor.getInt(idColumnIndex);
            final String productName = cursor.getString(nameColumnIndex);
            int productPrice = cursor.getInt(priceColumnIndex);
            int productQuantity = cursor.getInt(quantityColumnIndex);
            String productSupplier = cursor.getString(supplierColumnIndex);
            Uri productPictureUri = Uri.parse(cursor.getString(pictureColumnIndex));

            mNameTextView.setText(productName);
            mPriceTextView.setText(Integer.toString(productPrice) + getString(R.string.currency_country));
            mQuantityTextView.setText(Integer.toString(productQuantity));
            mSupplierTextView.setText(productSupplier);

            Bitmap productPictureBitmap = getBitmapFromUri(productPictureUri);
            mImageView.setImageBitmap(productPictureBitmap);

            final EditText orderMoreEditText = (EditText) findViewById(R.id.edit_text_order_details_activity);
            final String[] supplierEmail = {productSupplier};

            // Set click listener for the order more product button to send an intent to email app
            Button placeOrderButton = (Button) findViewById(R.id.button_place_order_details_activity);
            placeOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (orderMoreEditText.getText().toString().trim().length() != 0) {

                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, supplierEmail);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.order_email_subject_1)
                                + " " + orderMoreEditText.getText().toString().trim()
                                + " " + getText(R.string.order_email_subject_2) + " " + productName);
                        if (emailIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(emailIntent);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Order field can't be blank", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = null;
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            }
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
            return image;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
