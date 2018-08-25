package com.example.android.inventorymanager.Inventory;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

//Class is final because it's just a class to providing constants and there no need to import or extend
public final class InventoryContract {
    //To prevent someone from instantiating the contract class, empty constructor is given
    private InventoryContract() {
    }

    //The "Content authority" is a name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.inventorymanager";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    public static abstract class InventoryEntry implements BaseColumns {
        /**
         * The content URI to access the data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);
        //The MIME type of the CONTENT_URI
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        // The MIME type of the CONTENT_URI for a single row.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_PHONE = "phone";

    }
}
