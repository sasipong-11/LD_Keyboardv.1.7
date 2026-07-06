/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package th.or.nectec.twskeyboard;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;
import java.text.BreakIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import th.or.nectec.twskeyboard.editdistance.Distance;
import th.or.nectec.twskeyboard.ldrule.AddMissingChar;
import th.or.nectec.twskeyboard.ldrule.LDRule;
import th.or.nectec.twskeyboard.soundex.Soundex;
import th.or.nectec.twskeyboard.soundex.SoundexWord;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class SoftKeyboard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener, SpellCheckerSession.SpellCheckerSessionListener {
    static final boolean DEBUG = false;

    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;
    private int paging = 0;
    private int countStep1 = 0;
    private int countStep2 = 0;
    private int countStep3 = 0;
    private int countStep4 = 0;

    private int totalPage;
    private boolean isThaikey = true;

    private InputMethodManager mInputMethodManager;

    private LatinKeyboardView mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;

    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;

    private LatinKeyboard mSymbolsKeyboard;
    private LatinKeyboard mSymbolsShiftedKeyboard;
    private LatinKeyboard mQwertyKeyboard;
    private LatinKeyboard mQwertyKeyboardShift;
    private LatinKeyboard mQwertyKeyboardThai;
    private LatinKeyboard mQwertyKeyboardThaiShift;

    private LatinKeyboard mCurKeyboard;

    private String mWordSeparators;

    private List<String> mSuggestions;
    TextToSpeech tts;

    ArrayList<String> wordPredictList;
    ArrayList<String> firstLD;
    AddMissingChar missChar;
    BreakIterator boundary;
    ProgressDialog progress;
    Soundex soundex;
    SoundexWord soundexWord;

    RelativeLayout candidateWordLayout;
    RelativeLayout functionLayout;
    RelativeLayout onKeyLayout;
    RelativeLayout dictionaryLayout;
    RelativeLayout progressLayout;
    RelativeLayout lineKbLayout;

    ListView wordList;
    TextView selectedWordtxt;
    TextView onKeyTxt;
    TextView progressTxt;
    ImageButton speakBtn;
    ImageButton dictBtn;
    ImageButton commitTextBtn;
    ImageButton prevBtn;
    ImageButton nextBtn;
    ImageButton closeDictBtn;
    Button word01Btn;
    Button word02Btn;
    Button word03Btn;
    Button word04Btn;
    Button word05Btn;
    Button word06Btn;
    ProgressBar progressBar;

    double screenInches;
    TableRow.LayoutParams params;

    SharedPreferences sharedPreferences;
    LayoutInflater inflater;
    View view;

    Paint paint;

    private int mProgressStatus = 0;
    ProgressDialog mProgressDialog;
    Tracker mTracker;

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        inflater = getLayoutInflater();

        view = inflater.inflate(getResources().getLayout(R.layout.candidate_view), null, false);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        sharedPreferences = this.getSharedPreferences("th.or.nectec.twskeyboard", Context.MODE_PRIVATE);
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);

        paint = new Paint();

        Locale thaiLocale = new Locale("th");
        boundary = BreakIterator.getWordInstance(thaiLocale);   //word segment

        wordPredictList = new ArrayList<String>();
        soundex = new Soundex(this);
        soundexWord = new SoundexWord(this);
        missChar = new AddMissingChar(this);
        //  progress = new ProgressDialog(this);

        //  progress.setTitle("กำลังค้นหาคำศัพท์");
