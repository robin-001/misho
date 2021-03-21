package com.scanner.misho;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

public class SQLiteDocumentDatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "IdentityDB";
    private static final String TABLE_NAME = "Documents";
    private static final String KEY_ID = "id";
    private static final String KEY_TRX_ID = "trx_id";
    private static final String KEY_TYPE = "type";

    private static final String KEY_DOCUMENT_NUMBER = "document_number";
    private static final String KEY_IDENTIFICATION_NUMBER = "identification_number";
    private static final String KEY_FIRST_NAME = "firstname";
    private static final String KEY_SURNAME = "surname";
    private static final String KEY_GIVENNAMES = "givennames";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_DOB = "dob";
    private static final String KEY_ISSUING_DATE = "issuing_date";
    private static final String KEY_EXPIRY_DATE = "expiry_date";
    private static final String KEY_ISSUING_AUTHORITY = "issuing_authority";
    private static final String KEY_NATIONALITY = "nationality";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_IMEI = "imei";
    private static final String KEY_CREATED_BY = "created_by";
    private static final String KEY_FRONT_PHOTO = "front_photo";
    private static final String KEY_BACK_PHOTO = "back_photo";
    private static final String KEY_EXTRA0 = "extra0";
    private static final String KEY_EXTRA1 = "extra1";
    private static final String KEY_EXTRA2 = "extra2";
    private static final String KEY_RAW_DATA = "raw_data";
    private static final String KEY_SYNCED = "synced";
    private static final String KEY_CREATED_AT = "created_at";

    private static final String[] COLUMNS = {KEY_ID,KEY_TRX_ID,KEY_TYPE,KEY_DOCUMENT_NUMBER,KEY_IDENTIFICATION_NUMBER,KEY_FIRST_NAME,KEY_SURNAME,KEY_GIVENNAMES,KEY_GENDER,KEY_DOB,KEY_ISSUING_DATE,KEY_EXPIRY_DATE,KEY_ISSUING_AUTHORITY,KEY_NATIONALITY,KEY_LATITUDE,KEY_LONGITUDE,KEY_IMEI,KEY_CREATED_BY,KEY_FRONT_PHOTO,KEY_BACK_PHOTO,KEY_EXTRA0,KEY_EXTRA1,KEY_EXTRA2,KEY_RAW_DATA,KEY_SYNCED,KEY_CREATED_AT};
    private static final String TAG = "DocumentsDBHandler";

    public SQLiteDocumentDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE = "CREATE TABLE 'Documents' (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_TRX_ID + " TEXT UNIQUE," +
                KEY_TYPE + " INTEGER," +
                KEY_DOCUMENT_NUMBER + " TEXT," +
                KEY_IDENTIFICATION_NUMBER + " TEXT," +
                KEY_FIRST_NAME + " TEXT," +
                KEY_SURNAME + " TEXT," +
                KEY_GIVENNAMES + " TEXT," +
                KEY_GENDER + " TEXT," +
                KEY_DOB + " TEXT," +
                KEY_ISSUING_DATE + " TEXT," +
                KEY_EXPIRY_DATE + " TEXT," +
                KEY_ISSUING_AUTHORITY + " TEXT," +
                KEY_NATIONALITY + " TEXT," +
                KEY_LATITUDE + " TEXT," +
                KEY_LONGITUDE + " TEXT," +
                KEY_IMEI + " TEXT," +
                KEY_CREATED_BY + " TEXT," +
                KEY_FRONT_PHOTO + " TEXT," +
                KEY_BACK_PHOTO + " TEXT," +
                KEY_EXTRA0 + " TEXT," +
                KEY_EXTRA1 + " TEXT," +
                KEY_EXTRA2 + " TEXT," +
                KEY_RAW_DATA + " TEXT," +
                KEY_SYNCED + " TEXT," +
                KEY_CREATED_AT + " TEXT)";

        Log.d(TAG, CREATION_TABLE);

        try {
            db.execSQL(CREATION_TABLE);
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // you can implement here migration process
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public void deleteOne(int id) {
        // Get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public String getTableAsString(String tableName) {
        Log.d(TAG, "getTableAsString called");
        SQLiteDatabase db = this.getReadableDatabase();
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }
        db.close();
        return tableString;
    }


    public Document getDocument(int id) throws ParseException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, // a. table
                COLUMNS, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id)}, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit

        if (cursor != null)
            cursor.moveToFirst();

        Document document = new Document();
        document.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        document.setTrxID(cursor.getString(cursor.getColumnIndex(KEY_TRX_ID)));
        document.setType(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
        document.setDocument_number(cursor.getString(cursor.getColumnIndex(KEY_DOCUMENT_NUMBER)));
        document.setIdentification_number(cursor.getString(cursor.getColumnIndex(KEY_IDENTIFICATION_NUMBER)));
        document.setFirstname(cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME)));
        document.setSurname(cursor.getString(cursor.getColumnIndex(KEY_SURNAME)));
        document.setGivennames(cursor.getString(cursor.getColumnIndex(KEY_GIVENNAMES)));
        document.setGender(cursor.getString(cursor.getColumnIndex(KEY_GENDER)));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        document.setDob(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_DOB))));
        if(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_DATE))!=null){
            document.setIssuing_date(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_DATE))));
        }
        document.setExpiry_date(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_EXPIRY_DATE))));
        document.setIssuing_authority(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_AUTHORITY)));
        document.setNationality(cursor.getString(cursor.getColumnIndex(KEY_NATIONALITY)));
        document.setLatitude(cursor.getString(cursor.getColumnIndex(KEY_LATITUDE)));
        document.setLongitude(cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE)));
        document.setImei(cursor.getString(cursor.getColumnIndex(KEY_IMEI)));
        document.setCreated_by(cursor.getString(cursor.getColumnIndex(KEY_CREATED_BY)));
        document.setFront_photo(cursor.getString(cursor.getColumnIndex(KEY_FRONT_PHOTO)));
        document.setBack_photo(cursor.getString(cursor.getColumnIndex(KEY_BACK_PHOTO)));
        document.setExtra0(cursor.getString(cursor.getColumnIndex(KEY_EXTRA0)));
        document.setExtra1(cursor.getString(cursor.getColumnIndex(KEY_EXTRA1)));
        document.setExtra2(cursor.getString(cursor.getColumnIndex(KEY_EXTRA2)));
        document.setRaw_data(cursor.getString(cursor.getColumnIndex(KEY_RAW_DATA)));
        document.setSynced(cursor.getString(cursor.getColumnIndex(KEY_SYNCED)).equals("1")?true:false);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        document.setCreated_at(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT))));

        db.close();
        return document;
    }

    public Document getDocument(String trx_id) throws ParseException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, // a. table
                COLUMNS, // b. column names
                " trx_id = ?", // c. selections
                new String[] { trx_id}, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit

        if (cursor != null)
            cursor.moveToFirst();

        Document document = new Document();
        document.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        document.setTrxID(cursor.getString(cursor.getColumnIndex(KEY_TRX_ID)));
        document.setType(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
        document.setDocument_number(cursor.getString(cursor.getColumnIndex(KEY_DOCUMENT_NUMBER)));
        document.setIdentification_number(cursor.getString(cursor.getColumnIndex(KEY_IDENTIFICATION_NUMBER)));
        document.setFirstname(cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME)));
        document.setSurname(cursor.getString(cursor.getColumnIndex(KEY_SURNAME)));
        document.setGivennames(cursor.getString(cursor.getColumnIndex(KEY_GIVENNAMES)));
        document.setGender(cursor.getString(cursor.getColumnIndex(KEY_GENDER)));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        document.setDob(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_DOB))));
        document.setIssuing_date(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_DATE))));
        document.setExpiry_date(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_EXPIRY_DATE))));
        document.setIssuing_authority(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_AUTHORITY)));
        document.setNationality(cursor.getString(cursor.getColumnIndex(KEY_NATIONALITY)));
        document.setLatitude(cursor.getString(cursor.getColumnIndex(KEY_LATITUDE)));
        document.setLongitude(cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE)));
        document.setImei(cursor.getString(cursor.getColumnIndex(KEY_IMEI)));
        document.setCreated_by(cursor.getString(cursor.getColumnIndex(KEY_CREATED_BY)));
        document.setFront_photo(cursor.getString(cursor.getColumnIndex(KEY_FRONT_PHOTO)));
        document.setBack_photo(cursor.getString(cursor.getColumnIndex(KEY_BACK_PHOTO)));
        document.setExtra0(cursor.getString(cursor.getColumnIndex(KEY_EXTRA0)));
        document.setExtra1(cursor.getString(cursor.getColumnIndex(KEY_EXTRA1)));
        document.setExtra2(cursor.getString(cursor.getColumnIndex(KEY_EXTRA2)));
        document.setRaw_data(cursor.getString(cursor.getColumnIndex(KEY_RAW_DATA)));
        document.setSynced(cursor.getString(cursor.getColumnIndex(KEY_SYNCED)).equals("1")?true:false);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        document.setCreated_at(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT))));

        db.close();
        return document;
    }

    public ArrayList<Document> allDocuments() throws ParseException {

        ArrayList<Document> documents = new ArrayList<Document>();
        String query = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Document document = null;

        while (cursor.moveToNext()) {

                document = new Document();
                document.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                document.setTrxID(cursor.getString(cursor.getColumnIndex(KEY_TRX_ID)));
                document.setType(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
                document.setDocument_number(cursor.getString(cursor.getColumnIndex(KEY_DOCUMENT_NUMBER)));
                document.setIdentification_number(cursor.getString(cursor.getColumnIndex(KEY_DOCUMENT_NUMBER)));
                document.setFirstname(cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME)));
                document.setSurname(cursor.getString(cursor.getColumnIndex(KEY_SURNAME)));
                document.setGivennames(cursor.getString(cursor.getColumnIndex(KEY_GIVENNAMES)));
                document.setGender(cursor.getString(cursor.getColumnIndex(KEY_GENDER)));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                document.setDob(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_DOB))));

                if(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_DATE))!=null){
                    document.setIssuing_date(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_DATE))));
                }
                document.setExpiry_date(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_EXPIRY_DATE))));
                document.setIssuing_authority(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_AUTHORITY)));
                document.setNationality(cursor.getString(cursor.getColumnIndex(KEY_NATIONALITY)));
                document.setLatitude(cursor.getString(cursor.getColumnIndex(KEY_LATITUDE)));
                document.setLongitude(cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE)));
                document.setImei(cursor.getString(cursor.getColumnIndex(KEY_IMEI)));
                document.setCreated_by(cursor.getString(cursor.getColumnIndex(KEY_CREATED_BY)));
                document.setFront_photo(cursor.getString(cursor.getColumnIndex(KEY_FRONT_PHOTO)));
                document.setBack_photo(cursor.getString(cursor.getColumnIndex(KEY_BACK_PHOTO)));
                document.setExtra0(cursor.getString(cursor.getColumnIndex(KEY_EXTRA0)));
                document.setExtra1(cursor.getString(cursor.getColumnIndex(KEY_EXTRA1)));
                document.setExtra2(cursor.getString(cursor.getColumnIndex(KEY_EXTRA2)));
                document.setRaw_data(cursor.getString(cursor.getColumnIndex(KEY_RAW_DATA)));
                document.setSynced(cursor.getString(cursor.getColumnIndex(KEY_SYNCED)).equals("1")?true:false);

                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                document.setCreated_at(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT))));
                documents.add(document);

        }
        db.close();
        return documents;
    }

    public int addDocument(Document document) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, document.getType());
        values.put(KEY_TRX_ID, UUID.randomUUID().toString().replace("-", ""));//generate random uuid to sync with server
        values.put(KEY_DOCUMENT_NUMBER, document.getDocument_number());
        values.put(KEY_IDENTIFICATION_NUMBER, document.getIdentification_number());
        values.put(KEY_FIRST_NAME, document.getFirstname());
        values.put(KEY_SURNAME, document.getSurname());
        values.put(KEY_GIVENNAMES, document.getGivennames());
        values.put(KEY_GENDER, document.getGender());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        values.put(KEY_DOB, sdf.format(document.getDob()));
        values.put(KEY_ISSUING_DATE, document.getIssuing_date()!=null?sdf.format(document.getIssuing_date()):null);
        values.put(KEY_EXPIRY_DATE,sdf.format(document.getExpiry_date()));

        values.put(KEY_ISSUING_AUTHORITY, document.getIssuing_authority());
        values.put(KEY_NATIONALITY, document.getNationality());
        values.put(KEY_LATITUDE, document.getLatitude());
        values.put(KEY_LONGITUDE, document.getLongitude());
        values.put(KEY_IMEI, document.getImei());
        values.put(KEY_CREATED_BY, document.getCreated_by());
        values.put(KEY_FRONT_PHOTO, document.getFront_photo());
        values.put(KEY_BACK_PHOTO, document.getBack_photo());
        values.put(KEY_EXTRA0, document.getExtra0());
        values.put(KEY_EXTRA1, document.getExtra1());
        values.put(KEY_EXTRA2, document.getExtra2());
        values.put(KEY_RAW_DATA, document.getRaw_data());
        values.put(KEY_SYNCED, document.isSynced());

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        values.put(KEY_CREATED_AT, sdf.format(document.getCreated_at()));

        // insert
        int result = (int) db.insert(TABLE_NAME, null, values);
        db.close();
        return result;
    }

    public int updateDocument(Document document) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TYPE, document.getType());
        values.put(KEY_TRX_ID, UUID.randomUUID().toString().replace("-", ""));//generate random uuid to sync with server
        values.put(KEY_DOCUMENT_NUMBER, document.getDocument_number());
        values.put(KEY_IDENTIFICATION_NUMBER, document.getIdentification_number());
        values.put(KEY_FIRST_NAME, document.getFirstname());
        values.put(KEY_SURNAME, document.getSurname());
        values.put(KEY_GIVENNAMES, document.getGivennames());
        values.put(KEY_GENDER, document.getGender());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        values.put(KEY_DOB, sdf.format(document.getDob()));
        values.put(KEY_ISSUING_DATE, document.getIssuing_date()!=null?sdf.format(document.getIssuing_date()):null);
        values.put(KEY_EXPIRY_DATE,sdf.format(document.getExpiry_date()));

        values.put(KEY_ISSUING_AUTHORITY, document.getIssuing_authority());
        values.put(KEY_NATIONALITY, document.getNationality());
        values.put(KEY_LATITUDE, document.getLatitude());
        values.put(KEY_LONGITUDE, document.getLongitude());
        values.put(KEY_IMEI, document.getImei());
        values.put(KEY_CREATED_BY, document.getCreated_by());
        values.put(KEY_FRONT_PHOTO, document.getFront_photo());
        values.put(KEY_BACK_PHOTO, document.getBack_photo());
        values.put(KEY_EXTRA0, document.getExtra0());
        values.put(KEY_EXTRA1, document.getExtra1());
        values.put(KEY_EXTRA2, document.getExtra2());
        values.put(KEY_RAW_DATA, document.getRaw_data());
        values.put(KEY_SYNCED, document.isSynced());

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        values.put(KEY_CREATED_AT, sdf.format(document.getCreated_at()));


        int i = db.update(TABLE_NAME, // table
                values, // column/value
                KEY_TRX_ID+" = ?", // selections
                new String[] { document.getTrxID() });

        db.close();
        Log.d(TAG,"Updated Document :"+document.toString());

        return i;
    }

    public ArrayList<Document> getUnsyncedDocuments() throws ParseException {

        ArrayList<Document> documents = new ArrayList<Document>();
        String query = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY_SYNCED + "=0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Document document = null;

        while (cursor.moveToNext()) {

            document = new Document();
            document.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
            document.setTrxID(cursor.getString(cursor.getColumnIndex(KEY_TRX_ID)));
            document.setType(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
            document.setDocument_number(cursor.getString(cursor.getColumnIndex(KEY_DOCUMENT_NUMBER)));
            document.setIdentification_number(cursor.getString(cursor.getColumnIndex(KEY_DOCUMENT_NUMBER)));
            document.setFirstname(cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME)));
            document.setSurname(cursor.getString(cursor.getColumnIndex(KEY_SURNAME)));
            document.setGivennames(cursor.getString(cursor.getColumnIndex(KEY_GIVENNAMES)));
            document.setGender(cursor.getString(cursor.getColumnIndex(KEY_GENDER)));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            document.setDob(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_DOB))));

            if(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_DATE))!=null){
                document.setIssuing_date(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_DATE))));
            }
            document.setExpiry_date(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_EXPIRY_DATE))));
            document.setIssuing_authority(cursor.getString(cursor.getColumnIndex(KEY_ISSUING_AUTHORITY)));
            document.setNationality(cursor.getString(cursor.getColumnIndex(KEY_NATIONALITY)));
            document.setLatitude(cursor.getString(cursor.getColumnIndex(KEY_LATITUDE)));
            document.setLongitude(cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE)));
            document.setImei(cursor.getString(cursor.getColumnIndex(KEY_IMEI)));
            document.setCreated_by(cursor.getString(cursor.getColumnIndex(KEY_CREATED_BY)));
            document.setFront_photo(cursor.getString(cursor.getColumnIndex(KEY_FRONT_PHOTO)));
            document.setBack_photo(cursor.getString(cursor.getColumnIndex(KEY_BACK_PHOTO)));
            document.setExtra0(cursor.getString(cursor.getColumnIndex(KEY_EXTRA0)));
            document.setExtra1(cursor.getString(cursor.getColumnIndex(KEY_EXTRA1)));
            document.setExtra2(cursor.getString(cursor.getColumnIndex(KEY_EXTRA2)));
            document.setRaw_data(cursor.getString(cursor.getColumnIndex(KEY_RAW_DATA)));
            document.setSynced(cursor.getString(cursor.getColumnIndex(KEY_SYNCED)).equals("1")?true:false);

            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            document.setCreated_at(sdf.parse(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT))));
            documents.add(document);

        }
        db.close();
        return documents;
    }
}
