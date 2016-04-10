
package ma.fprint;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import android.os.Vibrator;
import android.util.Log;
import android.app.Activity;
//import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

public class Util {
	public enum PhoneStatus {
		SCREEN_ON, SCREEN_OFF
	}
	
	private static final String mName = "Preferences";

    public static String KEY_ISSAFETY = "key_issafety";
    public static String KEY_NEED_PWD = "key_need_pwd";
    public static String KEY_PWD = "key_pwd";
    public static String VALUE_ERROR_PWD = "value_error_pwd";
    public static String KEY_APP_LOCK_LIST_STRING = "key.applock.list.string";
    public static String KEY_LAST_APP_LOCK_STRING = "key.applock.last.string";
	
	public static final String SWITCH_LONG_TAP = "ma.fprint.long.key";
	public static final String SWITCH_SINGLE_TAP = "ma.fprint.single.key";
	
	private static  PhoneStatus mPhonestatus = PhoneStatus.SCREEN_ON;
	public static int mMatchFailCounter = 0;
	public static final int MAX_FAIL_COUNT = 500;
	
	public static void updateFingerName(Context context,String name, int ID){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("finger" + ID, name);
        editor.commit();
	}
	
	public static String getFingerName(Context context, int ID){
		String ret;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        ret = sp.getString("finger" + ID, context.getResources().getString(R.string.ma_enroll_fid) + ID);
        
        return ret;
	}
	
    public static void updateFingerLaunchAppName(Context context,String name, int ID){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("fingerApp" + ID, name);
        editor.commit();
	}
	
	public static String getFingerLaunchAppName(Context context, int ID){
		String ret;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        ret = sp.getString("fingerApp" + ID, context.getResources().getString(R.string.ma_enroll_fid) + ID);
        
        return ret;
	}

    public static void updateFingerLaunchAppFlag(Context context,int flag, int ID){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("fingerAppFlag" + ID, flag);
        editor.commit();
	}
	
	public static int getFingerLaunchAppFlag(Context context, int ID){
		int ret;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        ret = sp.getInt("fingerAppFlag" + ID, 0);
        
        return ret;
	}

    

    public static PhoneStatus getPhonestatus() {
		return mPhonestatus;
	}

	public static void setPhonestatus(PhoneStatus mPhone) {
		mPhonestatus = mPhone;
	}

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
                print(e.getMessage());
            }
        }
        return dir.toString();
    }

    public static void print(String str) {
        Log.i("JTAG", str);
    }

    public static void vibrate(Context context, long ms) {
        Vibrator v = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        v.vibrate(ms);
    }

    public static void writeXML(Context context, String str, int val) {
        print("writeXML: val=" + Integer.toString(val));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(str, val);
        editor.commit();
    }

    public static int readXML(Context context, String str, int def) {
        int ret;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        ret = sp.getInt(str, def);

        print("readXML ret=" + Integer.toString(ret));

        return ret;
    }

    public static int isSafety(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(mName, Activity.MODE_PRIVATE);
		return preferences.getInt(KEY_ISSAFETY, 0);
	}
	
	
    public static boolean isNeedPwd(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(mName, Activity.MODE_PRIVATE);
		return preferences.getBoolean(KEY_NEED_PWD, true);
	}
	
	public static void enablePwd(Context context, boolean needPwd) {
		SharedPreferences preferences = context.getSharedPreferences(mName, Activity.MODE_PRIVATE);
		preferences.edit().putBoolean(KEY_NEED_PWD, needPwd).commit();
	}
    
	public static void setSafety(Context context, int isSafe) {
		SharedPreferences preferences = context.getSharedPreferences(mName, Activity.MODE_PRIVATE);
		preferences.edit().putInt(KEY_ISSAFETY, isSafe).commit();
	}
    
    public static void putPwd(Context context, String pwd) {
		SharedPreferences preferences = context.getSharedPreferences(mName, Activity.MODE_PRIVATE);
		preferences.edit().putString(KEY_PWD, pwd).commit();
	}

	public static String getPwd(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(mName, Activity.MODE_PRIVATE);
		return preferences.getString(KEY_PWD, VALUE_ERROR_PWD);
	}
    
	public static String getExistAppLockListString(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(mName, Activity.MODE_PRIVATE);
		return preferences.getString(KEY_APP_LOCK_LIST_STRING, "");
	}
    public static void Vibrate(final Context con, long milliseconds) {
        Vibrator vib = (Vibrator) con.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }
    
    public final static boolean isScreenLocked(Context c) {
        android.app.KeyguardManager mKeyguardManager = (KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode(); 
    }

	public static void putExistAppLockListString(Context context, String applist) {
		SharedPreferences preferences = context.getSharedPreferences(mName, Activity.MODE_PRIVATE);
		preferences.edit().putString(KEY_APP_LOCK_LIST_STRING, applist).commit();
	}
	
	public static String getLastAppLockString(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(mName, Activity.MODE_PRIVATE);
		return preferences.getString(KEY_LAST_APP_LOCK_STRING, "");
	}
	
	public static void putLastAppLockString(Context context, String applist) {
		SharedPreferences preferences = context.getSharedPreferences(mName, Activity.MODE_PRIVATE);
		preferences.edit().putString(KEY_LAST_APP_LOCK_STRING, applist).commit();
	}
	
	public static void setSettingFpAppLockOffOn(Context context, boolean value) {
	    if (value == false) {
			//*Settings.System.putInt(context.getContentResolver(), Settings.System.MICROARRAY_FINGERPRINT_USEDTO_APPLOCK, 0);
			//*Settings.System.putString(context.getContentResolver(), Settings.System.MICROARRAY_LAST_APP_LOCK_PACKAGE_NAME, "");
	    } else {
	    	//*Settings.System.putInt(context.getContentResolver(), Settings.System.MICROARRAY_FINGERPRINT_USEDTO_APPLOCK, 1);
	    }
	}
	

	public static boolean isSettingFpAppLockOn(Context context) {
		int able = 1;//*Settings.System.getInt(context.getContentResolver(), Settings.System.MICROARRAY_FINGERPRINT_USEDTO_APPLOCK, 0);
		return able == 1 ? true : false;
	}
	

	public static void setSettingFpScreenLockOffOn(Context context, boolean value) {
//		Settings.System.putInt(context.getContentResolver(), Settings.System.BIRD_SYSTEM_SETTINGS_FP_SCREENLOCK, value == true ? 1 : 0);
	}
	

	public static boolean isSettingFpScreenLockOn(Context context) {
		int able = 1;//Settings.System.getInt(context.getContentResolver(), Settings.System.BIRD_SYSTEM_SETTINGS_FP_SCREENLOCK, 0);
		return able == 1 ? true : false;
	}
}
