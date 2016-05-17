package com.tns;

import java.io.File;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.IOException;

public class RuntimeHelper
{
	private final Application app;
	private Runtime runtime;
	
	public RuntimeHelper(Application app)
	{
		this.app = app;
	}
	
	// hasErrorIntent tells you if there was an event (with an uncaught exception) raised from ErrorReport
	public boolean hasErrorIntent()
	{
		boolean hasErrorIntent = false;
		
		try
		{
			 //empty file just to check if there was a raised uncaught error by ErrorReport
			File errFile = new File(app.getFilesDir(), ErrorReport.ERROR_FILE_NAME);
			
			if (errFile.exists())
			{
				errFile.delete();
				hasErrorIntent = true;
			}
		}
		catch (Exception e)
		{
			Log.d(logTag, e.getMessage());
		}
		
		return hasErrorIntent;
	}
	
	public void initRuntime()
	{
		System.loadLibrary("NativeScript");
		
		Logger logger = new LogcatLogger(true, app);
		
		boolean showErrorIntent = hasErrorIntent();
		if (!showErrorIntent)
		{
			NativeScriptUncaughtExceptionHandler exHandler = new NativeScriptUncaughtExceptionHandler(logger, app);

			Thread.setDefaultUncaughtExceptionHandler(exHandler);
			
			Async.Http.setApplicationContext(this.app);
			
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
			catch (NameNotFoundException e)
			{
				if (logger.isEnabled()) logger.write("Error while getting current proxy thumb");
				e.printStackTrace();
			}
			ThreadScheduler workThreadScheduler = new WorkThreadScheduler(new Handler(Looper.getMainLooper()));
			Configuration config = new Configuration(this.app, workThreadScheduler, logger, appName, null, rootDir, appDir, classLoader, dexDir, dexThumb);

			Log.d("~~~1~~~", "Before new Runtime");
			this.runtime = new Runtime(config);

			exHandler.setRuntime(this.runtime);
			
			if (NativeScriptSyncService.isSyncEnabled(this.app))
			{
				NativeScriptSyncService syncService = new NativeScriptSyncService(this.runtime, logger, this.app);

				syncService.sync();
				syncService.startServer();
			}
			else
			{
				if (logger.isEnabled())
				{
					logger.write("NativeScript LiveSync is not enabled.");
				}
			}
			
			this.runtime.init();

			File file = new File(appDir, "app/internal/ts_helpers.js");

			runtime.runScript(file);
			// Runtime.initInstance(this.app);
			// runtime.run();
		}
	}

	public Object runScript(File jsFile) {
		Log.d("~~~2~~~", "runtime inside RuntimeHelper");
		return this.runtime.runScript(jsFile);
	}
	
	private final String logTag = "MyApp";
}
