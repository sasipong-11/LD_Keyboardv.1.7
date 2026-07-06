package th.or.nectec.twskeyboard.soundex;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import th.or.nectec.twskeyboard.GraphemeToPhoneme;

/**
 * Created by Lattapol on 19/12/2559.
 */

public class IgnoreLR {
    Context ctx;
    public IgnoreLR(Context context){
        this.ctx = context;
    }

    public ArrayList<String> getIgnoreLR(String word){

        //Find sequence if r and l
        ArrayList<Integer> charSeq = new ArrayList<Integer>();
        for(int i=0; i<word.length(); i++){
            if(word.charAt(i) == 'ร' || word.charAt(i) == 'ล'){
                charSeq.add(i);
            }
        }

        //Find Number of possible word
        ArrayList<String> insertChar = permatation(charSeq.size());
        ArrayList<String> result = new ArrayList<String>();
        String newWord;
        //จำนวนแบบ G2P ที่จะเกิดขึ้นทั้งหมด
        for(int wordSeq=0; wordSeq<insertChar.size(); wordSeq++) {
            newWord = new String(word);
            //แต่ละคำมี ร ล กี่ตำแหน่ง
            for (int j = 0; j < charSeq.size(); j++) {
                //ในแต่ละแบบของ G2P ที่จะเกิดขึ้นนำมาเปลี่ยนตัวอักษร ร  --> ร, ล
                //หาตำแหน่งที่จะเปลี่ยน ร ล
                newWord = ChangeChar(newWord, insertChar.get(wordSeq).charAt(j), charSeq.get(j));
            }
            result.add(newWord);
        }

        //Convert to G2P
        GraphemeToPhoneme g2p = new GraphemeToPhoneme(ctx);
        ArrayList<String> outputG2P = new ArrayList<String>();


        //ถ้าเข้า for o/p จะเป็น size ไม่ใช่คำ
        for(int i=0; i<result.size(); i++){

            String G2PText = g2p.getG2P(result.get(i)).replace("*","");
            outputG2P.add(G2PText);

        }
        return outputG2P;
    }

    //Swap
    public static String ChangeChar(String word, char key, int sequence){

        char [] wordArray = word.toCharArray();
        char [] compare;
        compare = wordArray.clone();
        compare[sequence] = key;
        String result  = new String(compare);

        return result;
    }

    private static ArrayList<String> permatation(int lengthOfSinglePermutation){
        ArrayList<String> result = new ArrayList<String>();
        String[] input = { "ร", "ล" };

        // we need to check number of unique values in array
        Set<String> arrValues = new HashSet<String>();
        for (String i : input) {
            arrValues.add(i);
        }
        int noOfUniqueValues = arrValues.size();

        int[] indexes = new int[lengthOfSinglePermutation];
        int totalPermutations = (int) Math.pow(noOfUniqueValues, lengthOfSinglePermutation);
        String character;

        for (int i = 0; i < totalPermutations; i++) {
            character = "";
            for (int j = 0; j < lengthOfSinglePermutation; j++) {
                character += input[indexes[j]];
            }
            result.add(character);

            for (int j = 0; j < lengthOfSinglePermutation; j++) {
                if (indexes[j] >= noOfUniqueValues - 1) {
                    indexes[j] = 0;
                }
                else {
                    indexes[j]++;
                    break;
                }
            }
        }
        return result;
    }
}