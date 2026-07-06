package th.or.nectec.twskeyboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import com.example.bablueza.g2p.g2pJni;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.util.Log;
import java.net.URI;

/**
 * Created by Lattapol on 29/11/2559.
 */

public class GraphemeToPhoneme {

    private g2pJni g2p = null;
    private int stG2P = 0;
    private String deviceID = "";
    private String account = "";
    Context ctx;
    SharedPreferences sharedPreferences;


    public GraphemeToPhoneme(Context context){
        this.ctx = context;
        g2p = new g2pJni();
        deviceID = G2PApp.getDeviceID(ctx);
        sharedPreferences = context.getSharedPreferences("th.or.nectec.twskeyboard", Context.MODE_PRIVATE);

    }

    public String getG2P(String grapheme){
        try{
            downloadVoiceData();
            if (stG2P == 0) {
                String initFile = G2PApp.G2P_DATA_PATH + "initTTS.in";
                stG2P = g2p.initG2P(initFile, G2PApp.G2P_DATA_PATH, deviceID);
                Log.d("G2P_DEBUG", "initG2P path=" + initFile + " stG2P=" + stG2P);
            }
            if(!grapheme.equals("")) {
                String result = g2p.getPhoneme(grapheme);
                Log.d("G2P_DEBUG", "getPhoneme(" + grapheme + ")=" + result);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        }catch (Exception ex){
            Log.e("G2P_DEBUG", "Exception: " + ex.toString());
        }
        return "";
    }

    public void downloadVoiceData() {
        // delete folder when in install first time
        if (!sharedPreferences.getBoolean("downloadG2P", false)) {
            G2PApp.deleteFolder();
            sharedPreferences.edit().putBoolean("downloadG2P", true).apply();
        }
       if(!G2PApp.checkData())
        {
            Intent intent = new Intent(ctx, DownloadDataActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        }


    }



}


