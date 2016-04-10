package ma.library;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.PowerManager;

public class Util {
	public static String getMessage(Exception e) {
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer, true));
		return writer.toString();
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {

		}
	}

	public static String getPath() {
		File dir = new File("/data/data/ma.fprint/databases");
		if (!dir.exists()) {
			try {
				dir.mkdir();
			} catch (Exception e) {
			}
		}
		return dir.toString();
	}

	public final static boolean isScreenLocked(Context c) {
		android.app.KeyguardManager mKeyguardManager = (KeyguardManager) c
				.getSystemService(Context.KEYGUARD_SERVICE);
		return mKeyguardManager.inKeyguardRestrictedInputMode();
	}

	public static void setAlarm(Context context, int gap, PendingIntent sender) {

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ gap * 60 * 1000, gap * 60 * 1000, sender);
	}

	public static void cancelAlarm(Context context, PendingIntent sender) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
	}

	static PowerManager.WakeLock wl;

	public static void getPowerLock(Context context) {
		if (wl == null) {
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"authentication.wake_lock");
		}
		if(!wl.isHeld())
			wl.acquire();
	}

	public static void releasePowerLock() {
		if (wl != null && wl.isHeld())
			wl.release();
	}
}
