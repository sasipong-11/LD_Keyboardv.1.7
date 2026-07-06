package th.or.nectec.twskeyboard.soundex;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by Lattapol on 16/12/2559.
 */

public class MissSpellingWord {
    //ค้นหาแบบคำผิด  1 คำ
    public ArrayList<Part> oneMissSpellingWord(String wordSound){
        //ตัดเสียงคำคัพท์ออกเป็นพยางย่อยๆ
        String [] segment = wordSound.split(Pattern.quote("|"));
        Part wordCom;
        ArrayList<Part> result = new ArrayList<Part>();
        boolean firstPart = true;

        for(int row=0; row<segment.length; row++){
            firstPart = true;
            wordCom = new Part();
            wordCom.first = "";
            wordCom.second = "";
            for(int col=0; col<segment.length; col++){

                //ข้ามการเปลี่ยนเทียบคำที่อยู่ตามแนวเส้นทแยงมุม
                if(row == col){
                    firstPart = false;
                    continue;
                }

                //แยกส่วนหน้ากับส่วนหลังของคำ
                if(firstPart){
                    wordCom.first += segment[col] + "|";
                }else{
                    wordCom.second += segment[col] + "|";
                }
            }
            result.add(wordCom);
        }

        return result;
    }
}
