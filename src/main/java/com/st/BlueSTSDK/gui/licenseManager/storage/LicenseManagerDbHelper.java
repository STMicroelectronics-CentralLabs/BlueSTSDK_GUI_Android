package com.st.BlueSTSDK.gui.licenseManager.storage;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseManagerDBContract.LicenseEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for crate the DB for store the license data and for doing query to it
 */
public class LicenseManagerDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LicenseManager.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_LICENSE_TABLE =
            "CREATE TABLE " + LicenseEntry.TABLE_NAME + " (" +
                    LicenseEntry._ID + " INTEGER PRIMARY KEY," +
                    LicenseEntry.COLUMN_NAME_BOARD_ID + TEXT_TYPE + COMMA_SEP +
                    LicenseEntry.COLUMN_NAME_LICENSE_TYPE + TEXT_TYPE + COMMA_SEP +
                    LicenseEntry.COLUMN_NAME_LICENSE_CODE + BLOB_TYPE +
                    //put unique board id/licenseType
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LicenseEntry.TABLE_NAME;


    private static LicenseManagerDbHelper mInstance = null;

    public static LicenseManagerDbHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new LicenseManagerDbHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }


    private LicenseManagerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LICENSE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Insert a new license into the db
     * @param entry license to add
     */
    public void insert(LicenseEntry entry){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LicenseEntry.COLUMN_NAME_BOARD_ID, entry.getBoardId());
        values.put(LicenseEntry.COLUMN_NAME_LICENSE_TYPE, entry.getLicenseType());
        values.put(LicenseEntry.COLUMN_NAME_LICENSE_CODE, entry.getLicenseCode());

        // Insert the new row, returning the primary key value of the new row
        entry.setId(db.insert(LicenseEntry.TABLE_NAME,null, values));

        db.close();

    }//insert


    /**
     * Return a cursor loader for query the database and returing all the license associated with a
     * specific board id
     * @param c context to use for open the db
     * @param boardId board to search
     * @return loader that will select all the license that can be apply to the specific board id
     */
    static public Loader<Cursor> getLicenseForBoard(final Context c,final String boardId){

        return new AsyncTaskLoader<Cursor>(c) {

            @Override
            public Cursor loadInBackground() {

                SQLiteDatabase db = getInstance(c).getReadableDatabase();
                String[] projection = {
                        LicenseEntry._ID,
                        LicenseEntry.COLUMN_NAME_BOARD_ID,
                        LicenseEntry.COLUMN_NAME_LICENSE_TYPE,
                        LicenseEntry.COLUMN_NAME_LICENSE_CODE
                };

                // How you want the results sorted in the resulting Cursor
                String sortOrder = LicenseEntry.COLUMN_NAME_LICENSE_TYPE;

                String selection = LicenseEntry.COLUMN_NAME_BOARD_ID + " LIKE ?";
                // Specify arguments in placeholder order.
                String[] selectionArgs = {boardId};

                return db.query(
                        LicenseEntry.TABLE_NAME,  // The table to query
                        projection,               // The columns to return
                        selection,                // The columns for the WHERE clause
                        selectionArgs,            // The values for the WHERE clause
                        null,                     // don't group the rows
                        null,                     // don't filter by row groups
                        sortOrder                 // The sort order
                );
            }
        };
    }//getLicenseForBoard

    /**
     * create a loader for return all the board that have a license in the DB
     * @param c context used for open the db
     * @return a list of board id present in the db
     */
    static public Loader<Cursor> getBoards(final Context c){

        return new AsyncTaskLoader<Cursor>(c){

            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db =getInstance(c).getReadableDatabase();

                String[] projection = {
                        LicenseEntry._ID,
                        LicenseEntry.COLUMN_NAME_BOARD_ID,
                };

                // How you want the results sorted in the resulting Cursor
                String sortOrder = LicenseEntry.COLUMN_NAME_BOARD_ID + " DESC";

                return db.query(
                    LicenseEntry.TABLE_NAME,  // The table to query
                    projection,               // The columns to return
                    null,                     // The columns for the WHERE clause
                    null,                     // The values for the WHERE clause
                    null,                     // don't group the rows
                    null,                     // don't filter by row groups
                    sortOrder                 // The sort order;
                );
            }
        };

    }//getLicenseForBoard

    /**
     * build a LicenseEntry from a cursor line
     * @param c cursor to use for build the entry
     * @return license entry build with the data inside the cursor
     */
    public static LicenseEntry buildLicenseEntry(Cursor c){
        long id = c.getLong(c.getColumnIndex(LicenseEntry._ID));
        String boardId = c.getString(c.getColumnIndex(LicenseEntry.COLUMN_NAME_BOARD_ID));
        String licType = c.getString(c.getColumnIndex(LicenseEntry.COLUMN_NAME_LICENSE_TYPE));
        byte[] licCode = c.getBlob(c.getColumnIndex(LicenseEntry.COLUMN_NAME_LICENSE_CODE));

        LicenseEntry temp = new LicenseEntry(boardId,licType,licCode);
        temp.setId(id);
        return temp;
    }

    /**
     * read all the entry in the cursor and build a list of license
     * @param c cursor where extract the data
     * @return list of license inside the cursor
     */
    public static List<LicenseEntry> buildLicenseEntryList(Cursor c){
        List<LicenseEntry> list = new ArrayList<>(c.getCount());
        c.moveToFirst();
        while(!c.isAfterLast()){
            list.add(buildLicenseEntry(c));
            c.moveToNext();
        }//while
        return list;
    }

    /**
     * remove all the license in the db
     */
    public void deleteLicenses(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(LicenseEntry.TABLE_NAME, null, null);
    }

    /**
     * remove all the license for a specific board
     * @param boardId board to delete
     */
    public void deleteLicenses(String boardId){
        SQLiteDatabase db = getWritableDatabase();

        String selection = LicenseEntry.COLUMN_NAME_BOARD_ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { boardId };

        db.delete(LicenseEntry.TABLE_NAME, selection, selectionArgs);
    }


}
