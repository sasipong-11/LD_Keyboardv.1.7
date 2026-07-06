package th.or.nectec.twskeyboard;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExtractActivity extends Activity {
    private static String TAG = "G2P EFA";

    public static final int DIALOG_EXTRACT_PROGRESS = 0;
    private ProgressDialog mProgressDialog;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extract);
        ExtractFileAsync DFA = new ExtractFileAsync();
        DFA.execute(G2PApp.G2P_DATA_PATH+G2PApp.G2P_ZIP_FILE);
    }
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_EXTRACT_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Extract file..");
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
    class ExtractFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_EXTRACT_PROGRESS);
        }
        protected String doInBackground(String... afile) {
            String zipFile = afile[0];
            if(checkFile(zipFile)) {
                try {
                    long lenghtOfFile = G2PApp.G2P_SIZE_DATA;
                    long total = 0;
                    int count;

                    FileInputStream fis = new FileInputStream(zipFile);
                    ZipInputStream inputStream = new ZipInputStream(fis);
                    // Loop through all the files and folders
                    for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {
                        String innerFileName = G2PApp.G2P_DATA_PATH + File.separator
                                + entry.getName();
                        File innerFile = new File(innerFileName);
                        if (innerFile.exists()) {
                            innerFile.delete();
                        }
                        // Check if it is a folder
                        if (entry.isDirectory()) {
                            // Its a folder, create that folder
                            innerFile.mkdirs();
                        }
                        else {
                            // Create a file output stream
                            innerFile.getParentFile().mkdirs();
                            FileOutputStream outputStream = new FileOutputStream(innerFileName);
                            final int BUFFER = 2048;
                            // Buffer the output to the file
                            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                                    outputStream, BUFFER);
                            // Write the contents
                            byte[] data = new byte[BUFFER];

                            while ((count = inputStream.read(data, 0, BUFFER)) != -1) {
                                total += count;
                                publishProgress(""+(int)((total*100)/lenghtOfFile));
                                bufferedOutputStream.write(data, 0, count);
                            }
                            // Flush and close the buffers
                            bufferedOutputStream.flush();
                            bufferedOutputStream.close();
                        }
                        // Close the current entry
                        inputStream.closeEntry();
                    }
                    inputStream.close();
                    Log.i(TAG,"total = "+total);
                    deleteFile(zipFile);
                } catch (Exception e) {}
            }
            return null;

        }
        protected void onProgressUpdate(String... progress) {
//            Log.i(TAG,progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            dismissDialog(DIALOG_EXTRACT_PROGRESS);
            Log.i(TAG,"finish");
            finish();

        }
        protected boolean checkFile(String filename)
        {
            File file = new File(filename);
            return file.exists();
        }
        protected void deleteFile(String filename)
        {
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
