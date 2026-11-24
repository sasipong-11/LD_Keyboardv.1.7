package th.or.nectec.twskeyboard;

import android.content.Context;
import android.provider.Settings;

import java.io.File;

public class G2PApp {
    protected static String G2P_DATA_DIR = "/G2P-data/";
    public static String G2P_DATA_PATH = "";  // set via init(Context)

    public static void init(android.content.Context ctx) {
        G2P_DATA_PATH = ctx.getFilesDir().getAbsolutePath() + G2P_DATA_DIR;
    }
    protected static String G2P_ZIP_FILE = "g2p-data.zip";
    protected static String G2P_KEY_FILE = "data.dat";
    protected static long G2P_SIZE_DATA = 9735342;
//    protected static long G2P_SIZE_DATA = 41261092; //vaja-data




    public   static String getDeviceID(Context context) {
        // Used only locally as the key passed to the native G2P library; never sent off-device.
        String uid = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (uid == null) {
            uid = "";
        }
        if (uid.length() < 24) {
            int lenuid = 24 - uid.length();
            for (int i = 0; i < lenuid; i++) {
                uid = "0" + uid;
            }
        } else if (uid.length() > 24) {
            uid = uid.substring(uid.length() - 24, uid.length());
        }
        return uid;
    }
    public static boolean checkData()
    {
        File dir = new File(G2P_DATA_PATH);
        //check /storage/emulated/0/G2P-data/ มี folder หรือมีไฟล์อยู่เพื่อ download (g2p-data.zip)
        if(!dir.exists() || dir.listFiles().length == 0)
           return false;

       /* if(!dir.exists())
        {
            return false;
        }*/
        return true;
    }

    public static void deleteFolder() {
        File dir = new File(G2PApp.G2P_DATA_PATH);
       // if (dir.exists())
            recursiveDelete(dir);

    }

    public static void recursiveDelete(File file) {
        //to end the recursive loop
        if (!file.exists())
            return;

        //if directory, go inside and call recursively
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //call recursively
                recursiveDelete(f);
            }
        }
        //call delete to delete files and empty directory
        file.delete();
        System.out.println("Deleted file/folder: " + file.getAbsolutePath());
    }
    }
