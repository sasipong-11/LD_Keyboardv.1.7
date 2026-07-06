package th.or.nectec.twskeyboard.editdistance;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Lattapol on 1/3/2560.
 */

public class Distance {

    ArrayList<String> beforeSort;
    String input;
    public int min = 100;
    public Distance(ArrayList<String> LDWord, String userInput){
        beforeSort = LDWord;
        input = userInput;
    }

    public ArrayList<String> getEditDistance(){
        ArrayList<WordDistance> calDistance = sortDistance(input, beforeSort);

        ArrayList<String> sortDistance = new ArrayList<String>();
        Log.d("calDistance:---> ", String.valueOf(calDistance.size()));
        for(int i=0; i<calDistance.size(); i++){

            //Delete hight distance
            if(calDistance.get(i).getDistance() > (min + 1)){
                continue;
            }
            sortDistance.add(calDistance.get(i).getWord());
            //Log.d("sortDistance:---> ", String.valueOf(sortDistance));
        }

        return  sortDistance;
    }

    private ArrayList<WordDistance> sortDistance(String userInput, ArrayList<String> searchText){
        ArrayList<WordDistance> result = new ArrayList<WordDistance>();
        WordDistance cal = new WordDistance();
        //searchText.size คือ คำทั้งหมดที่ select ด้วย %% ออกมา
        int distance = 0;
        for(int i=0; i<searchText.size(); i++){
            cal = new WordDistance();
            distance = minDistance(userInput, searchText.get(i));
            // Print Distance
            cal.setWord(searchText.get(i));
            cal.setDistance(distance);
            result.add(cal);

            if(min > distance){
                min = distance;
            }
        }

        bubbleSort(result);

        return result;
    }

    private int minDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();

        // len1+1, len2+1, because finally return dp[len1][len2]
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        //iterate though, and check last char
        for (int i = 0; i < len1; i++) {
            char c1 = word1.charAt(i);
            for (int j = 0; j < len2; j++) {
                char c2 = word2.charAt(j);

                //if last two chars equal
                if (c1 == c2) {
                    //update dp value for +1 length
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;

                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;
                    dp[i + 1][j + 1] = min;
                }
            }
        }

        return dp[len1][len2];
    }

    public void bubbleSort(ArrayList<WordDistance> stringArray) {

        int n = stringArray.size();
        WordDistance temp;

        for(int i=0; i < n; i++){
            for(int j=1; j < (n-i); j++){
                if(stringArray.get(j-1).getDistance() > stringArray.get(j).getDistance()){
                    //swap the elements!
                    temp = stringArray.get(j-1);
                    stringArray.set(j-1, stringArray.get(j));
                    stringArray.set(j,temp);
                }
            }
        }
    }
}
