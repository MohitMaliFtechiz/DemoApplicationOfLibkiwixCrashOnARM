package com.example.myapplication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.kiwix.libkiwix.JNIKiwix;
import org.kiwix.libzim.Archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String icuData = loadICUData(this);
        JNIKiwix jniKiwix = new JNIKiwix(this);
        jniKiwix.setDataDirectory(icuData);
        try {
            Archive archive = new Archive(getZimFilePath());
            String articlePath = archive.getEntryByPath("favicon.ico")
                    .getItem(true)
                    .getPath();
            Log.e("ZIM_FILE_READER", "getting the favicon path " + articlePath);
        } catch (Exception e) {
            Log.e("ZIM_FILE_READER", "Error in getting the favicon path " + e);
        }
    }

    private String loadICUData(Context context) {
        try {
            File icuDir = new File(context.getFilesDir(), "icu");
            if (!icuDir.exists()) {
                icuDir.mkdirs();
            }

            String[] icuFileNames = context.getAssets().list("icu");
            if (icuFileNames == null) {
                icuFileNames = new String[0];
            }

            for (String icuFileName : icuFileNames) {
                File icuDataFile = new File(icuDir, icuFileName);
                if (!icuDataFile.exists()) {
                    try (FileOutputStream outputStream = new FileOutputStream(icuDataFile);
                         InputStream inputStream = context.getAssets().open("icu/" + icuFileName)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
            }
            return icuDir.getAbsolutePath();
        } catch (Exception e) {
            Log.w("TAG_KIWIX", "Error copying icu data file", e);
            return null;
        }
    }

    private String getZimFilePath() throws PackageManager.NameNotFoundException, IOException {
        Context context = createPackageContext(getPackageName(), 0);
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = assetManager.open("testzim.zim");
            File tempFile = new File(getExternalCacheDir(), "testzim.zim");

            if (tempFile.exists()) {
                tempFile.delete();
            }
            tempFile.createNewFile();

            outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return tempFile.getCanonicalPath();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Log error or handle exception
                    Log.w("TAG_KIWIX", "Error closing inputStream", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // Log error or handle exception
                    Log.w("TAG_KIWIX", "Error closing outputStream", e);
                }
            }
        }
    }
}
