package com.jetec.csvexample;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName()+"My";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**取得讀寫權限*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            /**製作CSV*/
            makeCSV();
        });
    }//onCreate
    private void makeCSV() {
        new Thread(() -> {
            /**決定檔案名稱*/
            String date = new SimpleDateFormat("yyyy-MM-dd",
                    Locale.getDefault()).format(System.currentTimeMillis());
            String fileName = "[" + date + "]碼農日常輸出的CSV.csv";
            /**撰寫內容*/
            //以下用詞：直行橫列
            //設置第一列的內容
            String[] title ={"Id","Chinese","English","Math","Physical"};
            StringBuffer csvText = new StringBuffer();
            for (int i = 0; i < title.length; i++) {
                csvText.append(title[i]+",");
            }
            //設置其餘內容，共15行
            for (int i = 0; i < 15; i++) {
                csvText.append("\n" + (i+1));
                //此處巢狀迴圈為設置每一列的內容
                for (int j = 1; j < title.length; j++) {
                    int random = new Random().nextInt(80) + 20;
                    csvText.append(","+random);
                }
            }
            Log.d(TAG, "makeCSV: \n"+csvText);//可在此監視輸出的內容
            runOnUiThread(() -> {
                try {
                    //->遇上exposed beyond app through ClipData.Item.getUri() 錯誤時在onCreate加上這行
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    builder.detectFileUriExposure();
                    //->遇上exposed beyond app through ClipData.Item.getUri() 錯誤時在onCreate加上這行
                    FileOutputStream out = openFileOutput(fileName, Context.MODE_PRIVATE);
                    out.write((csvText.toString().getBytes()));
                    out.close();
                    File fileLocation = new File(Environment.
                            getExternalStorageDirectory().getAbsolutePath(), fileName);
                    FileOutputStream fos = new FileOutputStream(fileLocation);
                    fos.write(csvText.toString().getBytes());
                    Uri path = Uri.fromFile(fileLocation);
                    Intent fileIntent = new Intent(Intent.ACTION_SEND);
                    fileIntent.setType("text/csv");
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                    startActivity(Intent.createChooser(fileIntent, "輸出檔案"));
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w(TAG, "makeCSV: "+e.toString());
                }
            });
        }).start();
    }//makeCSV
}