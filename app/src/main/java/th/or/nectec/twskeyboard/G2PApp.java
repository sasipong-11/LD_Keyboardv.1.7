package th.or.nectec.twskeyboard;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.FileUtils;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.os.Build;
import java.util.Locale;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import android.os.Environment;

public class G2PApp {
    protected static String G2P_DATA_DIR = "/G2P-data/";
    public static String G2P_DATA_PATH = "";  // set via init(Context)

    public static void init(android.content.Context ctx) {
        G2P_DATA_PATH = ctx.getFilesDir().getAbsolutePath() + G2P_DATA_DIR;
    }
    //public static String G2P_DATA_PATH = "file:///android_asset/G2P-data/";
    // protected static String G2P_URL = "http://vaja.nectec.or.th/vaja_android/G2P-data.zip";
//    protected static String G2P_URL = "http://localhost:8080/G2PAndroid/files/G2P-data.zip";
//    protected static String G2P_URL = "http://10.225.62.95:8080/G2PAndroid/files/G2P-data.zip";
  //  protected static String G2P_URL = "http://10.225.62.95:8080/G2PAndroid/register.php";

   protected static String G2P_URL = "http://spt-g2p.openservice.in.th/G2PAndroid/register.php";
    protected static String G2P_ZIP_FILE = "g2p-data.zip";
    protected static String G2P_KEY_FILE = "data.dat";
    protected static long G2P_SIZE_DATA = 9735342;
//    protected static long G2P_SIZE_DATA = 41261092; //vaja-data




    public   static String getDeviceID(Context context) {
        TelephonyManager telephonyManager  = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        ContentResolver contentResolver = context.getContentResolver();
        String uid = null;
        if (uid == null) {
            uid = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
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
    public static String getAccount(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();
        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }
        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");
            if (parts.length > 1)
                return parts[0];
        }
        return "";
    }
    protected static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String device = Build.DEVICE;
        if (device.toLowerCase(Locale.US).startsWith(manufacturer.toLowerCase(Locale.US))) {
            return capitalize(device);
        } else {
            return capitalize(manufacturer) + " " + device;
        }
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
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
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
