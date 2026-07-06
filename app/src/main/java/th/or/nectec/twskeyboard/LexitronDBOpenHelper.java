package th.or.nectec.twskeyboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lattapol on 8/2/16 AD.
 *
 */
public class LexitronDBOpenHelper  extends SQLiteOpenHelper {

    //Constants for db name and version
    private static final String DATABASE_NAME = "lexitron.db";
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

    public LexitronDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        /*if(TWSActivity.isEnglish){
            TABLE_NAME = "LEXITRON_EN_SENSE";
        }else{
            TABLE_NAME = "LEXITRON_TH_SENSE";
        }*/
        TABLE_NAME = "lexitron";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
