package th.or.nectec.twskeyboard.ldrule;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import th.or.nectec.twskeyboard.DictionaryDatabaseHelper;
import th.or.nectec.twskeyboard.soundex.Soundex;


public class AddMissingChar {
    Soundex soundex;
    Context ctx;
    public AddMissingChar(Context context){
        ctx = context;
    }

    public ArrayList<String> findMissingChar(String input){

        int strSize = input.length() + 3;
        //input = input.replace("ะ","");
        input.toLowerCase();

        ArrayList<String> deleteChar1 = deleteChar(input);
        ArrayList<String> deleteChar2 = new ArrayList<String>();
        //ArrayList<String> deleteChar3 = new ArrayList<String>();

        for(int i=0; i<deleteChar1.size(); i++){
            deleteChar2.add(deleteChar1.get(i));
            deleteChar2.addAll(deleteChar(deleteChar1.get(i)));
        }
        Log.d("DeleteChar2:---> ", String.valueOf(deleteChar2));

        /*for(int i=0; i<deleteChar2.size(); i++){
            deleteChar3.add(deleteChar2.get(i));
            deleteChar3.addAll(deleteChar(deleteChar3.get(i)));
        }*/
        //Log.d("DeleteChar3:---> ", String.valueOf(deleteChar2));

        //Delete Depleting Word
        Set<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        set.addAll(deleteChar2);
        deleteChar2 = new ArrayList<String>(set);

        //set.addAll(deleteChar3);
        //deleteChar3 = new ArrayList<String>(set);

        //String sqlCondition = createSQLState(deleteChar3);
        String sqlCondition = createSQLState(deleteChar2);
        sqlCondition += ") AND LENGTH(SENSEGROUP) < " + strSize;

        ArrayList<String> wordList = new ArrayList<String>();
        String noteFilter = sqlCondition;
        //Log.d("Lattapol!!!!!!",noteFilter);
        //wordList.add(noteFilter);
        //Cursor cursor = ctx.getContentResolver().query(DictionaryProvider.CONTENT_URI,
                //DictionaryDBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
        Cursor cursor = new DictionaryDatabaseHelper(ctx).getDatafromDictionary(noteFilter);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                String word = cursor.getString(1);
                wordList.add(word);
            }while (cursor.moveToNext());
        }
        //Log.d("wordList:", String.valueOf(wordList));

        /////////////////////////////////////////
        /*int deleteChar2Size = deleteChar2.size()/6;
        soundex = new Soundex(ctx);
        ArrayList<String> delChar2Soundex;

        ArrayList<String> addDelChar2Soundex = new ArrayList<String>();

        if(wordList.isEmpty()) {

            for (int i = 0; i < deleteChar2Size; i++) {

                delChar2Soundex = soundex.getSoundex(deleteChar2.get(i));
                addDelChar2Soundex.addAll(delChar2Soundex);

            }
            wordList.addAll(addDelChar2Soundex);
        }*/
        /////////////////////////////////////////

        return wordList;
    }

    public static ArrayList<String> deleteChar(String text){
        String result = "";
        ArrayList<String> returnValue = new ArrayList<String>();
        if(text.length() < 3){

            return returnValue;
        }

        for(int j=0; j<text.length(); j++){
            result = "";
            for(int i=0; i<text.length(); i++){
                if(i == j){
                    continue;
                }else{
                    result = result + text.substring(i, i+1);

                    //Log.d("CHK addMissingChar:", String.valueOf(result));
                }
            }
            //result = result + "%";
            returnValue.add(result);
        }
        //Log.d("ooooooooooooooooo:", String.valueOf(returnValue));
        return returnValue;
    }

    public static String createSQLState(ArrayList<String> wordList){
        //String test = "(SENSEGROUP LIKE '%ธ%ร%ร%ม%ช%า%ิ%ค%' OR SENSEGROUP LIKE 'โรงเรียน') AND LENGTH(SENSEGROUP) < 11";
        String sql = "(";
        if(wordList.size() > 0){
            sql += DictionaryDatabaseHelper.WORD_VOCAB +" LIKE '" + addSign(wordList.get(0)) + "'";
        }

        for(int i=1; i<wordList.size(); i++){
            sql = sql + " OR " + DictionaryDatabaseHelper.WORD_VOCAB + " LIKE '"+ addSign(wordList.get(i))+ "'" ;
        }

        return sql;
    }

    public static String addSign(String text){
        String result = "";
        for(int i=0; i<text.length(); i++){
            result = result + "%" + text.substring(i, i+1);
        }
        result = result + "%";
        return result;
    }

}
