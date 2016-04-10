package ma.service;

import ma.fprint.Util;
import ma.library.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.provider.Settings;
import ma.release.Fprint;

public class BootService extends BroadcastReceiver {
	private final String ACTION_BOOT = Intent.ACTION_BOOT_COMPLETED;
	private static final String FP_RESTART_SERVICE = "com.microarray.action.fingerprint.restartService";
	private static final String INTENT_UPDATE_LAST_PACKAGE_NAME = "ma.fprint.receiver.update.last_package_name";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();		
		if (action.equals(ACTION_BOOT)) { // 开机完成	
			int ret = Util.readXML(context, "lock", 0); 
			if(ret==1) { // 检查锁屏服务是否开启				
				Intent it = new Intent(context, MatchService.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);			
				context.startService(it);
			} else { 				
				ret = Fprint.open();
				Fprint.load();
				Fprint.start();
				for (int i=0;i < 10; i++){
				  ret = Fprint.power(Fprint.POWER_SLEEP);
				  Log.i("JTAG", "power sleep:" + ret);
				  if(ret == 0 ) break;
			  }
				Fprint.close();
			}
    } else if (intent.getAction().equals(FP_RESTART_SERVICE)){ 
    	Log.i("JTAG", "restart service action :" + action);
			int ret = Util.readXML(context, "lock", 0); 
			if(ret==1) { // 检查锁屏服务是否开启				
				Intent it = new Intent(context, MatchService.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);			
				context.startService(it);
			}
		} else if (intent.getAction().equals(INTENT_UPDATE_LAST_PACKAGE_NAME)) { 
			String packageName = intent.getStringExtra("packagename");
			//*Settings.System.putString(context.getContentResolver(), Settings.System.MICROARRAY_LAST_APP_LOCK_PACKAGE_NAME, packageName);
			//Util.putLastAppLockString(context, packageName);
	    }
	}
}