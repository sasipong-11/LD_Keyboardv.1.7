package th.or.nectec.twskeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;


/**
 * Created by NTemp on 18/08/2016.
 */
public class DictionaryAdapter extends BaseAdapter {

    private Context ctx;
    public static ArrayList<Vocab> arrayMeaning;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;

    public DictionaryAdapter(Context context, ArrayList<Vocab> arr) {
        ctx = context;
        arrayMeaning = arr;
        inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return arrayMeaning.size();

    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ListViewHolder holder;

        sharedPreferences = ctx.getSharedPreferences("th.or.nectec.twskeyboard", Context.MODE_PRIVATE);




        if (convertView == null) {
            holder = new ListViewHolder();
            convertView = inflater.inflate(R.layout.dictionary_adapter, null);

            holder.number = (TextView) convertView.findViewById(R.id.sequence);
            holder.thaiHeader = (TextView) convertView.findViewById(R.id.thaiHeader);
            holder.engHeader = (TextView) convertView.findViewById(R.id.englishHeader);
            holder.wordHeader = (TextView) convertView.findViewById(R.id.wordTxt);
            holder.thai = (TextView) convertView.findViewById(R.id.thaiTxt);
            holder.eng = (TextView)convertView.findViewById(R.id.engTxt);
            holder.speakThai = (ImageButton)convertView.findViewById(R.id.speakThai);
            holder.speakEnglish = (ImageButton)convertView.findViewById(R.id.speakEnglish);

            //SetFont
            //Typeface typeFace = Typeface.createFromAsset(ctx.getAssets(), sharedPreferences.getString("font","fonts/CSChatThaiUI.ttf"));
            Typeface typeFace = Typeface.createFromAsset(ctx.getAssets(), "fonts/layijimahaniyom.ttf");

            //Typeface font_sarabun_bold = Typeface.createFromAsset(getAssets(), "fonts/thsarabunnew_bold.ttf");
            //paint.setTypeface(Typeface.create(font_sarabun,Typeface.BOLD));


            holder.number.setTypeface(typeFace.create(typeFace,Typeface.BOLD));
            holder.thaiHeader.setTypeface(typeFace);
            holder.engHeader.setTypeface(typeFace);
            holder.wordHeader.setTypeface(typeFace.create(typeFace,Typeface.BOLD));
            holder.thai.setTypeface(typeFace);
            holder.eng.setTypeface(typeFace);

            //setColor
            int theme = sharedPreferences.getInt("theme",3);
            int color;
            int speckerColor;
            if(theme == 1){
                color = ctx.getResources().getColor(R.color.greenTheme);
                speckerColor = ctx.getResources().getColor(R.color.greenActionBar);
            }else if(theme == 2){
                color = ctx.getResources().getColor(R.color.pinkTheme);
                speckerColor = ctx.getResources().getColor(R.color.redActionBar);
            }else{
                color = ctx.getResources().getColor(R.color.blueTheme);
                speckerColor = ctx.getResources().getColor(R.color.blueActionBar);
            }
            holder.number.setTextColor(color);
            holder.thaiHeader.setTextColor(color);
            holder.engHeader.setTextColor(color);
            holder.wordHeader.setTextColor(color);
            holder.speakThai.setColorFilter(speckerColor);
            holder.speakEnglish.setColorFilter(speckerColor);

            convertView.setTag(holder);
        } else {
            holder = (ListViewHolder) convertView.getTag();
        }

        holder.number.setText("" + (position+1) + ". ");

        holder.tts = new TextToSpeech(ctx, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    holder.tts.setLanguage(new Locale("th"));
                    holder.tts.setPitch(sharedPreferences.getInt("pitch",100)/100f);
                    holder.tts.setSpeechRate(sharedPreferences.getInt("speech",100)/100f);

                }
            }
        },"com.google.android.tts");

        /*if(TWSActivity.isEnglish){
            holder.thaiHeader.setVisibility(View.INVISIBLE);
            holder.thaiHeader.setVisibility(View.GONE);
            holder.thai.setVisibility(View.INVISIBLE);
            holder.thai.setVisibility(View.GONE);
            holder.speakThai.setVisibility(View.INVISIBLE);
            holder.speakThai.setVisibility(View.GONE);
            holder.engHeader.setText("ไทย");
        }else{
            holder.thaiHeader.setText("ไทย");
            holder.engHeader.setText("อังกฤษ");
        }*/

        holder.thaiHeader.setText(" ไทย");
        holder.engHeader.setText(" อังกฤษ");


        holder.speakThai.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    holder.speakThai.setBackgroundColor(Color.parseColor("#b8e0ef"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    holder.speakThai.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });


        holder.speakEnglish.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    holder.speakEnglish.setBackgroundColor(Color.parseColor("#b8e0ef"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    holder.speakEnglish.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });

        if (arrayMeaning.get(position).getWord() != null) {


            String pertOfSpeechTH = "";

            String part = arrayMeaning.get(position).getPartOfSpeech();
            if(part.equals("N")){
                pertOfSpeechTH = "คำนาม";
            }else if(part.equals("V")){
                pertOfSpeechTH = "คำกิริยา";
            }else if(part.equals("ADV")){
                pertOfSpeechTH = "ขยายกิริยา";
            }else if(part.equals("ADJ")){
                pertOfSpeechTH = "ขยายนาม";
            }else if(part.equals("CLAS")){
                pertOfSpeechTH = "ลักษณะนาม";
            }else if(part.equals("CONJ")){
                pertOfSpeechTH = "คำเชื่อม";
            }else if(part.equals("INT")){
                pertOfSpeechTH = "คำอุทาน";
            }else if(part.equals("PRON")){
                pertOfSpeechTH = "คำสรรพนาม";
            }else if(part.equals("VI")){
                pertOfSpeechTH = "อกรรมกิริยา";
            }else if(part.equals("VT")){
                pertOfSpeechTH = "สกรรมกิริยา";
            }else{
                pertOfSpeechTH = part;
            }

            holder.wordHeader.setText(" " + arrayMeaning.get(position).getWord() + " (" + pertOfSpeechTH + ") ");
        }

        if(arrayMeaning.get(position).getThaiTrans() != null){
            holder.thai.setText(arrayMeaning.get(position).getThaiTrans());
            holder.speakThai.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  /*  holder.tts = new TextToSpeech(ctx, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status != TextToSpeech.ERROR) {
                                holder.tts.setLanguage(new Locale("th"));
                                holder.tts.setPitch(sharedPreferences.getInt("pitch",100)/100f);
                                holder.tts.setSpeechRate(sharedPreferences.getInt("speech",100)/100f);
                               // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    holder.tts.speak(arrayMeaning.get(position).getThaiTrans(), TextToSpeech.QUEUE_FLUSH, null, null);
                               // }
                            }
                        }
                    });*/

                    //holder.tts.speak(arrayMeaning.get(position).getThaiTrans(), TextToSpeech.QUEUE_FLUSH, null, null);
                   // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.tts.speak(arrayMeaning.get(position).getThaiTrans(), TextToSpeech.QUEUE_FLUSH, null, null);
                   // }else {
                   //     holder.tts.speak(arrayMeaning.get(position).getThaiTrans(), TextToSpeech.QUEUE_FLUSH, null);
                   // }
                }
            });
        }
        if(arrayMeaning.get(position).getEngTrans() !=null){
            holder.eng.setText(arrayMeaning.get(position).getEngTrans());
            holder.speakEnglish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*holder.tts = new TextToSpeech(ctx, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status != TextToSpeech.ERROR) {*/
                                /*if(TWSActivity.isEnglish){
                                    holder.tts.setLanguage(new Locale("th"));
                                }else{
                                    holder.tts.setLanguage(Locale.UK);
                                }*/
                               /* holder.tts.setLanguage(new Locale("th"));

                                holder.tts.setPitch(sharedPreferences.getInt("pitch",100)/100f);
                                holder.tts.setSpeechRate(sharedPreferences.getInt("speech",100)/100f);
                                   holder.tts.speak(arrayMeaning.get(position).getEngTrans(), TextToSpeech.QUEUE_FLUSH, null, null);

                            }
                        }
                    });*/

                    //holder.tts.speak(arrayMeaning.get(position).getEngTrans(), TextToSpeech.QUEUE_FLUSH, null, null);
                   // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.tts.speak(arrayMeaning.get(position).getEngTrans(), TextToSpeech.QUEUE_FLUSH, null, null);
                   // }else {
                   //     holder.tts.speak(arrayMeaning.get(position).getEngTrans(), TextToSpeech.QUEUE_FLUSH, null);
                   // }
                }
            });
        }

        return convertView;
    }

    private class ListViewHolder {
        private TextView number;
        private TextView thaiHeader;
        private TextView engHeader;
        private TextView wordHeader;
        private TextView thai;
        private TextView eng;
        private ImageButton speakThai;
        private ImageButton speakEnglish;
        private TextToSpeech tts;
    }

}
