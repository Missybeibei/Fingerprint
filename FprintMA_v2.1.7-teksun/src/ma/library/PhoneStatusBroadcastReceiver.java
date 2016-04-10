package ma.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;


public class PhoneStatusBroadcastReceiver extends BroadcastReceiver {

	public static final String SCREEN_ON = Intent.ACTION_SCREEN_ON;
	public static final String SCREEN_OFF = Intent.ACTION_SCREEN_OFF;
	public static final String KEY_F1 = "ma.fprint.action.KEY_F1";
	public static final String KEY_HOME = "ma.fprint.action.KEY_HOME";
	public static final String KEY_POWER = "ma.fprint.action.KEY_POWER";
	public static final String MA_UNLOCK = "ma.fprint.match.UNLOCK";
	public static final String REFRESH_INT = "ma.fprint.action.REFRESH_INT";
	public static final String APP_LOCK = "ma.fprint.action.APP_REQUEST_MATCH";
	public static final String APP_LOCK_RELEASE = "ma.fprint.action.APP_QUIT_MATCH";
	
	public static final int screen_on = 10;
	public static final int screen_off = 11;
	public static final int key_f1 = 12;
	public static final int screen_unlocked = 13;
	public static final int key_home = 14;
	public static final int refresh_int = 17;
	public static final int app_lock = 19;
	public static final int app_lock_release = 20;

	private Handler mHandler;

	public PhoneStatusBroadcastReceiver(){}
	public PhoneStatusBroadcastReceiver(Handler handler) {
		mHandler = handler;
	}

	@Override
	public void onReceive(Context arg0, Intent intent) {
		String action = intent.getAction();
		Log.d("JTAG","receiver action: " + action);
		if (action.equals(SCREEN_ON)) {
			mHandler.sendEmptyMessage(screen_on);
		} else if (action.equals(SCREEN_OFF)) {
			mHandler.sendEmptyMessage(screen_off);
		} else if (action.equals(KEY_F1)) {
			mHandler.sendEmptyMessage(key_f1);
		} else if (action.equals(Intent.ACTION_USER_PRESENT)) {
			mHandler.sendEmptyMessage(screen_unlocked);
		} else if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
			mHandler.sendEmptyMessage(key_home);
		} else if (action.equals(REFRESH_INT)) {
			mHandler.sendEmptyMessage(key_f1);
		} else if (action.equals(APP_LOCK)) {
			mHandler.sendEmptyMessage(app_lock);
		} else if (action.equals(APP_LOCK_RELEASE)) {
			mHandler.sendEmptyMessage(app_lock_release);
		}
	}
	
	public static IntentFilter getFilter(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(SCREEN_ON);
		filter.addAction(SCREEN_OFF);
		filter.addAction(KEY_F1);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		filter.addAction(REFRESH_INT);
		filter.addAction(APP_LOCK);
		filter.addAction(APP_LOCK_RELEASE);
		return filter;
	}

}
