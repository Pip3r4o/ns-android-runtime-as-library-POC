package com.example.pkanev.myapplication;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tns.RuntimeHelper;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Application app = getApplication();

        RuntimeHelper rHelper = new RuntimeHelper(app);
        rHelper.initRuntime();

        try {
            InputStream in = getAssets().open("app/testScript.js");

            File file = File.createTempFile("temp", "script");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            IOUtils.copy(in, out);

            Object res = rHelper.runScript(file);
            System.out.println(res.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
