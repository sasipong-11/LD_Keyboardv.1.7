package th.or.nectec.twskeyboard;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class KeyboardSettingActivity extends AppCompatActivity {

    public static int flag = 0;
    public static int flagbeep = 0;
    RadioGroup themeRadioGroup;
    RadioButton basicRadio,monkeyRadio,jumpRadio,templeRadio,fishRadio,dollRadio;
    RadioButton parrotRadio,catRadio,horseRadio,goldfishRadio,dolphinRadio;

    RadioGroup beepRadioGroup;
    RadioButton beepOnRadio;
    RadioButton beepOffRadio;

    SeekBar keyTextSeekBar;

    Button settingButton;
    Button manualButton;

    ImageView themeBgImageView;
    SharedPreferences sharedPreferences;

    TextView headSelectBg, headSelectSize, headSelectVibration, versionHeadTxt, versionDetailTxt,keySeekBarProgress;

    TextView keyThai1, keyThai2, keyThai3, keyThai4, keyThai5, keyThai6, keyThai7, keyThai8;
    TextView keyEng1, keyEng2, keyEng3, keyEng4, keyEng5, keyEng6, keyEng7, keyEng8;
    //Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard_setting);
        requestAppPermissions();
       /* AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Keyboard Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());*/

        setTitle(R.string.settings_name);

        Typeface font_mahaniyom = Typeface.createFromAsset(getAssets(), "fonts/layijimahaniyom.ttf");
        Typeface font_CSChatThaiUI = Typeface.createFromAsset(getAssets(), "fonts/CSChatThaiUI.ttf");

        sharedPreferences = this.getSharedPreferences("th.or.nectec.twskeyboard", Context.MODE_PRIVATE);
        //sharedPreferences.edit().putString("font", "fonts/raingan.ttf").apply();


        if (sharedPreferences.getBoolean("start", true)) {
            //ViewPDFFile("LD Keyboard Manual.pdf");
            sharedPreferences.edit().putBoolean("start", false).apply();
        }


        themeBgImageView = (ImageView) findViewById(R.id.themeBgImageView);

        themeRadioGroup = (RadioGroup) findViewById(R.id.themeRadioGroup);
        basicRadio = (RadioButton) findViewById(R.id.basicThemeRadio);
        monkeyRadio = (RadioButton) findViewById(R.id.monkeyThemeRadio);
        jumpRadio = (RadioButton) findViewById(R.id.jumpThemeRadio);
        templeRadio = (RadioButton) findViewById(R.id.templeThemeRadio);
        fishRadio = (RadioButton) findViewById(R.id.fishThemeRadio);
        dollRadio = (RadioButton) findViewById(R.id.dollThemeRadio);
        parrotRadio = (RadioButton) findViewById(R.id.parrotThemeRadio);
        catRadio = (RadioButton) findViewById(R.id.catThemeRadio);
        horseRadio = (RadioButton) findViewById(R.id.horseThemeRadio);
        goldfishRadio = (RadioButton) findViewById(R.id.goldfishThemeRadio);
        dolphinRadio = (RadioButton) findViewById(R.id.dolphinThemeRadio);

        basicRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        monkeyRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        jumpRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        templeRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        fishRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        dollRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        parrotRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        catRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        horseRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        goldfishRadio.setTypeface(font_mahaniyom.create(font_mahaniyom,Typeface.NORMAL));
        dolphinRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));

        if (flag == 1) {
            monkeyRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img11);
        }
        else if (flag == 2){
            jumpRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img14);
        }
        else if (flag == 3){
            templeRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img15);
        }
        else if (flag == 4){
            fishRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img22);
        }
        else if (flag == 5){
            dollRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img23);
        }
        else if (flag == 6){
            parrotRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img24);
        }
        else if (flag == 7){
            catRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img27);
        }
        else if (flag == 8){
            horseRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img31);
        }
        else if (flag == 9){
            goldfishRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img34);
        }
        else if (flag == 10){
            dolphinRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img39);
        }
        else if (flag == 11){
            basicRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.theme_blue);
        }else{
            monkeyRadio.setChecked(true);
            themeBgImageView.setImageResource(R.drawable.img11);
        }

        beepRadioGroup = (RadioGroup) findViewById(R.id.beepRadioGroup);
        beepOffRadio = (RadioButton) findViewById(R.id.beepOffRadio);
        beepOnRadio = (RadioButton) findViewById(R.id.beepOnRadio);

        beepOffRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        beepOnRadio.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));

        if (flagbeep == 1) beepOffRadio.setChecked(true);
        else beepOnRadio.setChecked(true);

        keyTextSeekBar = (SeekBar) findViewById(R.id.keyTextSeekBar);

        keyThai1 = (TextView) findViewById(R.id.keyThai1);
        keyEng1 = (TextView) findViewById(R.id.keyEng1);
        keySeekBarProgress = (TextView) findViewById(R.id.keySeekBarProgress);

        keyThai1.setTypeface(font_CSChatThaiUI.create(font_CSChatThaiUI, Typeface.BOLD));
        keyEng1.setTypeface(font_CSChatThaiUI.create(font_CSChatThaiUI, Typeface.BOLD));

        settingButton = (Button) findViewById(R.id.settingBtn);
        manualButton = (Button) findViewById(R.id.manualBtn);


        headSelectBg = (TextView) findViewById(R.id.headSelectBg);
        headSelectSize = (TextView) findViewById(R.id.headSelectSize);
        headSelectVibration = (TextView) findViewById(R.id.headSelectVibration);
        versionHeadTxt = (TextView) findViewById(R.id.versionHeadTxt);
        versionDetailTxt = (TextView) findViewById(R.id.versionDetailTxt);

        headSelectBg.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.BOLD));
        headSelectSize.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.BOLD));
        headSelectVibration.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.BOLD));
        versionHeadTxt.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.BOLD));
        versionDetailTxt.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));

        settingButton.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));
        manualButton.setTypeface(font_mahaniyom.create(font_mahaniyom, Typeface.NORMAL));

        int keyTextSize = sharedPreferences.getInt("keyTextSize", 50);

        keyTextSeekBar.setProgress(keyTextSize);
        keySeekBarProgress.setText("ขนาดตัวอักษร : " + keyTextSeekBar.getProgress() // Get
                // progress
                + "/" + keyTextSeekBar.getMax());

        keyThai1.setTextSize(TypedValue.COMPLEX_UNIT_PX, keyTextSeekBar.getProgress());
        keyEng1.setTextSize(TypedValue.COMPLEX_UNIT_PX, keyTextSeekBar.getProgress());


        themeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                int themeIndex = themeRadioGroup.indexOfChild(findViewById(themeRadioGroup.getCheckedRadioButtonId()));
                int image;
                int setting;

                if (themeIndex == 1) {
                    image = R.drawable.theme_img11;
                    setting = R.drawable.img11;
                    flag = 1;

                } else if (themeIndex == 2) {
                    image = R.drawable.theme_img14;
                    setting = R.drawable.img14;
                    flag = 2;
                } else if (themeIndex == 3) {
                    image = R.drawable.theme_img15;
                    setting = R.drawable.img15;
                    flag = 3;
                } else if (themeIndex == 4) {
                    image = R.drawable.theme_img22;
                    setting = R.drawable.img22;
                    flag = 4;
                } else if (themeIndex == 5) {
                    image = R.drawable.theme_img23;
                    setting = R.drawable.img23;
                    flag = 5;
                } else if (themeIndex == 6) {
                    image = R.drawable.theme_img24;
                    setting = R.drawable.img24;
                    flag = 6;
                } else if (themeIndex == 7) {
                    image = R.drawable.theme_img27;
                    setting = R.drawable.img27;
                    flag = 7;
                } else if (themeIndex == 8) {
                    image = R.drawable.theme_img31;
                    setting = R.drawable.img31;
                    flag = 8;
                } else if (themeIndex == 9) {
                    image = R.drawable.theme_img34;
                    setting = R.drawable.img34;
                    flag = 9;
                } else if (themeIndex == 10) {
                    image = R.drawable.theme_img39;
                    setting = R.drawable.img39;
                    flag = 10;
                } else {
                    image = R.drawable.theme_blue;
                    setting = R.drawable.theme_blue;
                    flag = 11;

                }
                sharedPreferences.edit().putInt("bgTheme", image).apply();
                themeBgImageView.setImageResource(setting);
            }
        });
        beepRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                int beepIndex = beepRadioGroup.indexOfChild(findViewById(beepRadioGroup.getCheckedRadioButtonId()));
                if (beepIndex == 0) {
                    flagbeep =1;
                    sharedPreferences.edit().putBoolean("beepStatus", false).apply();
                } else {
                    flagbeep =2;
                    sharedPreferences.edit().putBoolean("beepStatus", true).apply();
                }
            }
        });

        keyTextSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // int progressChanged = 0;


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 10) {
                    sharedPreferences.edit().putInt("keyTextSize", progress + 10).apply();

                } else {
                    sharedPreferences.edit().putInt("keyTextSize", progress).apply();

                }
                keyThai1.setTextSize(TypedValue.COMPLEX_UNIT_PX, progress + 10);
                keyEng1.setTextSize(TypedValue.COMPLEX_UNIT_PX, progress + 10);
                keySeekBarProgress.setText("ขนาดตัวอักษร : " + keyTextSeekBar.getProgress() // Get
                        // progress
                        + "/" + keyTextSeekBar.getMax());


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //  Toast.makeText(KeyboardSettingActivity.this,"seek bar progress:"+progressChanged,
                //           Toast.LENGTH_SHORT).show();

            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {

                            String packageLocal = getPackageName();
                            Log.i("packageLocal","packageLocal: "+packageLocal);

                            InputMethodManager im = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                            String list = im.getEnabledInputMethodList().toString();
                            if(list.contains(packageLocal)){
                                //LD keyboard on แล้ว -> เลือกใช้งาน kb
                                InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                                imeManager.showInputMethodPicker();

                            }else{
                                //ถ้าLD Keyboard ยัง on
                                Intent intent = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
                                startActivity(intent);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            }
        });
        manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ManualActivity.class);
                startActivity(intent);
            }
        });




    }




    private void ViewPDFFile(String pdfFileName) {
        AssetManager assetManager = getAssets();

        InputStream in = null;
        OutputStream out = null;
        File file = new File(getFilesDir(), pdfFileName);
        try {
            in = assetManager.open(pdfFileName);
            out = openFileOutput(file.getName(), Context.MODE_WORLD_READABLE);

            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
                    Uri.parse("file://" + getFilesDir() + "/" + pdfFileName),
                    "application/pdf");
            startActivity(intent);
            finish();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "PDF reader does not found." + e, Toast.LENGTH_LONG).show();
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this ,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 112); // your request code
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }


}
