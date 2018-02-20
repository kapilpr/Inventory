package com.example.shruthi.inventory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shruthi.inventory.data.InventoryContract.InventoryEntry;

import java.io.FileDescriptor;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 0;
    private Uri mUri;
    private String mStringUri;
    private Bitmap mBitmap;

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierEditText;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mImageView = (ImageView) findViewById(R.id.image_view_editor_activity);
        Button choosePictureButton = (Button) findViewById(R.id.button_choose_picture);
        choosePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prompt the user to choose a picture from the file system
                openImageSelector();
            }
        });
    }

    private void openImageSelector() {

        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mUri = resultData.getData();
                mBitmap = getBitmapFromUri(mUri);
                mImageView.setImageBitmap(mBitmap);
                // Uri string to be passed on to the database
                mStringUri = mUri.toString();
            }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // Save the product into the database
                saveProduct();
                return true;

            case android.R.id.home:
                if (!productEditorHasChanged()) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the product editor hasn't changed, continue with handling back button press
        if (!productEditorHasChanged()) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void saveProduct() {
        // Input Validation
        if (isInputValid()) {

            String productName = mNameEditText.getText().toString().trim();
            int productPrice = Integer.parseInt(mPriceEditText.getText().toString());
            int productQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
            String productSupplier = mSupplierEditText.getText().toString().trim();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPrice);
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER, productSupplier);
            values.put(InventoryEntry.COLUMN_PRODUCT_PICTURE, mStringUri);

            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, "Saving product failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Saved product successfully", Toast.LENGTH_SHORT).show();
            }
            finish();
        } else {
            Toast.makeText(this, "Fields can't be left blank", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isInputValid() {
        mNameEditText = (EditText) findViewById(R.id.edit_text_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_text_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_text_quantity);
        mSupplierEditText = (EditText) findViewById(R.id.edit_text_supplier);

        return !TextUtils.isEmpty(mNameEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mPriceEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mQuantityEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mSupplierEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mStringUri);
    }

    private boolean productEditorHasChanged() {
        mNameEditText = (EditText) findViewById(R.id.edit_text_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_text_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_text_quantity);
        mSupplierEditText = (EditText) findViewById(R.id.edit_text_supplier);

        return !TextUtils.isEmpty(mNameEditText.getText().toString().trim()) ||
                !TextUtils.isEmpty(mPriceEditText.getText().toString().trim()) ||
                !TextUtils.isEmpty(mQuantityEditText.getText().toString().trim()) ||
                !TextUtils.isEmpty(mSupplierEditText.getText().toString().trim()) ||
                !TextUtils.isEmpty(mStringUri);
    }
}
