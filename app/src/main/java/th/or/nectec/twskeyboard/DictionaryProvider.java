package th.or.nectec.twskeyboard;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by Lattapol on 7/8/16 AD.
 */

public class DictionaryProvider  extends ContentProvider {

    private static final String AUTHORITY = "th.or.nectec.twskeyboard.g2p";
    private static final String BASE_PATH = "dictionary";
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    // Constant to identify the requested operation
    private static final int WORDS = 1;
    private static final int WORDS_ID = 2;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "Word";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, WORDS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH +  "/#", WORDS_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DictionaryDBOpenHelper helper = new DictionaryDBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if (uriMatcher.match(uri) == WORDS_ID) {
            selection = DictionaryDBOpenHelper._ID + "=" + uri.getLastPathSegment();
        }
       /*if(TWSActivity.isEnglish){
            DictionaryDBOpenHelper.TABLE_NAME = "BEST_SPELL_EN";
        }else{
            DictionaryDBOpenHelper.TABLE_NAME = "BEST_SPELL_TH";
        }*/
        DictionaryDBOpenHelper.TABLE_NAME = "BEST_SPELL_TH";
        return database.query(DictionaryDBOpenHelper.TABLE_NAME, DictionaryDBOpenHelper.ALL_COLUMNS,
                selection, null, null, null,
                null + " DESC");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(DictionaryDBOpenHelper.TABLE_NAME,
                null, values);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DictionaryDBOpenHelper.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(DictionaryDBOpenHelper.TABLE_NAME,
                values, selection, selectionArgs);
    }



}