package th.or.nectec.twskeyboard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;


public class DictionaryDatabaseHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "spell_sys.db";
    private static final int DATABASE_VERSION = 1;

    //Constants for identifying table and columns
    public static String TABLE_NAME = "BEST_SPELL_TH";
    public static final String _ID = "ID";
    public static final String WORD_VOCAB = "SENSEGROUP";
    public static final String WORD_G2P = "G2P";

    public  String[] ALL_COLUMNS =
            {_ID, WORD_VOCAB, WORD_G2P};

    public DictionaryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        //db.execSQL("DROP TABLE IF EXISTS " + "User");
        // Create tables again
        onCreate(db);
    }

    public ArrayList<String> getListDatafromDictionary (String noteFilter){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> words = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM BEST_SPELL_TH WHERE " + noteFilter, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            words.add(cursor.getString(1));
            cursor.moveToNext();
        }
        db.close();
        cursor.close();
        return words;
    }

    public Cursor getDatafromDictionary (String noteFilter){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ID,SENSEGROUP,G2P FROM BEST_SPELL_TH WHERE "+ noteFilter, null);

        return cursor;
    }



}