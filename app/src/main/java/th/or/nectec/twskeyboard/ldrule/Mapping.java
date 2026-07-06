package th.or.nectec.twskeyboard.ldrule;

import android.util.Log;

/**
 * Created by Lattapol on 6/28/16 AD.
 */

public class Mapping {

    public static char [] ThaiMapping(char wordArray)
    {
        String s = "";
        char[] c;

        switch (wordArray)
        {
            /*case 'ก':
                String[] sArray = {"ก","ด"};

                for (String n:sArray)
                    s+= n;
                c = s.toCharArray();

                return c;*/
            case 'ก':  //อักษรจากคำที่ผู้ใช้พิมพ์ ***
                return new char[] {'ก','ค','ท','ธ','อ','จ','ด','ถ','บ','ป','พ','ง','น','ข','ช','ศ','ส'};
            case 'ข':
                return new char[] {'ข','ค','ช'};
            case 'ฃ':
                return new char[] {'ซ'};
            case 'ค':
                return new char[] {'ค','ด','ข','ภ','ศ'};
            //ฅ
            case 'ฆ':
                return new char[] {'ค'};
            case 'ง':
                return new char[] {'ง','ก','ญ','น','ร','ล','ว','ด','ม','ย'};
            case 'จ':
                return new char[] {'จ','ล','ช','ด'};
            case 'ฉ':
                return new char[] {'ช','ฉ'};
            case 'ช':
                return new char[] {'ข','ช','ซ','ว','ษ','ส','ฉ','ด'};
            case 'ซ':
                return new char[] {'ซ','ช','ส','ศ','ษ'};
            case 'ฌ':
                return new char[] {'ณ'};
            case 'ญ':
                return new char[] {'ย','ญ'};
            case 'ฎ':
                return new char[] {'ถ','ฏ'};
            case 'ฏ':
                return new char[] {'ฏ'};
            case 'ฐ':
                return new char[] {'ฐ','ถ','ท','ษ'};
            /* case 'ฑ':
                return new char[] {'ฑ','ท','ห','ซ'};*/
            case 'ฒ':
                return new char[] {'ฒ'};
            case 'ณ':
                return new char[] {'ณ','น','ญ'};
            case 'ด':
                return new char[] {'ด','ค','ก','ท','ธ','จ','ฐ','ฒ','ต','ถ','ศ','ส','ษ','บ','ป','ข','ช','พ','ฬ','ง','น','ม','ห','ฏ','ย','ว'};
            case 'ต':
                return new char[] {'ต','จ','ท','ธ','ฒ','ด','ถ','ศ','ส','ษ','บ','น','พ','ฏ','ช'};
            case 'ถ':
                return new char[] {'ถ','ภ','ท','ธ','ค','ฒ','ต','ศ','ฐ','ผ'};
            case 'ท':
                return new char[] {'ท','ธ','ต','ฒ','ถ','ภ','จ','ด','ต','ส','ฐ','ผ','ศ'};
            case 'ธ':
                return new char[] {'ธ','ช','ท','ฒ'};
            case 'น':
                return new char[] {'น','จ','ณ','ด','ม','ก','ฒ','ศ','ส','ค','บ','ป','ฬ','ข','ช','พ','ง','ญ','ร','ล','ย','ว','ท','ธ','ฏ','ฐ'};
            case 'บ':
                return new char[] {'บ','ข','ภ','ม','ย','ก','ด','ศ','ป','พ','ง','น','ร','ล','จ'};
            case 'ป':
                return new char[] {'ม','บ','ภ','ย','ป','พ'};
            case 'ผ':
                return new char[] {'ผ','ภ','พ'};
            case 'ฝ':
                return new char[] {'ฝ'};
            case 'พ':
                return new char[] {'ท','ธ','ผ','ภ','ป','พ'};
            case 'ฟ':
                return new char[] {'ภ','ฝ','ฟ'};
            case 'ภ':
                return new char[] {'ภ','ถ','ผ','พ'};
            case 'ม':
                return new char[] {'ม','ข','ณ','ญ','น','ย','ก','ด','บ','ป','พ','ง','ร','ว'};
            case 'ย':
                return new char[] {'ย','ช','น','ก','ว','ญ','ร','จ','ง'};
            case 'ร':
                return new char[] {'ร','ด','ล','ว','ญ','ณ','ศ','ส','น','ม','ฬ'};
            case 'ล':
                return new char[] {'ล','ช','ท','ร','ว','ส','อ','น','ฬ'};
            case 'ว':
                return new char[] {'ว','อ'};
            case 'ศ':
                return new char[] {'ศ','ษ','ส','ถ','ต','ท'};
            case 'ษ':
                return new char[] {'ส','ษ','ศ'};
            case 'ส':
                return new char[] {'ส','ศ','ษ','ซ','ล','ช','ด','ธ'};
            case 'ห':
                return new char[] {'ห','ง','ม'};
            case 'ฬ':
                return new char[] {'ฬ'};
            case 'อ':
                return new char[] {'อ','ห'};
            /* case 'ฮ':
                return new char[] {'ฮ','อ','ฬ'};
            case 'ฯ':
                return new char[] {'ฯ','ษ'};*/
            case 'ฤ':
                return new char[] {'ฤ','ฦ','ึ','ื'};
            case 'ฦ':
                return new char[] {'ฤ'};
            case 'ะ':
                return new char[] {'ะ','า','ั'};
            case 'ั':
                return new char[] {'ั','ำ','ะ'};
            case 'า':
                return new char[] {'า','ะ','ำ','ั','ไ'};
            case 'ำ':
                return new char[] {'ำ','ั'};
            case 'ิ':
                return new char[] {'ิ','ี'};
            case 'ี':
                return new char[] {'ิ','ื','ี'};
            case 'ึ':
                return new char[] {'ึ','ื','ิ','ฤ'};
            case 'ื':
                return new char[] {'ี','ึ','ื','ิ','ฤ'};
            case 'ุ':
                return new char[] {'ุ','ู'};
            case 'ู':
                return new char[] {'ุ','ู'};
            case 'แ':
                return new char[] {'แ','า'};
            case 'เ':
                return new char[] {'เ','แ'};
            case 'โ':
                return new char[] {'โ'};
               /* String[] sArray = {"โ"," "};

                for (String n:sArray)
                    s+= n;
                c = s.toCharArray();

                return c;*/
            case 'ใ':
                return new char[] {'ใ','ไ','ั'};
            case 'ไ':
                return new char[] {'ไ','โ','ี','ั','ใ'};
            /*case 'ๆ':
                return new char[] {'ๆ'};
            case '็':
                return new char[] {'็','๊','์','้'};*/
            case '่':
                return new char[] {'่','้'};
            case '้':
                return new char[] {'่','้'};
            case '๊':
                return new char[] {'๊','๋'};
            case '๋':
                return new char[] {'๋'};
            /* case '์':
                return new char[] {'์','้','็','๊'};*/
            default:
                 return new char[] {wordArray};
        }
    }

