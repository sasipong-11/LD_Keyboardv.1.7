package th.or.nectec.twskeyboard;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Lattapol on 7/8/16 AD.
 */
public class CopyDatabase {
    final private static String DB_PATH = "th.or.nectec.twskeyboard";
    private String DB_NAME = "spell_sys.db"; //the extension may be .sqlite or .db
    private String DB_OUTPUT = "dictionary.db";

    CopyDatabase(String fileNameInput, String fileNameOutput){
        DB_NAME = fileNameInput;
        DB_OUTPUT = fileNameOutput;
    }

    private void createDirectory(){
        File theDir = new File("/data/data/" + DB_PATH + "/databases");

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            theDir.mkdir();
        }
    }

    public void copyFile(Context ctx) throws IOException {
        createDirectory();
        //Open your local db as the input stream
        InputStream myinput = ctx.getAssets().open(DB_NAME);

        //Open the empty db as the output stream
        OutputStream myoutput = new FileOutputStream("/data/data/" + DB_PATH + "/databases/" + DB_OUTPUT);

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[8192];
        int length;
        while ((length = myinput.read(buffer))>0) {
            myoutput.write(buffer,0,length);
        }

        //Close the streams
        myoutput.flush();
        myoutput.close();
        myinput.close();
    }
}
