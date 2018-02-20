package com.example.shruthi.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.shruthi.inventory.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    public InventoryDbHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the products table
     */
    private static final int PRODUCTS = 100;

    /**
     * URI matcher code for the content URI for a single product in the products table
     */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static Initializer
    static {

        //Add uri for the complete products table
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS, PRODUCTS);

        // Add uri for a single product on the products table
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS + "/#", PRODUCT_ID);

    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                // For the PRODUCTS code, query the PRODUCTS table directly with the given
                // projection, selection, selection arguments, and sort order.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // The selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the products table with the id and return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor, so that we know what content URI the cursor was created for.
        // If the data at this URI changes, then we know we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {

        // Check name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check price is not null
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Product requires a valid price");
        }

        // If the quantity is provided, check that it's greater than 0
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product requires a valid quantity");
        }

        // Check supplier is not null
        String supplier = values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplier == null) {
            throw new IllegalArgumentException("Product requires a supplier");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);

        // If the id is -1, then log the error to show that insertion failed
        if (id == -1) {
            Log.e(LOG_TAG, "Insertion failed for row " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the inventory content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);

    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);

            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the InventoryEntry #COLUMN_PRODUCT_NAME key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // If the InventoryEntry #COLUMN_PRODUCT_PRICE key is present,
        // check that the price value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Product requires a valid price");
            }
        }

        // If the InventoryEntry #COLUMN_PRODUCT_QUANTITY key is present,
        // check that the quantity value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0.
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        // If the InventoryEntry #COLUMN_PRODUCT_SUPPLIER key is present,
        // check that the supplier value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_SUPPLIER)) {
            String supplier = values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Product requires a supplier");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Get the number of database rows affected by the update statement
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If the number of rows updated is 1 or more than 1, notify all listeners that the data
        // at the given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        int rowsDeleted;
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args.
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If the number of rows updated is 1 or more than 1, notify all listeners that the data
        // at the given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }
}