    public static char [] ThaiVowelMapping(char wordArray)
    {
        String s = "";
        char[] c;

        switch (wordArray)
        {
            case 'ฤ':
                return new char[] {'ฤ','ฦ','ึ','ื'};
            case 'ฦ':
                return new char[] {'ฤ'};
            case 'ะ':
                return new char[] {'ะ','า','ั'};
            case 'ั':
                return new char[] {'ั','ำ','ะ'};
            case 'า':
                return new char[] {'า','ะ','ำ','ั','ไ'};
            case 'ำ':
                return new char[] {'ำ','ั'};
            case 'ิ':
                return new char[] {'ิ','ี'};
            case 'ี':
                return new char[] {'ิ','ื','ี'};
            case 'ึ':
                return new char[] {'ึ','ื','ิ','ฤ'};
            case 'ื':
                return new char[] {'ี','ึ','ื','ิ','ฤ'};
            case 'ุ':
                return new char[] {'ุ','ู'};
            case 'ู':
                return new char[] {'ุ','ู'};
            case 'แ':
                return new char[] {'แ','า'};
            case 'เ':
                return new char[] {'เ','แ'};
            case 'โ':
                return new char[] {'โ'};
            case 'ใ':
                return new char[] {'ใ','ไ','ั'};
            case 'ไ':
                return new char[] {'ไ','โ','ี','ั','ใ'};
            default:
                return new char[] {wordArray};
        }
    }
}
