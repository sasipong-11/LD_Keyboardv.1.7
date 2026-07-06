package th.or.nectec.twskeyboard.ldrule;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import th.or.nectec.twskeyboard.soundex.Soundex;

/**
 * Created by Lattapol on 6/28/16 AD.
 */

public class LDRule {
    private static final String strThai1 = "กขคฆงจฉชซฌญฎฏฐฑฒณดตถทธนบปผฝพฟภมยรฤลวศษสหฬอฮฯะัาำิีึืุูแเโใไๆ็่้๊๋์";
    private static final String strEng1= "abcdefghijklmnopqrstuvwxyz";
    private static boolean isEnglish;
    Soundex soundex;

    public ArrayList<String> findWordWithLDLaw(String inputWord){

        char lan = inputWord.charAt(0);
        if( (lan >= 'a' && lan <= 'z') || (lan >='A' && lan <='Z') ){
            isEnglish = true;
            inputWord = inputWord.toLowerCase();
        }else{
            isEnglish = false;
        }
        ArrayList<String> combineResult = new ArrayList<String>();
        combineResult.add(inputWord);
        combineResult.addAll(changeChar(inputWord));
        //combineResult.addAll(deleteChar(inputWord));
        combineResult.addAll(switchChar(inputWord));
        combineResult.addAll(addChar(inputWord));

        return combineResult;
    }

    //Delete
    public ArrayList<String> deleteChar(String word){
        ArrayList<String> result = new  ArrayList<String>();

        for(int i=0; i< word.length(); i++){
            String before = word.substring(0,i);
            //Log.d("Before:-->", String.valueOf(before));
            String after = word.substring(i+1);
            //Log.d("After:-->", String.valueOf(after));
            result.add(before + after);
        }
        //Log.d("Result:-->", String.valueOf(result));
        return result;
    }

    //Switching
    public ArrayList<String> switchChar(String word){
        ArrayList<String> result = new ArrayList<String>();
        char firstIndex;
        char secondIndex;
        char [] wordArray;

        for(int i=0; i<word.length()-1; i++){
            //Convert String to Array of Char
            wordArray = word.toCharArray();

            //Get char
            firstIndex = word.charAt(i);
            secondIndex = word.charAt(i+1);

            //Swap
            wordArray[i] = secondIndex;
            wordArray[i+1] = firstIndex;

            result.add(new String(wordArray));
        }
        //Log.d("SWAP:", String.valueOf(result));
        return result;
    }

    //Changging
    public ArrayList<String> changeChar(String word){
        ArrayList<String> result = new ArrayList<String>();

        if(isEnglish){
            result.add(word);
        }else{
            char [] wordArray = word.toCharArray();
            char [] compare;
            for(int i=0; i<word.length(); i++){
                char [] key = Mapping.ThaiMapping(wordArray[i]);

                Log.d("Get cases from Mapping:", String.valueOf(key));
                for(int j=0; j<key.length;j++){

                    compare = wordArray.clone();

                    compare[i] = key[j];

                    result.add(new String(compare));

                }
            }
        }

        Log.d("Words from changing:-->", String.valueOf(result));  //get list of words from changing
        return result;
    }

    //Changing Vowel
    public ArrayList<String> changeVowel(String word){
        ArrayList<String> result = new ArrayList<String>();

        if(isEnglish){
            result.add(word);
        }else{
            char [] wordArray = word.toCharArray();
            char [] compare;
            for(int i=0; i<word.length(); i++){
                char [] key = Mapping.ThaiVowelMapping(wordArray[i]);

                Log.d("Cases of VowelMapping:", String.valueOf(key));
                for(int j=0; j<key.length;j++){

                    compare = wordArray.clone();

                    compare[i] = key[j];

                    result.add(new String(compare));

                }
            }
        }
        ///////////
        /*int wordSize = result.size();
        ArrayList<String> ruleSoundex = new ArrayList<String>();

        for(int i=0; i<wordSize; i++) {

             ruleSoundex = soundex.getSoundex(result.get(i));
        }*/


        /////////////
        Log.d("Words Vowelchange:-->", String.valueOf(result));  //get list of words from changing
        return result;
    }

    //Adding
    public ArrayList<String> addChar(String word){
        ArrayList<String> result  = new ArrayList<String>();
        ArrayList<String> wordSequence  = new ArrayList<String>();
        char [] ch = word.toCharArray();
        for(int i=0; i<ch.length; i++){
            wordSequence.add(""+ch[i]);
        }

        //Loop n+1
        for(int i=0; i<=word.length(); i++){
            //Insert a char at i
            String relpace;

            if(isEnglish){
                relpace = strEng1;
            }else{
                relpace = strThai1;
            }
            for(int j=0; j<relpace.length(); j++){
                ArrayList<String> temp = new ArrayList<String>();
                temp.addAll(wordSequence);
                temp.add(i, ""+relpace.charAt(j));

                //Log.d("eachLetterEachPosition:", String.valueOf(temp));

                //นำมารวมเป็นคำ[กรว, ขรว, ครว, ฆรว, งรว,...]
                String newWord = "";
                for(int index=0; index<temp.size(); index++){
                    newWord += temp.get(index);
                }
                result.add(newWord);

            }
        }

        return result;
    }


}
