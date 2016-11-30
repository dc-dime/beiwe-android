package org.beiwe.app;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.beiwe.app.ui.DebugInterfaceActivity;
import org.beiwe.app.ui.LoadingActivity;
import org.beiwe.app.ui.user.MainMenuActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class CrashHandler implements java.lang.Thread.UncaughtExceptionHandler{
	private final Context errorHandlerContext;
	private int millisecondsUntilRestart = 500;
	public CrashHandler(Context context) { this.errorHandlerContext = context; }

	/** This function is where any errors that occur in any Activity that inherits RunningBackgroundServiceActivity
	 * will dump its errors.  We roll it up, stick it in a file, and try to restart the app after exiting it.
	 * (using a new alarm like we do in the BackgroundService). */
	public void uncaughtException(Thread thread, Throwable exception){
		
		Log.w("CrashHandler Raw","start original stacktrace");
		exception.printStackTrace();
		Log.w("CrashHandler Raw","end original stacktrace");
		
		//Write that log file
		writeCrashlog(exception, errorHandlerContext);
//		Log.i("inside crashlog", "does this line happen");  //keep this line for debugging crashes in the crash handler (yup.)
		//setup to restart service
		Intent restartServiceIntent = new Intent( errorHandlerContext, thread.getClass() );
		restartServiceIntent.setPackage( errorHandlerContext.getPackageName() );
		PendingIntent restartServicePendingIntent = PendingIntent.getService( errorHandlerContext, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT );
		AlarmManager alarmService = (AlarmManager) errorHandlerContext.getSystemService( Context.ALARM_SERVICE );
		alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + millisecondsUntilRestart, restartServicePendingIntent);
		//exit beiwe
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(10);
	}
	
	
	/**Creates a crash log file that will be uploaded at the next upload event.
	 * Also writes error to the error log so that it is visible in logcat.
	 * @param exception A Throwable (probably your error).
	 * @param context An android Context */
	public static void writeCrashlog(Throwable exception, Context context) {
		//TODO: low priority. Add optional message parameter
		String appVariant = "unknown";
//		public static Class loadThisActivity = DebugInterfaceActivity.class;
//		public static Class loadThisActivity = MainMenuActivity.class;
		if( LoadingActivity.loadThisActivity == MainMenuActivity.class ) { appVariant = "clinical variant"; }
		if( LoadingActivity.loadThisActivity == DebugInterfaceActivity.class ) { appVariant = "debug variant"; }
		
		String exceptionInfo =  System.currentTimeMillis() + "\n"
				                + "BeiweVersion:" + DeviceInfo.getBeiweVersion()
								+ ", AndroidVersion:" + DeviceInfo.getAndroidVersion()
								+ ", Product:" + DeviceInfo.getProduct()
								+ ", Brand:" + DeviceInfo.getBrand()
								+ ", HardwareId:" + DeviceInfo.getHardwareId()
								+ ", Manufacturer:" + DeviceInfo.getManufacturer()
								+ ", Model:" + DeviceInfo.getModel()
								+ ", App Variant:" + appVariant + "\n";

		exceptionInfo += "Error message: " + exception.getMessage() + "\n";
		exceptionInfo += "Error type: " + exception.getClass() + "\n";

		if (exception.getSuppressed().length > 0) {
			for (Throwable throwable: exception.getSuppressed() ) {
				exceptionInfo += "\nSuppressed Error:\n";
				for (StackTraceElement element : throwable.getStackTrace() ) { exceptionInfo +="\t" + element.toString() + "\n"; }
			}
		}
		
		exceptionInfo += "\nError-fill:\n";
		for (StackTraceElement element : exception.fillInStackTrace().getStackTrace() ) { exceptionInfo += "\t" + element.toString() + "\n"; }
		exceptionInfo += "\nActual Error:\n";
		for (StackTraceElement element : exception.getCause().getStackTrace() ) { exceptionInfo += "\t" + element.toString() + "\n"; }

		Log.e("BEIWE ENCOUNTERED THIS ERROR", exceptionInfo); //Log error...

		FileOutputStream outStream; //write a file...
		try { outStream = context.openFileOutput("crashlog_" + System.currentTimeMillis(), Context.MODE_APPEND);
			outStream.write( ( exceptionInfo ).getBytes() );
			outStream.flush(); outStream.close(); }
		catch (FileNotFoundException e) {
			Log.e("Error Handler Failure", "Could not write to file, file DNE.");
			e.printStackTrace(); }
		catch (IOException e) {
			Log.e("Error Handler Failure", "Could not write to file, IOException.");
			e.printStackTrace(); }
	}
}