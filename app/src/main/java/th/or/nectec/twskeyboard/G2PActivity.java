package th.or.nectec.twskeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bablueza.g2p.g2pJni;

public class G2PActivity extends Activity {

    private static String TAG = "G2P Main";
    Button button = null;

    EditText input = null;
    EditText output = null;

    g2pJni g2p = null;
    int stG2P = 0;
    String deviceID = "";
    String account = "";
    SharedPreferences sharedPreferences;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getSharedPreferences("th.or.nectec.twskeyboard", Context.MODE_PRIVATE);

        deviceID = G2PApp.getDeviceID(this);
        account = G2PApp.getAccount(this);

        setContentView(R.layout.activity_g2_p);
        Log.i(TAG,"DeviceID = "+deviceID);
        Log.i(TAG,"Account = "+account);

        downloadVoiceData();

        g2p = new g2pJni();
        Log.d("Lattapol: ",deviceID);
        String abc = G2PApp.G2P_DATA_PATH+"initTTS.in ";

        Log.d("Lattapollllll", abc);
        try{

        }catch (Exception e){
            Toast.makeText(G2PActivity.this,"Exception : " + e,Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        stG2P = g2p.initG2P(G2PApp.G2P_DATA_PATH+"initTTS.in",G2PApp.G2P_DATA_PATH,deviceID);

        //Log.i(TAG,"stG2P = "+stG2P);

        input = (EditText) findViewById(R.id.input);
        output = (EditText) findViewById(R.id.output);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = input.getText().toString();
                Log.i(TAG,"Click -> "+text);
                if(!text.equals("")) {


                    if(stG2P==0) {
                        try{
                            stG2P = g2p.initG2P(G2PApp.G2P_DATA_PATH+"initTTS.in",G2PApp.G2P_DATA_PATH,deviceID);
                            Log.i(TAG,"stG2P@@@@@@@@@@@@ = "+stG2P);
                        }catch (Exception e){
                            Toast.makeText(G2PActivity.this,"Exception : " + e,Toast.LENGTH_LONG).show();

                            e.printStackTrace();
                        }

                        Log.i(TAG,"stG2P = "+stG2P);
                        if(stG2P==1)
                        {
                            String result = g2p.getPhoneme(text);
                            Log.i(TAG, result);
                            output.setText(result);
                        }
                    }
                    else {
                        String result = g2p.getPhoneme(text);
                        Log.i(TAG, result);
                        output.setText(result);
                    }
                }
            }
        });
    }

    protected void onDestroy() {
        Log.i(TAG,"deallocateG2P");
        g2p.deallocateG2P();
        Log.i(TAG,"onDestroy");
        super.onDestroy();
    }

    public void downloadVoiceData() {
        // delete folder when in install first time
        if (!sharedPreferences.getBoolean("downloadG2P", false)) {
            G2PApp.deleteFolder();
            sharedPreferences.edit().putBoolean("downloadG2P", true).apply();
        }
        if (!G2PApp.checkData()) {
                Log.i(TAG, "downloadVoiceData -> start");
                Intent intent = new Intent(G2PActivity.this, DownloadDataActivity.class);
                startActivity(intent);
                Log.i(TAG, "downloadVoiceData -> end");

            }

    }
}
