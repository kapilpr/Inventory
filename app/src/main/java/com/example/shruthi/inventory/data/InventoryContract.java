package com.example.shruthi.inventory.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract Class for the Inventory Database
 */

public class InventoryContract {

    private InventoryContract() {

    }

    public static final String CONTENT_AUTHORITY = "com.example.shruthi.inventory";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the product data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        public static final String COLUMN_PRODUCT_SUPPLIER = "supplier";
        public static final String COLUMN_PRODUCT_PICTURE = "picture";
    }
}
