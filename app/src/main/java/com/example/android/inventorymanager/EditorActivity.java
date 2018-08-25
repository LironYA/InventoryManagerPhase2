package com.example.android.inventorymanager;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventorymanager.Inventory.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Content URI for the existing item (null if it's a new item)
     */
    private Uri mCurrentUri;
    /**
     * Identifier for the item data loader
     */
    private static final int EXISTING_ITEM_LOADER = 0;
    private EditText mEditText_name;
    private EditText mEditText_price;
    private EditText mEditQuantity;
    private EditText mSupplier_name;
    private EditText mSupplier_phone;
    private Button button_increase;
    private Button button_decrease;
    private Button call_supplier;
    int quantity = 0;
    /**
     * Boolean flag that keeps track of whether the item has been edited (true) or not (false)
     */
    private boolean itemHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mEditText_name = (EditText) findViewById(R.id.editText);
        mEditText_price = (EditText) findViewById(R.id.editText_price);
        mEditQuantity = (EditText) findViewById(R.id.editQuantity);
        mSupplier_name = (EditText) findViewById(R.id.supplier_name);
        mSupplier_phone = (EditText) findViewById(R.id.supplier_phone);
        //Buttons for increasing and decreasing the quantity
        button_increase = (Button) findViewById(R.id.button_increase);
        button_decrease = (Button) findViewById(R.id.button_decrease);
        call_supplier = (Button) findViewById(R.id.button_call);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();

        mCurrentUri = intent.getData();

        // If the intent DOES NOT contain an item content URI, then we know that we are
        // creating a new item.
        if (mCurrentUri == null) {
            // This is a new item, so change the app bar to say "Add an item"
            setTitle(getString(R.string.editor_activity_title_new_item));
            mEditQuantity.setText(String.valueOf(quantity));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit item"
            setTitle(getString(R.string.editor_activity_title_edit_item));

            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mEditText_name.setOnTouchListener(mTouchListener);
        mEditText_price.setOnTouchListener(mTouchListener);
        mEditQuantity.setOnTouchListener(mTouchListener);
        mSupplier_name.setOnTouchListener(mTouchListener);
        mSupplier_phone.setOnTouchListener(mTouchListener);

        //Buttons
        button_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = mEditQuantity.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);
                if (quantityString.matches("")) {
                    return;
                }
                if (quantity > 0) {
                    int newQuantity = quantity - 1;
                    mEditQuantity.setText(String.valueOf(newQuantity));
                }
            }
        });
        button_increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = mEditQuantity.getText().toString().trim();
                if (quantityString.matches("")) {
                    return;
                }
                else {
                int quantity = Integer.parseInt(quantityString);
                int newQuantity = quantity + 1;
                    mEditQuantity.setText(String.valueOf(newQuantity));
                }}

        });
        call_supplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callSupplier(mSupplier_phone.getText().toString());
            }
        });
    }

    public void validateEditTexts(){
        if (mEditText_name.getText().toString().isEmpty() || mEditText_price.getText().toString().isEmpty() || mEditQuantity.getText().toString().isEmpty() || mSupplier_name.getText().toString().isEmpty()
                || mSupplier_phone.getText().toString().isEmpty())  {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
    }
    else {
            InsertItem();
        }
    }
    private void InsertItem() {
        // Read from input fields
        // We use trim to eliminate leading or trailing white space
        String nameString = mEditText_name.getText().toString().trim();
        String priceString = mEditText_price.getText().toString().trim();
        double price = Double.parseDouble(priceString);
        String quantityString = mEditQuantity.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String supplierNameString = mSupplier_name.getText().toString().trim();
        String supplierPhoneString = mSupplier_phone.getText().toString().trim();
        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank
        if (TextUtils.isEmpty(nameString)
                || TextUtils.isEmpty(priceString)
                || TextUtils.isEmpty(quantityString)
                || TextUtils.isEmpty(supplierNameString)
                || TextUtils.isEmpty(supplierPhoneString)) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_PHONE, supplierPhoneString);
        // Determine if this is a new or existing item by checking if mCurrentUri is null or not
        if (mCurrentUri == null) {
            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();

            }
        } else {
            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            validateEditTexts();
            finish();
            return true;
        }
        // Respond to a click on the "Delete" menu option
        if (id == R.id.action_delete) {
            // Pop up confirmation dialog for deletion
            showDeleteConfirmationDialog();
            return true;
        }
        if (id == R.id.home) {
            // If the item hasn't changed, continue with navigating up to parent activity
            // which is the {@link CatalogActivity}.
            if (!itemHasChanged) {
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                return true;
            }
            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that
            // changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        }
                    };

            // Show a dialog that notifies the user they have unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the item table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PHONE };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentUri,         // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
// Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE);
            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);

            // Update the views on the screen with the values from the database
            mEditText_name.setText(name);
            mEditText_price.setText(Double.toString(price));
            mEditQuantity.setText(Integer.toString(quantity));
            mSupplier_name.setText(supplier);
            mSupplier_phone.setText(phone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
// If the loader is invalidated, clear out all the data from the input fields
        mEditText_name.setText("");
        mEditText_price.setText("");
        ;
        mEditQuantity.setText("");
        ;
        mSupplier_name.setText("");
        ;
        mSupplier_phone.setText("");
        ;
    }
    public void callSupplier(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }}
}
