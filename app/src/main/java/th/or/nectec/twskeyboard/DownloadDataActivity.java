package th.or.nectec.twskeyboard;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;



import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadDataActivity extends Activity {
    private static String TAG = "G2P DDA";
    private static boolean stDownload = false;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_data);
        DownloadFileAsync DFA = new DownloadFileAsync();
        DFA.execute();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        GraphemeToPhoneme.releaseDataSetup();
        super.onDestroy();
    }

    class DownloadFileAsync extends AsyncTask<String, String, String>  {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        protected String doInBackground(String... aurl) {
            checkFolder();
            // Skip server registration (server is unavailable); copy G2P data directly from assets
            if (downloadDataFromAssets(G2PApp.G2P_ZIP_FILE, G2PApp.G2P_DATA_PATH + G2PApp.G2P_ZIP_FILE)) {
                stDownload = true;
                Log.i(TAG, "G2P data copied from assets");
            } else {
                Log.e(TAG, "Failed to copy G2P data from assets");
            }
            return null;
        }



        protected void onProgressUpdate(String... progress) {
//            Log.i(TAG,progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            if (stDownload) {
                Log.i(TAG, "ExtractFileAsync -> start");
                Intent intent = new Intent(DownloadDataActivity.this, ExtractActivity.class);
                startActivity(intent);
                Log.i(TAG, "ExtractFileAsync -> end");
            }
            Log.i(TAG, "finish");
            finish();
        }

        protected void checkFolder() {
            File dir = new File(G2PApp.G2P_DATA_PATH);

            if (!dir.exists())
                dir.mkdir();
        }

        protected void deleteFolder() {
            File dir = new File(G2PApp.G2P_DATA_PATH);

            if (dir.exists())
                dir.delete();
        }
        //download data g2p-data.zip from asstes
        private boolean downloadDataFromAssets(String inputFile, String outputFile) {
            try {
                byte data[] = new byte[1024];
                long total = 0;
                int count;
                int lenghtOfFile = 0;
                AssetManager assetManager = getAssets();

                InputStream input = assetManager.open(inputFile);
                OutputStream output = new FileOutputStream(outputFile);

                lenghtOfFile = input.available();

                Log.i(TAG, "Lenght of file: " + lenghtOfFile);

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                Log.e(TAG, "Error: downloadDataFromAssets "+ e);
                return false;
            }
            return true;
        }



    }




}
