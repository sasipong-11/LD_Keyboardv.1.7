package th.or.nectec.twskeyboard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

public class LexitronDatabaseHelper extends SQLiteAssetHelper{
    //Constants for db name and version
    private static final String DATABASE_NAME = "lexitron_v3.db";
    private static final int DATABASE_VERSION = 1;

    //Constants for identifying table and columns
    public static String TABLE_NAME = "lexitron";
    public static final String _ID = "id";
    public static final String LEXITRON_PART = "scat";
    public static final String LEXITRON_WORD = "sentry";
    public static final String LEXITRON_THAI = "sdef";
    public static final String LEXITRON_ENG = "tentry";

    public static final String[] ALL_COLUMNS =
            {_ID, LEXITRON_PART, LEXITRON_WORD, LEXITRON_THAI, LEXITRON_ENG};

    public LexitronDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        //db.execSQL("DROP TABLE IF EXISTS " + "User");
        // Create tables again
        onCreate(db);
    }
    public Cursor getDatafromLexitron (String noteFilter){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, scat, sentry, sdef, tentry FROM lexitron WHERE "+ noteFilter, null);

        return cursor;
    }
}

