package com.example.pkanev.myapplication;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.tns.AssetExtractor;
import com.tns.Async;
import com.tns.Configuration;
import com.tns.DefaultExtractPolicy;
import com.tns.ExtractPolicy;
import com.tns.LogcatLogger;
import com.tns.Logger;
import com.tns.NativeScriptSyncService;
import com.tns.NativeScriptUncaughtExceptionHandler;
import com.tns.Runtime;
import com.tns.ThreadScheduler;
import com.tns.Util;
import com.tns.WorkThreadScheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Application app = getApplication();

        System.loadLibrary("NativeScript");

        Logger logger = new LogcatLogger(true, app);

        boolean showErrorIntent = false;
        if (!showErrorIntent)
        {
            NativeScriptUncaughtExceptionHandler exHandler = new NativeScriptUncaughtExceptionHandler(logger, app);

            Thread.setDefaultUncaughtExceptionHandler(exHandler);

            Async.Http.setApplicationContext(app);

            ExtractPolicy extractPolicy = new DefaultExtractPolicy(logger);
            new AssetExtractor(null, logger).extractAssets(app, extractPolicy);

            String appName = app.getPackageName();
            File rootDir = new File(app.getApplicationInfo().dataDir);
            File appDir = app.getFilesDir();

            try
            {
                appDir = appDir.getCanonicalFile();
            }
            catch (IOException e1)
            {
            }

            ClassLoader classLoader = app.getClassLoader();
            File dexDir = new File(rootDir, "code_cache/secondary-dexes");
            String dexThumb = null;
            try
            {
                dexThumb = Util.getDexThumb(app);
            }
            catch (PackageManager.NameNotFoundException e)
            {
                if (logger.isEnabled()) logger.write("Error while getting current proxy thumb");
                e.printStackTrace();
            }
            ThreadScheduler workThreadScheduler = new WorkThreadScheduler(new Handler(Looper.getMainLooper()));
            Configuration config = new Configuration(app, workThreadScheduler, logger, appName, null, rootDir, appDir, classLoader, dexDir, dexThumb);
            Runtime runtime = new Runtime(config);

            exHandler.setRuntime(runtime);

            if (NativeScriptSyncService.isSyncEnabled(app))
            {
                NativeScriptSyncService syncService = new NativeScriptSyncService(runtime, logger, app);

                syncService.sync();
                syncService.startServer();

                // preserve this instance as strong reference
                // do not preserve in NativeScriptApplication field inorder to make the code more portable
                // @@@
                //Runtime.getOrCreateJavaObjectID(syncService);
            }
            else
            {
                if (logger.isEnabled())
                {
                    logger.write("NativeScript LiveSync is not enabled.");
                }
            }

            runtime.init();
            System.out.println("~~~~~~ after runtime.init");
//            runtime.runScript(new File(appDir, "internal/ts_helpers.js"));
//            System.out.println("~~~~~~ after runtime.runScript");
//            Runtime.initInstance(app);
//            System.out.println("~~~~~~ after runtime.initInstance");
//            runtime.run();
            System.out.println("~~~~~~ after runtime.run");

            try {
                InputStream in = getAssets().open("app/testScript.js");

                File file = File.createTempFile("temp", "script");
                file.deleteOnExit();
                FileOutputStream out = new FileOutputStream(file);
                IOUtils.copy(in, out);

                Object res = runtime.runScript(file);

                int a = 5;
                System.out.println(a);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //runtime.runScript(new File("testScript.js"));
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
