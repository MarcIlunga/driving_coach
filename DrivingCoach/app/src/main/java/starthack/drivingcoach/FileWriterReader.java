package starthack.drivingcoach;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by joachim on 3/18/17.
 */

public class FileWriterReader {


    public static void writeFile(String filename, String toWrite, Context context) {
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(toWrite.getBytes());
            outputStream.close();
            Log.d("<<<<<", "Writed in file !");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFile (String filename, Context context) {
        try {
            File file = new File(context.getFilesDir(), filename);
            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean fileExist(String filename, Context context) {
        File file = new File(context.getFilesDir(), filename);
        return file.exists();
    }

}
