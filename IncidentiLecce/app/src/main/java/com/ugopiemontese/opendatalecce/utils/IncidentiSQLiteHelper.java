package com.ugopiemontese.opendatalecce.utils;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.ugopiemontese.opendatalecce.utils.Incidenti;

public class IncidentiSQLiteHelper extends SQLiteOpenHelper {
	 
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "IncidentiDB";
    
    // Table name
    private static final String TABLE_INCIDENTI = "incidenti";

    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String KEY_TOTALE = "totale";
    private static final String KEY_COINVOLTI = "coinvolti";
    private static final String KEY_ILLESI = "illesi";
    private static final String KEY_MORTI = "morti";
    private static final String KEY_PROGNOSI = "prognosi";
    private static final String KEY_FERITI = "feriti";

    private static final String[] COLUMNS = {KEY_ID, KEY_ADDRESS, KEY_LAT, KEY_LNG, KEY_TOTALE, KEY_COINVOLTI, KEY_ILLESI, KEY_MORTI, KEY_PROGNOSI, KEY_FERITI};
 
    public IncidentiSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); 
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
    	
        String CREATE_INCIDENTI_TABLE = "CREATE TABLE incidenti ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "address TEXT, " +
                "lat FLOAT, " +
                "lng FLOAT, " +
                "totale INTEGER, " +
                "coinvolti INTEGER, " +
                "illesi INTEGER, " +
                "morti INTEGER, " +
                "prognosi INTEGER, " +
                "feriti INTEGER )";
        db.execSQL(CREATE_INCIDENTI_TABLE);
        
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	
        db.execSQL("DROP TABLE IF EXISTS incidenti");
        this.onCreate(db);
        
    }
    
    public void addIncidenti(Incidenti arg0){
    	
		//Log.d("addIncidenti", arg0.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_ADDRESS, arg0.getAddress());
		values.put(KEY_LAT, arg0.getLat());
		values.put(KEY_LNG, arg0.getLng());
		values.put(KEY_TOTALE, arg0.getTotale());
		values.put(KEY_COINVOLTI, arg0.getCoinvolti());
		values.put(KEY_ILLESI, arg0.getIllesi());
		values.put(KEY_MORTI, arg0.getMorti());
		values.put(KEY_PROGNOSI, arg0.getPrognosi());
		values.put(KEY_FERITI, arg0.getFeriti());

		db.insert(TABLE_INCIDENTI, null, values);
		db.close();
		
	}
    
    public Incidenti getIncidenti(int id){
    	
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =
                db.query(TABLE_INCIDENTI,
                COLUMNS,
                " id = ?",
                new String[] { String.valueOf(id) },
                null,
                null,
                null,
                null);
        if (cursor != null)
            cursor.moveToFirst();
     
        Incidenti arg0 = new Incidenti();
        arg0.setId(Integer.parseInt(cursor.getString(0)));
        arg0.setAddress(cursor.getString(1));
        arg0.setLat(Double.valueOf(cursor.getString(2)));
        arg0.setLng(Double.valueOf(cursor.getString(3)));
        arg0.setTotale(Integer.parseInt(cursor.getString(4)));
        arg0.setCoinvolti(Integer.parseInt(cursor.getString(5)));
        arg0.setIllesi(Integer.parseInt(cursor.getString(6)));
        arg0.setMorti(Integer.parseInt(cursor.getString(7)));
        arg0.setPrognosi(Integer.parseInt(cursor.getString(8)));
        arg0.setFeriti(Integer.parseInt(cursor.getString(9)));
        //Log.d("getIncidenti("+id+")", arg0.toString());
     
        return arg0;
        
    }
    
    public int getIncidentiCount(){
    	
    	String query = "SELECT COUNT(id) FROM " + TABLE_INCIDENTI;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
     
        int count = 0;
        
        if (cursor.moveToFirst()) {
        	count = cursor.getInt(0);
        }
        
        return count;
        
    }
    
    public List<Incidenti> getAllIncidenti() {
    	
        List<Incidenti> incidenti = new LinkedList<Incidenti>();
        String query = "SELECT  * FROM " + TABLE_INCIDENTI;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
  
        Incidenti arg0 = null;
        if (cursor.moveToFirst()) {
            do {
                arg0 = new Incidenti();
                arg0.setId(Integer.parseInt(cursor.getString(0)));
                arg0.setAddress(cursor.getString(1));
                arg0.setLat(Double.valueOf(cursor.getString(2)));
                arg0.setLng(Double.valueOf(cursor.getString(3)));
                arg0.setTotale(Integer.parseInt(cursor.getString(4)));
                arg0.setCoinvolti(Integer.parseInt(cursor.getString(5)));
                arg0.setIllesi(Integer.parseInt(cursor.getString(6)));
                arg0.setMorti(Integer.parseInt(cursor.getString(7)));
                arg0.setPrognosi(Integer.parseInt(cursor.getString(8)));
                arg0.setFeriti(Integer.parseInt(cursor.getString(9)));
                incidenti.add(arg0);
            } while (cursor.moveToNext());
        }
        //Log.d("getAllIncidenti()", incidenti.toString());
  
        return incidenti;
        
    }
 
}