//       progress.setMessage("กรุณารอสักครู่...");

        //Copy Database from Assets
        try {
            CopyDatabase abc = new CopyDatabase("spell_sys.db", "dictionary.db");
            abc.copyFile(this);

            CopyDatabase def = new CopyDatabase("lexitron_v3.db", "lexitron.db");
            def.copyFile(this);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override
    public void onInitializeInterface() {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        screenInches = Math.sqrt(x + y);

        //Toast.makeText(view.getContext(),"screenInches: "+screenInches,Toast.LENGTH_SHORT).show();

        if (mQwertyKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }

        mQwertyKeyboard = new LatinKeyboard(this, R.xml.qwerty);
        mQwertyKeyboardShift = new LatinKeyboard(this, R.xml.qwerty_shift);
        mQwertyKeyboardThai = new LatinKeyboard(this, R.xml.qwerty_thai);
        mQwertyKeyboardThaiShift = new LatinKeyboard(this, R.xml.qwerty_thai_shift);
        mSymbolsKeyboard = new LatinKeyboard(this, R.xml.symbols);
        mSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbols_shift);
    }

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override
    public View onCreateInputView() {

        mInputView = (LatinKeyboardView) getLayoutInflater().inflate(R.layout.input, null, false);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setPreviewEnabled(true);

        int bgTheme = sharedPreferences.getInt("bgTheme", R.drawable.theme_img11);
        mInputView.setBackgroundResource(bgTheme);
        if (isThaikey) {
            setLatinKeyboard(mQwertyKeyboardThai);
        } else {
            setLatinKeyboard(mQwertyKeyboard);
        }
        mInputView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    beep();
                }
                return false;
            }
        });

        return mInputView;
    }

    private void setLatinKeyboard(LatinKeyboard nextKeyboard) {

        final boolean shouldSupportLanguageSwitchKey = true;
        //mInputMethodManager.shouldOfferSwitchingToNextInputMethod(getToken());
        nextKeyboard.setLanguageSwitchKeyVisibility(shouldSupportLanguageSwitchKey);
        mInputView.setKeyboard(nextKeyboard);
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    private void onTracker(String screenName) {
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    @Override
    public View onCreateCandidatesView() {

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(new Locale("th"));
                }
            }
        }, "com.google.android.tts");


        Typeface font_CSChatThaiUI = Typeface.createFromAsset(getAssets(), "fonts/CSChatThaiUI.ttf");
        paint.setTypeface(Typeface.create(font_CSChatThaiUI, Typeface.NORMAL));

        Typeface font_CSChatThaiUI_bold = Typeface.createFromAsset(getAssets(), "fonts/CSChatThaiUI.ttf");
        paint.setTypeface(Typeface.create(font_CSChatThaiUI_bold, Typeface.BOLD));

        Typeface font_Mahaniyom = Typeface.createFromAsset(getAssets(), "fonts/layijimahaniyom.ttf");
        paint.setTypeface(Typeface.create(font_Mahaniyom, Typeface.NORMAL));

        LinearLayout parent = new LinearLayout(this);
        parent.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);

        wordList = (ListView) view.findViewById(R.id.wordList);
        selectedWordtxt = (TextView) view.findViewById(R.id.selectedWordTxt);
        onKeyTxt = (TextView) view.findViewById(R.id.onKeyTxt);   //show typing tab
        progressTxt = (TextView) view.findViewById(R.id.progressTxt);
        speakBtn = (ImageButton) view.findViewById(R.id.speakBtn);
        dictBtn = (ImageButton) view.findViewById(R.id.dictBtn);
        commitTextBtn = (ImageButton) view.findViewById(R.id.commitTextBtn);
        prevBtn = (ImageButton) view.findViewById(R.id.prevBtn);
        nextBtn = (ImageButton) view.findViewById(R.id.nextBtn);
        closeDictBtn = (ImageButton) view.findViewById(R.id.closeDictBtn);
        word01Btn = (Button) view.findViewById(R.id.word01Btn);
        word02Btn = (Button) view.findViewById(R.id.word02Btn);
        word03Btn = (Button) view.findViewById(R.id.word03Btn);
        word04Btn = (Button) view.findViewById(R.id.word04Btn);
        word05Btn = (Button) view.findViewById(R.id.word05Btn);
        word06Btn = (Button) view.findViewById(R.id.word06Btn);

        progressBar = (ProgressBar) view.findViewById(R.id.indeterminateBar);


        candidateWordLayout = (RelativeLayout) view.findViewById(R.id.candidateWordLayout);
        functionLayout = (RelativeLayout) view.findViewById(R.id.funcLayout);
        onKeyLayout = (RelativeLayout) view.findViewById(R.id.onKeyLayout);
        dictionaryLayout = (RelativeLayout) view.findViewById(R.id.dictionaryLayout);
        progressLayout = (RelativeLayout) view.findViewById(R.id.progressLayout);
        lineKbLayout = (RelativeLayout) view.findViewById(R.id.lineKbLayout);

        progressTxt.setTypeface(font_Mahaniyom);
        onKeyTxt.setTypeface(font_CSChatThaiUI);
        word01Btn.setTypeface(font_CSChatThaiUI);
        word02Btn.setTypeface(font_CSChatThaiUI);
        word03Btn.setTypeface(font_CSChatThaiUI);
        word04Btn.setTypeface(font_CSChatThaiUI);
        word05Btn.setTypeface(font_CSChatThaiUI);
        word06Btn.setTypeface(font_CSChatThaiUI);

        selectedWordtxt.setTypeface(font_CSChatThaiUI);

        candidateWordLayout.setVisibility(View.INVISIBLE);
        candidateWordLayout.setVisibility(View.GONE);
        functionLayout.setVisibility(view.INVISIBLE);
        functionLayout.setVisibility(view.GONE);
        onKeyLayout.setVisibility(View.INVISIBLE);
        onKeyLayout.setVisibility(View.GONE);
        dictionaryLayout.setVisibility(View.INVISIBLE);
        dictionaryLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.INVISIBLE);
        progressLayout.setVisibility(View.GONE);
        lineKbLayout.setVisibility(View.INVISIBLE);
        lineKbLayout.setVisibility(View.GONE);

        prevBtn.setImageResource(R.drawable.ic_prev_word);
        nextBtn.setImageResource(R.drawable.ic_next_word);
        speakBtn.setImageResource(R.drawable.ic_tts);
        dictBtn.setImageResource(R.drawable.ic_dictionary);
        commitTextBtn.setImageResource(R.drawable.ic_select_word);
        closeDictBtn.setImageResource(R.drawable.ic_close);

        speakBtn.setOnTouchListener(new ImageButtonHighlighterOnTouchListener(speakBtn));
        dictBtn.setOnTouchListener(new ImageButtonHighlighterOnTouchListener(dictBtn));
        commitTextBtn.setOnTouchListener(new ImageButtonHighlighterOnTouchListener(commitTextBtn));
        word01Btn.setOnTouchListener(new ButtonHighlighterOnTouchListener(word01Btn, 3));
        word02Btn.setOnTouchListener(new ButtonHighlighterOnTouchListener(word02Btn, 3));
        word03Btn.setOnTouchListener(new ButtonHighlighterOnTouchListener(word03Btn, 3));
        word04Btn.setOnTouchListener(new ButtonHighlighterOnTouchListener(word04Btn, 3));
        word05Btn.setOnTouchListener(new ButtonHighlighterOnTouchListener(word05Btn, 3));
        word06Btn.setOnTouchListener(new ButtonHighlighterOnTouchListener(word06Btn, 3));

        prevBtn.setOnTouchListener(new ImageButtonHighlighterOnTouchListener(prevBtn));
        nextBtn.setOnTouchListener(new ImageButtonHighlighterOnTouchListener(nextBtn));
        closeDictBtn.setOnTouchListener(new ImageButtonHighlighterOnTouchListener(closeDictBtn));

        selectedWordtxt.setText("");

        params = new TableRow.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.weight = 1f;
        params.leftMargin = 3;
        params.rightMargin = 3;
        params.topMargin = 3;
        params.bottomMargin = 3;
        if (screenInches < 7.5) {
            params.height = 80;
        } else {
            params.height = 120;
        }
        Button button = new Button(this);

        if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {

            word01Btn.setLayoutParams(params);
            word02Btn.setLayoutParams(params);
            word04Btn.setLayoutParams(params);
            word05Btn.setLayoutParams(params);

            word03Btn.setVisibility(View.GONE);
            word06Btn.setVisibility(View.GONE);
        } else {

            word01Btn.setLayoutParams(params);
            word02Btn.setLayoutParams(params);
            word03Btn.setLayoutParams(params);
            word04Btn.setLayoutParams(params);
            word05Btn.setLayoutParams(params);
            word06Btn.setLayoutParams(params);
        }

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");
                if (paging < totalPage - 1) {
                    paging++;
                    countStep4++;
                    Log.d("Test", "step4: Click nextBtn count" + countStep4);
                    if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {
                        setCandidateWord4(paging, wordPredictList);
                    } else {
                        setCandidateWord(paging, wordPredictList);
                    }

                } else {
                    Toast.makeText(view.getContext(), "ไม่มีรายการคำศัพท์ถัดไป", Toast.LENGTH_SHORT).show();
                }
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                if (paging > 0) {
                    paging--;
                    countStep3++;

                    Log.d("Test", "step3: Click prevBtn count" + countStep3);

                    if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {
                        setCandidateWord4(paging, wordPredictList);

                    } else {
                        setCandidateWord(paging, wordPredictList);
                    }
                } else {
                    Toast.makeText(view.getContext(), "ไม่มีรายการคำศัพท์ก่อนหน้า", Toast.LENGTH_SHORT).show();
                }
            }
        });

        speakBtn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(selectedWordtxt.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                // }else {
                //     tts.speak(selectedWordtxt.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                // }


            }
        });
        dictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);

                    word03Btn.setVisibility(View.GONE);
                    word06Btn.setVisibility(View.GONE);
                } else {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word03Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);
                    word06Btn.setLayoutParams(params);
                }

                String word = selectedWordtxt.getText().toString();
                ArrayList<Vocab> meaning = findMeaning(word);

                if (meaning.size() > 0) {
                    dictionaryLayout.setVisibility(View.VISIBLE);
                    DictionaryAdapter adt = new DictionaryAdapter(getApplicationContext(), meaning);
                    wordList.setAdapter(adt);
                } else {
                    onKeyTxt.setText(" ไม่พบความหมาย \"" + word + "\"");
                }
            }
        });
        commitTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                countStep1 = 0;
                countStep2 = 0;
                countStep3 = 0;
                countStep4 = 0;
                Log.d("Test", "step5:reset count commit click");


                pickSuggestion(selectedWordtxt.getText().toString());

            }
        });

        word01Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                countStep1 = 0;
                countStep2 = 0;
                countStep3 = 0;
                countStep4 = 0;
                Log.d("Test", "step5:reset count word01Btn click");
                pickSuggestion(word01Btn.getText().toString());

            }
        });
        word01Btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onTracker("Keyboard Activity");

                displayOption();
                selectedWordtxt.setText(word01Btn.getText().toString());

                if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);

                    word03Btn.setVisibility(View.GONE);
                    word06Btn.setVisibility(View.GONE);
                } else {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word03Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);
                    word06Btn.setLayoutParams(params);
                }

                return true;
            }
        });

        word02Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                countStep1 = 0;
                countStep2 = 0;
                countStep3 = 0;
                countStep4 = 0;
                Log.d("Test", "step5:reset count word02Btn click");
                pickSuggestion(word02Btn.getText().toString());
            }
        });
        word02Btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onTracker("Keyboard Activity");

                displayOption();
                selectedWordtxt.setText(word02Btn.getText().toString());

                if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);

                    word03Btn.setVisibility(View.GONE);
                    word06Btn.setVisibility(View.GONE);
                } else {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word03Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);
                    word06Btn.setLayoutParams(params);
                }

                return true;
            }
        });

        word03Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                countStep1 = 0;
                countStep2 = 0;
                countStep3 = 0;
                countStep4 = 0;
                Log.d("Test", "step5:reset count word03Btn click");
                pickSuggestion(word03Btn.getText().toString());
            }
        });
        word03Btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onTracker("Keyboard Activity");

                displayOption();
                selectedWordtxt.setText(word03Btn.getText().toString());

                if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);

                    word03Btn.setVisibility(View.GONE);
                    word06Btn.setVisibility(View.GONE);
                } else {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word03Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);
                    word06Btn.setLayoutParams(params);
                }

                return true;
            }
        });

        word04Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                countStep1 = 0;
                countStep2 = 0;
                countStep3 = 0;
                countStep4 = 0;
                Log.d("Test", "step5:reset count word04Btn click");
                pickSuggestion(word04Btn.getText().toString());
            }
        });
        word04Btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onTracker("Keyboard Activity");

                displayOption();
                selectedWordtxt.setText(word04Btn.getText().toString());

                if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);

                    word03Btn.setVisibility(View.GONE);
                    word06Btn.setVisibility(View.GONE);
                } else {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word03Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);
                    word06Btn.setLayoutParams(params);
                }
                return true;
            }
        });

        word05Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                countStep1 = 0;
                countStep2 = 0;
                countStep3 = 0;
                countStep4 = 0;
                Log.d("Test", "step5:reset count word05Btn click");
                pickSuggestion(word05Btn.getText().toString());
            }
        });
        word05Btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onTracker("Keyboard Activity");

                displayOption();
                selectedWordtxt.setText(word05Btn.getText().toString());
                if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);

                    word03Btn.setVisibility(View.GONE);
                    word06Btn.setVisibility(View.GONE);
                } else {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word03Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);
                    word06Btn.setLayoutParams(params);
                }
                return true;
            }
        });


        word06Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                countStep1 = 0;
                countStep2 = 0;
                countStep3 = 0;
                countStep4 = 0;
                Log.d("Test", "step5:reset count word06Btn click");
                pickSuggestion(word06Btn.getText().toString());
            }
        });
        word06Btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onTracker("Keyboard Activity");

                displayOption();
                selectedWordtxt.setText(word06Btn.getText().toString());
                if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);

                    word03Btn.setVisibility(View.GONE);
                    word06Btn.setVisibility(View.GONE);
                } else {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word03Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);
                    word06Btn.setLayoutParams(params);
                }
                return true;
            }
        });

        closeDictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTracker("Keyboard Activity");

                dictionaryLayout.setVisibility(View.INVISIBLE);
                dictionaryLayout.setVisibility(View.GONE);

                if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);

                    word03Btn.setVisibility(View.GONE);
                    word06Btn.setVisibility(View.GONE);
                } else {

                    word01Btn.setLayoutParams(params);
                    word02Btn.setLayoutParams(params);
                    word03Btn.setLayoutParams(params);
                    word04Btn.setLayoutParams(params);
                    word05Btn.setLayoutParams(params);
                    word06Btn.setLayoutParams(params);
                }
            }
        });

        onKeyTxt.setText(mComposing);

        if (view.getParent() != null)
            ((ViewGroup) view.getParent()).removeView(view); // <- fix flipping

        parent.addView(view);

        return parent;
    }

    private void displayOption() {
        onKeyTxt.setText(mComposing.toString());
        functionLayout.setVisibility(View.VISIBLE);
        dictionaryLayout.setVisibility(View.INVISIBLE);
        dictionaryLayout.setVisibility(View.GONE);


    }

    private void setCandidateWord(int paging, ArrayList<String> wordList) {

        int firstIndex = paging * 6;
        int lastIndex = firstIndex + 6;
        lastIndex = Math.min(lastIndex, wordList.size());

        /*TableRow.LayoutParams params = new TableRow.LayoutParams(
                LayoutParams.MATCH_PARENT,80);
        params.weight = 1.0f;
        params.leftMargin = 3;
        params.rightMargin = 3;
        params.topMargin = 3;
        params.bottomMargin = 3;
        Button button = new Button(this);

        word01Btn.setLayoutParams(params);
        word02Btn.setLayoutParams(params);
        word03Btn.setLayoutParams(params);
        word04Btn.setLayoutParams(params);
        word05Btn.setLayoutParams(params);
        word06Btn.setLayoutParams(params);*/
        try {
            word01Btn.setVisibility(View.VISIBLE);
            word01Btn.setText(wordList.get(firstIndex));
            word01Btn.setBackgroundColor(getResources().getColor(R.color.keyNormal));
            word01Btn.setOnTouchListener(new ButtonHighlighterOnTouchListener(word01Btn, 3));
            firstIndex++;

            if (firstIndex < lastIndex) {
                word02Btn.setVisibility(View.VISIBLE);
                word02Btn.setText(wordList.get(firstIndex));
                firstIndex++;
                if (firstIndex < lastIndex) {
                    word03Btn.setVisibility(View.VISIBLE);
                    word03Btn.setText(wordList.get(firstIndex));
                    firstIndex++;
                    if (firstIndex < lastIndex) {
                        word04Btn.setVisibility(View.VISIBLE);
                        word04Btn.setText(wordList.get(firstIndex));
                        firstIndex++;
                        if (firstIndex < lastIndex) {
                            word05Btn.setVisibility(View.VISIBLE);
                            word05Btn.setText(wordList.get(firstIndex));
                            firstIndex++;
                            if (firstIndex < lastIndex) {
                                word06Btn.setVisibility(View.VISIBLE);
                                word06Btn.setText(wordList.get(firstIndex));
                            } else {
                                word06Btn.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            word05Btn.setVisibility(View.INVISIBLE);
                            word06Btn.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        word04Btn.setVisibility(View.INVISIBLE);
                        word05Btn.setVisibility(View.INVISIBLE);
                        word06Btn.setVisibility(View.INVISIBLE);
                    }
                } else {
                    word03Btn.setVisibility(View.INVISIBLE);
                    word04Btn.setVisibility(View.INVISIBLE);
                    word05Btn.setVisibility(View.INVISIBLE);
                    word06Btn.setVisibility(View.INVISIBLE);
                }
            } else {
                word02Btn.setVisibility(View.INVISIBLE);
                word03Btn.setVisibility(View.INVISIBLE);
                word04Btn.setVisibility(View.INVISIBLE);
                word05Btn.setVisibility(View.INVISIBLE);
                word06Btn.setVisibility(View.INVISIBLE);
            }

        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(view.getContext(), "Error : " + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void setCandidateWord4(int paging, ArrayList<String> wordList) {

        int firstIndex = paging * 4;
        int lastIndex = firstIndex + 4;
        lastIndex = Math.min(lastIndex, wordList.size());

       /* TableRow.LayoutParams params = new TableRow.LayoutParams(
                LayoutParams.MATCH_PARENT,80);
        params.weight = 1f;
        params.leftMargin = 3;
        params.rightMargin = 3;
        params.topMargin = 3;
        params.bottomMargin = 3;
        Button button = new Button(this);

         word01Btn.setLayoutParams(params);
         word02Btn.setLayoutParams(params);
         word04Btn.setLayoutParams(params);
         word05Btn.setLayoutParams(params);

        word03Btn.setVisibility(View.GONE);
        word06Btn.setVisibility(View.GONE);*/

        try {

            word01Btn.setVisibility(View.VISIBLE);
            word01Btn.setText(wordList.get(firstIndex));
            word01Btn.setBackgroundColor(getResources().getColor(R.color.keyNormal));
            word01Btn.setOnTouchListener(new ButtonHighlighterOnTouchListener(word01Btn, 3));
            firstIndex++;

            if (firstIndex < lastIndex) {
                word02Btn.setVisibility(View.VISIBLE);
                word02Btn.setText(wordList.get(firstIndex));
                firstIndex++;
                if (firstIndex < lastIndex) {
                    word04Btn.setVisibility(View.VISIBLE);
                    word04Btn.setText(wordList.get(firstIndex));
                    firstIndex++;
                    if (firstIndex < lastIndex) {
                        word05Btn.setVisibility(View.VISIBLE);
                        word05Btn.setText(wordList.get(firstIndex));
                        firstIndex++;

                    } else {
                        word05Btn.setVisibility(View.INVISIBLE);

                    }
                } else {
                    word04Btn.setVisibility(View.INVISIBLE);
                    word05Btn.setVisibility(View.INVISIBLE);

                }
            } else {
                word02Btn.setVisibility(View.INVISIBLE);
                word04Btn.setVisibility(View.INVISIBLE);
                word05Btn.setVisibility(View.INVISIBLE);

            }

        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(view.getContext(), "Error : " + e, Toast.LENGTH_SHORT).show();
        }
    }


    class ButtonHighlighterOnTouchListener implements View.OnTouchListener {
        final Button button;
        int type;
        int actionDown;
        int actionUp;

        public ButtonHighlighterOnTouchListener(final Button button, int type) {
            super();
            this.button = button;
            this.type = type;
            actionDown = Color.parseColor("#89898a");
        }

        @Override
        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            if (type == 1) {//function button
                actionUp = Color.parseColor("#6eaca8");
            } else if (type == 2) {//user input button
                actionUp = getResources().getColor(R.color.funcKey);
            } else { //candidate word
                actionUp = getResources().getColor(R.color.keyNormal);
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                //grey color filter, you can change the color as you like
                button.setBackgroundColor(actionDown);
                beep();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                button.setBackgroundColor(actionUp);
            }
            return false;
        }
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        updateCandidates();

        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;
        Log.d("Test", "Set  mPredictionOn InputType");

        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                if (isThaikey) {
                    mCurKeyboard = mQwertyKeyboardThai;
                } else {
                    mCurKeyboard = mQwertyKeyboard;
                }


                mPredictionOn = true;

                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    mPredictionOn = false;

                }

                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == InputType.TYPE_TEXT_VARIATION_URI
                        || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are useful for e-mail addresses
                    // or URIs.
                    mPredictionOn = true;

                }

                if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mPredictionOn = true;
                    mCompletionOn = isFullscreenMode();

                }

                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;

            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                if (isThaikey) {
                    mCurKeyboard = mQwertyKeyboardThai;
                } else {
                    mCurKeyboard = mQwertyKeyboard;
                }

                updateShiftKeyState(attribute);
        }

        // Update the label on the enter key, depending on what the application
        // says it will do.
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override
    public void onFinishInput() {

        super.onFinishInput();

        // Clear current composing text and candidates.
        mComposing.setLength(0);

        updateCandidates();

        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        if (isThaikey) {
            mCurKeyboard = mQwertyKeyboardThai;
        } else {
            mCurKeyboard = mQwertyKeyboard;
        }

        if (mInputView != null) {
            mInputView.closing();
        }
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {

        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        setLatinKeyboard(mCurKeyboard);
        mInputView.closing();
        final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
        mInputView.setSubtypeOnSpaceKey(subtype);
        int bgTheme = sharedPreferences.getInt("bgTheme", R.drawable.theme_img11);
        mInputView.setBackgroundResource(bgTheme);
    }

    @Override
    public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) {
        mInputView.setSubtypeOnSpaceKey(subtype);
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.

        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {

            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {

        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }

            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < completions.length; i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {

        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        boolean dead = false;

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() - 1);
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length() - 1);
            }
        }

        onKey(c, null);

        return true;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;
            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                /*
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }*/
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.

        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {

        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    // Implementation of KeyboardViewListener

    public void onKey(int primaryCode, int[] keyCodes) {
        onTracker("LD Key(Search)");
        Log.d("Test", "KEYCODE: " + primaryCode);
        if (isWordSeparator(primaryCode)) {
            // Handle separator
            candidateWordLayout.setVisibility(View.INVISIBLE);
            candidateWordLayout.setVisibility(View.GONE);
            onKeyLayout.setVisibility(View.INVISIBLE);
            onKeyLayout.setVisibility(View.GONE);
            lineKbLayout.setVisibility(View.INVISIBLE);
            lineKbLayout.setVisibility(View.GONE);
            functionLayout.setVisibility(View.INVISIBLE);
            functionLayout.setVisibility(View.GONE);
            dictionaryLayout.setVisibility(View.INVISIBLE);
            dictionaryLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.INVISIBLE);
            progressLayout.setVisibility(View.GONE);

            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
           // if (!checkUnInstall()) {

                wordPredictList.clear();
                countStep1++;
                Log.d("Test", "step1: Click LD Keyboard count" + countStep1);
                paging = 0;


                if (mComposing.toString().length() > 20) {
                    Toast.makeText(this, "กรุณาค้นหาทีละคำ", Toast.LENGTH_LONG).show();
                } else {
                    if (mComposing.toString().length() > 1) {
                        //ArrayList<String> q_ = new DictionaryDatabaseHelper(getApplicationContext()).queryword1("BEST_SPELL_TH");
                        if (isInDictionary(mComposing.toString())) {

                            wordPredictList.clear();
                            wordPredictList.add(mComposing.toString());
                            ArrayList<String> similarWord = soundexWord.getSoundex(mComposing.toString());
                            wordPredictList.addAll(similarWord);

                            //Delete Repleting Word
                            LinkedHashSet<String> lhs = new LinkedHashSet<String>();
                            lhs.addAll(wordPredictList);
                            wordPredictList.clear();
                            wordPredictList.addAll(lhs);

                            //Calculate Total Page
                            candidateWordLayout.setVisibility(View.VISIBLE);
                            progressLayout.setVisibility(View.INVISIBLE);
                            progressLayout.setVisibility(View.GONE);

                            if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {
                                totalPage = wordPredictList.size() / 4;
                                if ((wordPredictList.size() % 4) != 0) {
                                    totalPage++;
                                }
                                setCandidateWord4(paging, wordPredictList);
                            } else {
                                totalPage = wordPredictList.size() / 6;
                                if ((wordPredictList.size() % 6) != 0) {
                                    totalPage++;
                                }
                                setCandidateWord(paging, wordPredictList);
                            }
                            //setFirstPageCandidateList(wordPredictList);

                        } else {
                            /** Show the progress dialog window */
                            new SearchEngingTask().execute(mComposing.toString());
                            countStep2++;
                            Log.d("Test", "step2 : ถูกใช้ LD rules " + countStep2);
                            progressLayout.setVisibility(View.VISIBLE);

                        }
                        //Toast.makeText(this,"G2P license has been expired",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "คำที่ใช้ค้นหาสั้นเกินไป", Toast.LENGTH_LONG).show();
                    }
                }

                //handleClose();
                return;
            //}
        } else if (primaryCode == LatinKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            Toast.makeText(this, "เปลี่ยนภาษา", Toast.LENGTH_SHORT).show();

            handleLanguageSwitch();

            return;
        } else if (primaryCode == LatinKeyboardView.KEYCODE_OPTIONS) {
            // Show a menu or somethin'
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && mInputView != null) {
            Keyboard current = mInputView.getKeyboard();
            /*if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
                if(isThaikey){
                    setLatinKeyboard(mQwertyKeyboardThai);
                }else{
                    setLatinKeyboard(mQwertyKeyboard);
                }
            } else {
                setLatinKeyboard(mSymbolsKeyboard);
                mSymbolsKeyboard.setShifted(false);
            }*/
            if (current == mSymbolsKeyboard) {
                setLatinKeyboard(mSymbolsShiftedKeyboard);
            } else {
                setLatinKeyboard(mSymbolsKeyboard);
            }

        } else if (primaryCode == LatinKeyboardView.KEYCODE_THAI_SHIFT) {
            Keyboard current = mInputView.getKeyboard();
            if (current == mQwertyKeyboardThai) {
                setLatinKeyboard(mQwertyKeyboardThaiShift);
            } else {
                setLatinKeyboard(mQwertyKeyboardThai);
            }

        } else if (primaryCode == LatinKeyboardView.KEYCODE_THAI_UNSHIFT) {
            setLatinKeyboard(mQwertyKeyboardThai);
        } else if (primaryCode == LatinKeyboardView.KEYCODE_ENG_SHIFT) {
            Keyboard current = mInputView.getKeyboard();
            if (current == mQwertyKeyboard) {
                setLatinKeyboard(mQwertyKeyboardShift);
            } else {
                setLatinKeyboard(mQwertyKeyboard);
            }
        } else if (primaryCode == LatinKeyboardView.KEYCODE_ENG) {
            setLatinKeyboard(mQwertyKeyboard);
        } else {
            onKeyLayout.setVisibility(View.VISIBLE);
            lineKbLayout.setVisibility(View.VISIBLE);
            handleCharacter(primaryCode, keyCodes);
        }
        candidateWordLayout.setVisibility(View.INVISIBLE);
        candidateWordLayout.setVisibility(View.GONE);
        functionLayout.setVisibility(View.INVISIBLE);
        functionLayout.setVisibility(View.GONE);
        dictionaryLayout.setVisibility(View.INVISIBLE);
        dictionaryLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.INVISIBLE);
        progressLayout.setVisibility(View.GONE);
    }

    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {
        Log.d("SoftKeyboard", "updateCandidates: " + mComposing.toString());

        if (onKeyTxt != null) {
            onKeyTxt.setText(mComposing);
        }
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();

                //Display string in candidate view when user typing
                list.add(mComposing.toString());
                Log.d("SoftKeyboard", "REQUESTING: " + mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }

    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {

        setCandidatesViewShown(true);
        mSuggestions = suggestions;
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }

    private void handleBackspace() {

        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();

        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();

        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }

        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        }/* else if (currentKeyboard == mSymbolsKeyboard) {

            mSymbolsKeyboard.setShifted(true);
            setLatinKeyboard(mSymbolsShiftedKeyboard);
            mSymbolsShiftedKeyboard.setShifted(true);
        }
        else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            setLatinKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
        }*/
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mInputView.isShifted() && !isThaikey) {
                primaryCode = Character.toUpperCase(AsciiCode.getChar(primaryCode));
            } else {
                primaryCode = AsciiCode.getChar(primaryCode);
            }
        }
        if (mPredictionOn) {
            mComposing.append(AsciiCode.getChar(primaryCode));
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates();

        } else {
            getCurrentInputConnection().commitText(
                    String.valueOf(AsciiCode.getChar(primaryCode)), 1);

        }
        if (mInputView.getKeyboard() == mQwertyKeyboardThaiShift) { //ถ้ากด shift อยู่
            mInputView.setKeyboard(mQwertyKeyboardThai);
            mCurKeyboard = mQwertyKeyboardThai;
        }
        if (mInputView.getKeyboard() == mQwertyKeyboardShift) { //ถ้ากด shift อยู่
            mInputView.setKeyboard(mQwertyKeyboard);
            mCurKeyboard = mQwertyKeyboard;
        }

    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }

    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    private void handleLanguageSwitch() {
        if (isThaikey) {
            setLatinKeyboard(mQwertyKeyboard);
            mCurKeyboard = mQwertyKeyboard;
            isThaikey = false;
        } else {
            setLatinKeyboard(mQwertyKeyboardThai);
            mCurKeyboard = mQwertyKeyboardThai;
            isThaikey = true;
        }
        //mInputMethodManager.switchToNextInputMethod(getToken(), false /* onlyCurrentIme */);
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char) code));
    }

    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
    }

    public void pickSuggestionManually(int index) throws IndexOutOfBoundsException {
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {

            if (mPredictionOn && mSuggestions != null && index >= 0) {
                mComposing.replace(0, mComposing.length(), mSuggestions.get(index));
            }
            commitTyped(getCurrentInputConnection());
        }
    }

    public void pickSuggestion(String word) throws IndexOutOfBoundsException {

        mComposing.replace(0, mComposing.length(), word);
        commitTyped(getCurrentInputConnection());
        functionLayout.setVisibility(View.INVISIBLE);
        functionLayout.setVisibility(View.GONE);
        candidateWordLayout.setVisibility(View.INVISIBLE);
        candidateWordLayout.setVisibility(View.GONE);
        onKeyLayout.setVisibility(View.INVISIBLE);
        onKeyLayout.setVisibility(View.GONE);
        lineKbLayout.setVisibility(View.INVISIBLE);
        lineKbLayout.setVisibility(View.GONE);
        dictionaryLayout.setVisibility(View.INVISIBLE);
        dictionaryLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.INVISIBLE);
        progressLayout.setVisibility(View.GONE);
        Toast.makeText(this, word, Toast.LENGTH_LONG).show();
    }

    public void swipeRight() {
        Log.d("SoftKeyboard", "Swipe right");
        if (mCompletionOn || mPredictionOn) {
            pickDefaultCandidate();
        }
    }

    public void swipeLeft() {
        Log.d("SoftKeyboard", "Swipe left");
        handleBackspace();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
    }

    public void onPress(int primaryCode) {

    }

    public void onRelease(int primaryCode) {

    }

    /**
     * http://www.tutorialspoint.com/android/android_spelling_checker.htm
     *
     * @param results results
     */
    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < results.length; ++i) {
            // Returned suggestions are contained in SuggestionsInfo
            final int len = results[i].getSuggestionsCount();
            sb.append('\n');

            for (int j = 0; j < len; ++j) {
                sb.append("," + results[i].getSuggestionAt(j));
            }

            sb.append(" (" + len + ")");
        }
        Log.d("SoftKeyboard", "SUGGESTIONS1: " + sb.toString());
    }

    private static final int NOT_A_LENGTH = -1;

    private void dumpSuggestionsInfoInternal(
            final List<String> sb, final SuggestionsInfo si, final int length, final int offset) {
        // Returned suggestions are contained in SuggestionsInfo
        final int len = si.getSuggestionsCount();
        for (int j = 0; j < len; ++j) {
            sb.add(si.getSuggestionAt(j));
        }
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
        Log.d("SoftKeyboard", "onGetSentenceSuggestions");
        final List<String> sb = new ArrayList<>();
        for (int i = 0; i < results.length; ++i) {
            final SentenceSuggestionsInfo ssi = results[i];
            for (int j = 0; j < ssi.getSuggestionsCount(); ++j) {
                dumpSuggestionsInfoInternal(
                        sb, ssi.getSuggestionsInfoAt(j), ssi.getOffsetAt(j), ssi.getLengthAt(j));
            }
        }

        Log.d("SoftKeyboard", "SUGGESTIONS2: " + sb.toString());
        setSuggestions(sb, true, true);
    }


    private class SearchEngingTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {

            progressBar.setVisibility(View.VISIBLE);
            progressTxt.setVisibility(View.VISIBLE);
            super.onPreExecute();

            candidateWordLayout.setVisibility(View.INVISIBLE);
            candidateWordLayout.setVisibility(View.GONE);
            functionLayout.setVisibility(View.INVISIBLE);
            functionLayout.setVisibility(View.GONE);
            dictionaryLayout.setVisibility(View.INVISIBLE);
            dictionaryLayout.setVisibility(View.GONE);

        }

        @Override
        protected String doInBackground(String... para) {
            //Call Soundex**********************************************
            ArrayList<String> similarWord = soundex.getSoundex(para[0]);

            //Count syllable
            boundary.setText(para[0]);
            int wordCount = countWord(boundary, para[0]);

            ArrayList<String> LDWord = new ArrayList<String>();

            ArrayList<String> delRule = new ArrayList<String>();
            ArrayList<String> delSoundex;
            ArrayList<String> allDelSoundex = new ArrayList<String>();

            ArrayList<String> missingChar;

            Log.i("No.of Syllable--->: ", String.valueOf(wordCount));
            LDRule search = new LDRule();

            if (wordCount == 1) {

                firstLD = search.findWordWithLDLaw(para[0]);
                int wordSize = firstLD.size();

                for (int i = 0; i < wordSize; i++) {
                    if (isInDictionary(firstLD.get(i))) {
                        LDWord.add(firstLD.get(i));
                    }
                }
                //Log.i("LDWord--->", String.valueOf(LDWord));

                delRule.addAll(search.deleteChar(para[0]));
                for (int i = 0; i < delRule.size(); i++) {
                    delSoundex = soundex.getSoundex(delRule.get(i));
                    allDelSoundex.addAll(delSoundex);

                }
                //Log.i("allDelSoundex--->", String.valueOf(allDelSoundex));

                LDWord.addAll(allDelSoundex);

            } else {

                //LDWord.addAll(missChar.findMissingChar(para[0]));
                missingChar = missChar.findMissingChar(para[0]);
                Log.i("missingChar--->", String.valueOf(missingChar));

                for (int i = 0; i < missingChar.size(); i++) {
                    boundary.setText(missingChar.get(i));
                    if (wordCount - 1 <= countWord(boundary, missingChar.get(i))
                            || wordCount + 1 >= countWord(boundary, missingChar.get(i))) {
                        //Log.i("wordCountwordCount--->", String.valueOf(countWord(boundary, missingChar.get(i))));
                        LDWord.add(missingChar.get(i));
                    }

                }
                //Log.i("LDWord2:--->", String.valueOf(LDWord));
            }

            Distance sortWord = new Distance(LDWord, para[0]);
            LDWord = sortWord.getEditDistance();

            //Log.i("similarWordsim--->", String.valueOf(similarWord));

            wordPredictList.addAll(similarWord); //from above soundex
            wordPredictList.addAll(LDWord);
            //Remove duplicate word
            LinkedHashSet<String> lhs = new LinkedHashSet<String>();
            lhs.addAll(wordPredictList);
            wordPredictList.clear();
            wordPredictList.addAll(lhs);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.INVISIBLE);
            progressTxt.setVisibility(View.INVISIBLE);

            if (wordPredictList.size() == 0) {
                onKeyTxt.setText(" ไม่พบคำใกล้เคียง");
            } else {
                //Add user input in the first button
                String userInput = mComposing.toString().trim();

                //Calculate Total Page
                if ((screenInches < 6.5) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {
                    totalPage = wordPredictList.size() / 4;
                    if ((wordPredictList.size() % 4) != 0) {
                        totalPage++;
                    }
                    setCandidateWord4(paging, wordPredictList);
                } else {
                    totalPage = wordPredictList.size() / 6;
                    if ((wordPredictList.size() % 6) != 0) {
                        totalPage++;
                    }
                    setCandidateWord(paging, wordPredictList);
                }
                candidateWordLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.INVISIBLE);

            }
        }


    }


    public static int countWord(BreakIterator boundary, String source) {

        int wordCount = 0;
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            wordCount++;
        }
        return wordCount;
    }


    private boolean isInDictionary(String word) {
        String noteFilter = DictionaryDatabaseHelper.WORD_VOCAB + "='" + word + "'";
        ArrayList<String> q_ = new DictionaryDatabaseHelper(getApplicationContext()).getListDatafromDictionary(noteFilter);

       // Cursor cursor = getContentResolver().query(DictionaryProvider.CONTENT_URI,
                //DictionaryDBOpenHelper.ALL_COLUMNS, noteFilter, null, null);

        try {
            if (q_.size() == 0) {
                //cursor.close();
            } else {
                //cursor.close();
                return true;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }


    class ImageButtonHighlighterOnTouchListener implements View.OnTouchListener {
        final ImageButton button;
        int highlightColor;
        int controlColor;

        public ImageButtonHighlighterOnTouchListener(final ImageButton button) {
            super();
            highlightColor = Color.parseColor("#89898a");

            if(button.getId() == R.id.closeDictBtn){
                controlColor = Color.BLACK;
            }else{
                controlColor = Color.WHITE;
            }
            this.button = button;
        }

        @Override
        public boolean onTouch(final View view, final MotionEvent motionEvent) {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                beep();
                button.setColorFilter(highlightColor);

            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                button.setColorFilter(controlColor);
            }
            return false;
        }
    }
    private void beep(){
        Boolean beepStatus = sharedPreferences.getBoolean("beepStatus",true);
        if(beepStatus){
            Vibrator vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(50);
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);

        if(candidateWordLayout != null){
            candidateWordLayout.setVisibility(View.INVISIBLE);
            candidateWordLayout.setVisibility(View.GONE);
        }
        if(onKeyLayout != null){
            onKeyLayout.setVisibility(View.INVISIBLE);
            onKeyLayout.setVisibility(View.GONE);
            lineKbLayout.setVisibility(View.INVISIBLE);
            lineKbLayout.setVisibility(View.GONE);
        }
        if(functionLayout != null){
            functionLayout.setVisibility(View.INVISIBLE);
            functionLayout.setVisibility(View.GONE);
        }
        if(dictionaryLayout != null){
            dictionaryLayout.setVisibility(View.INVISIBLE);
            dictionaryLayout.setVisibility(View.GONE);
        }

        if(progressLayout != null){
            progressLayout.setVisibility(View.INVISIBLE);
            progressLayout.setVisibility(View.GONE);

        }

    }

    private ArrayList<Vocab> findMeaning(String word){
        String noteFilter = LexitronDatabaseHelper.LEXITRON_WORD + "='" + word + "'";

        //Cursor cursor = getContentResolver().query(LexitronProvider.CONTENT_URI,
               // LexitronDBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
        Cursor cursor = new LexitronDatabaseHelper(getApplicationContext()).getDatafromLexitron(noteFilter);

        ArrayList<Vocab> dictList = new ArrayList<Vocab>();
        if(cursor.moveToFirst()){
            do{
                Vocab vocab = new Vocab();
                vocab.setWord(word);
                vocab.setPartOfSpeech(cursor.getString(cursor.getColumnIndex(LexitronDatabaseHelper.LEXITRON_PART)));
                vocab.setThaiTrans(cursor.getString(cursor.getColumnIndex(LexitronDatabaseHelper.LEXITRON_THAI)));
                vocab.setEngTrans(cursor.getString(cursor.getColumnIndex(LexitronDatabaseHelper.LEXITRON_ENG)));
                dictList.add(vocab);
            }while (cursor.moveToNext());
        }
        return dictList;
    }
   /* public boolean checkUnInstall() {
//set expire date for program
        String valid_until = "01/12/2019";
        boolean result = false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date strDate = null;
        try {
            strDate = sdf.parse(valid_until);
            if (new Date().after(strDate)) {
                Context context = getApplicationContext();
                CharSequence text = "โปรแกรมหมดอายุ กรุณาถอนการติดตตั้ง";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                intent.setData(Uri.parse("package:th.or.nectec.twskeyboard"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                result = true;

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;

    }*/



}
