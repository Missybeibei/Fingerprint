package ma.service;

import ma.fprint.Util;
import ma.library.FingerGestureDetector;
import ma.library.ICaptureAction;
import ma.library.IMatchAction;
import ma.library.MatchAction;
import ma.library.PhoneStatusBroadcastReceiver;
import ma.statusMachine.FingerPrintMachine;
import ma.statusMachine.Sensor;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;
//import com.android.settings.R;
import ma.fprint.R;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import ma.fprint.Util;
import android.provider.Settings;

public class MatchService extends Service {

	final String MA_UNLOCK = "ma.fprint.match.UNLOCK";
	final String MA_DISMISS = "ma.fprint.match.DISMISS";
	final String MA_MATCH_FAIL = "ma.intent.action.keyguard_goto_unlockscreen";
	
	private Handler mHandler = new MatchHandler();
	private RemoteCallbackList<IMatchAction> mAuthenticationList = new RemoteCallbackList<IMatchAction>();;
	private IMatchAction mAuthenticationCallback;
	
	private PhoneStatusBroadcastReceiver mReceiver = new PhoneStatusBroadcastReceiver(
			mHandler);
	
	private static FingerPrintMachine mFPM = null;
	private static boolean init = true;
	private static String pre,sub;
	
	@Override
	public void onCreate() {
		super.onCreate();
		pre = this.getResources().getString(R.string.ma_fprint_match_msg_pre);
		sub = this.getResources().getString(R.string.ma_fprint_match_msg_sub);
		
		System.out.println("hello create");
		//startForeground(1, new Notification());
		registerReceiver(mReceiver, PhoneStatusBroadcastReceiver.getFilter());
		mAuthenticationCallback = fa;
		Sensor.getInstance().setMatchAction(mAuthenticationCallback);
		if(init){
			mFPM = new FingerPrintMachine(getApplicationContext());
	//		mFPM.screenOn();
			init = false;
		}

		if(Util.isScreenLocked(getApplicationContext()))
		    mFPM.authentication();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("hello destroy");
		//stopForeground(true);
		unregisterReceiver(mReceiver);

		Intent it = new Intent(MatchService.this, MatchService.class);
        startService(it);
	}

	class MatchHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PhoneStatusBroadcastReceiver.screen_on:
				Util.setPhonestatus(Util.PhoneStatus.SCREEN_ON);
				if(Util.isScreenLocked(getApplicationContext()))
					mFPM.authentication();
				break;

			case PhoneStatusBroadcastReceiver.screen_off:
				Log.d("JTAG", "screen off");
				//*Settings.System.putString(getContentResolver(), Settings.System.MICROARRAY_ALREADY_UNLOCKED_PACKAGESNAME, "");
				//*Settings.System.putString(getContentResolver(), Settings.System.MICROARRAY_LAST_APP_LOCK_PACKAGE_NAME, "");

				if (Sensor.getInstance().fingerDetecting) {
					mFPM.fingerCancleDetect();
				}

                boolean isPMLock = Util.readXML(getApplicationContext(), "pmlock", 0) == 1? true : false;
				if(isPMLock) mFPM.authentication();

				Util.setPhonestatus(Util.PhoneStatus.SCREEN_OFF);
				Util.mMatchFailCounter = 0;
				break;

			case PhoneStatusBroadcastReceiver.key_f1:
				if (!Sensor.getInstance().disableInterrupt) {
					if (Sensor.getInstance().fingerDetecting) mFPM.fingerCancleDetect();
					mFPM.authentication();
				}
				break;
			case PhoneStatusBroadcastReceiver.app_lock:
				if (Sensor.getInstance().fingerDetecting) mFPM.fingerCancleDetect();
				mFPM.authentication();
				break;
			case PhoneStatusBroadcastReceiver.app_lock_release:
				if(!Util.isScreenLocked(getApplicationContext()))
					mFPM.unLocked();
				
				if (!Sensor.getInstance().fingerDetecting) mFPM.fingerDetect(null);
				break;
			case PhoneStatusBroadcastReceiver.screen_unlocked:
				mFPM.unLocked();
				Log.d("JTAG", " screen unlock");
				if (!Sensor.getInstance().fingerDetecting) {
					mFPM.fingerDetect(null);
				}
				Util.mMatchFailCounter = 0;
			default:
				break;
			}
		}
	}
	private IMatchAction.Stub fa = new IMatchAction.Stub() {

		@Override
		public void FingerMatchSucess(int id) throws RemoteException {
			// TODO Auto-generated method stub
			boolean bUnlock = (Util.readXML(getApplicationContext(), "lock", 0) == 1) ? true
                    : false;
			if (!bUnlock) return;
			Log.d("JTAG", "FingerMatchSuccess --------");
			Intent intent = new Intent(MA_UNLOCK);
			sendBroadcast(intent);
			Log.d("JTAG", "MatchService,successID:" + id);
			boolean isLauchApp = Util.getFingerLaunchAppFlag(getApplicationContext(), id) == 1? true : false;
			if(isLauchApp) {
			    String pkgName = Util.getFingerLaunchAppName(getApplicationContext(), id);
			    //Log.d("JTAG", "Match success, Lauch App:" + pkgName);
			    //*Settings.System.putString(getContentResolver(), Settings.System.MICROARRAY_LAST_APP_LOCK_PACKAGE_NAME, pkgName);
			    intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(pkgName);
				startActivity(intent);	
			} else {
			    Log.d("JTAG","nothing anymore!");	
			}
		}
		
		@Override
		public void FingerMatchFail() throws RemoteException {
			// TODO Auto-generated method stub
			Log.d("JTAG", "FingerMatchFail --------");
			boolean bUnlock = (Util.readXML(getApplicationContext(), "lock", 0) == 1) ? true
                            : false;
			if (!bUnlock) return;
			if(Util.getPhonestatus() == Util.PhoneStatus.SCREEN_OFF){
			    Util.vibrate(getApplicationContext(), 200);	
			    //Intent i = new Intent(MA_MATCH_FAIL);
			    //sendBroadcast(i);
			}
			
			if(Util.getPhonestatus() == Util.PhoneStatus.SCREEN_ON){
				Util.mMatchFailCounter++;
				if(Util.mMatchFailCounter < Util.MAX_FAIL_COUNT) {
					Intent i = new Intent(MA_MATCH_FAIL);
					//String msg = new String(pre + Util.mMatchFailCounter + sub);
                                        String msg = getApplicationContext().getResources().getString(R.string.msg_match_fail_tip);
					i.putExtra("msg_shown", msg);
					i.putExtra("msg_timeout", 2000);
					i.putExtra("faile_times",Util.mMatchFailCounter);
					sendBroadcast(i);
					Util.vibrate(getApplicationContext(), 200); 
					Log.d("JTAG", msg);
				} else {
			        Log.d("JTAG"," unLock before dismiss");
					Intent i = new Intent(MA_DISMISS);
					sendBroadcast(i);
				}
				
			}	
		}
	};
	
	
	private IMatchAction getCurrentAuthenticationCallback() throws RemoteException { 
		IMatchAction mAuthenticationAction = null;
        int N = mAuthenticationList.beginBroadcast();  
        for (int i = 0; i < N; i++) {  
			mAuthenticationAction = mAuthenticationList.getBroadcastItem(i); 
		}  
        mAuthenticationList.finishBroadcast();  
        
        return mAuthenticationAction;
    }
	
	private Handler mAuthenticationHandler = new Handler() {  
		   
        @Override  
        public void handleMessage(Message msg) { 
        	//getCurrentAuthenticationCallback();  
            super.handleMessage(msg);  
       }  
    };

	public FingerPrintMachine getFingerprintMachine(){
		return mFPM;
	}
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return new FingerServiceBinder();
	}
	
	public class FingerServiceBinder extends IFingerprintService.Stub{  
        public MatchService getFingerprintService(){  
            return MatchService.this;  
        }

		@Override
		public void startDetect() throws RemoteException {
			// TODO Auto-generated method stub
			Log.d("JTAG", "startDetector");
			boolean isDetectlongPress = Util.readXML(getApplicationContext(),Util.SWITCH_LONG_TAP,0) == 1;
			boolean isDetectSinglePress = Util.readXML(getApplicationContext(),Util.SWITCH_SINGLE_TAP,0) == 1;
			if(isDetectlongPress || isDetectSinglePress) {
			    if (!Sensor.getInstance().fingerDetecting) mFPM.fingerDetect(null);
			}
		}
		
		@Override
		public void cancleDetect() throws RemoteException {
			// TODO Auto-generated method stub
			Log.d("JTAG", "cancleDetector");
			if (Sensor.getInstance().fingerDetecting) mFPM.fingerCancleDetect();
			//mAuthenticationHandler.sendEmptyMessageDelayed(0, 100);
		}

		@Override
		public void registerAuthenticationCallBack(IMatchAction ma) throws RemoteException {
			// TODO Auto-generated method stub
			//mAuthenticationList.register(ma);
			Log.d("JTAG", "registerCallback");
			if (Sensor.getInstance().fingerDetecting) mFPM.fingerCancleDetect();
			if (null != ma) mAuthenticationCallback = ma;
			else mAuthenticationCallback = fa;
			Sensor.getInstance().setMatchAction(mAuthenticationCallback);
			mFPM.authentication();
		}

		@Override
		public void unregisterAuthenticationCallBack(IMatchAction ma) throws RemoteException {
			// TODO Auto-generated method stub
			//mAuthenticationList.unregister(ma);
			Log.d("JTAG", "unregisterCallback");
			mAuthenticationCallback = fa;
			Sensor.getInstance().setMatchAction(mAuthenticationCallback);
			mFPM.unLocked();
			
			if (!Sensor.getInstance().fingerDetecting) mFPM.fingerDetect(null);
		} 
		
		@Override
		public void registerCaptureCallBack(ICaptureAction ca) throws RemoteException {
			// TODO Auto-generated method stub
			//mAuthenticationList.register(ma);
			Log.d("JTAG", "registerCaptureCallback");
			if (Sensor.getInstance().fingerDetecting) mFPM.fingerCancleDetect();
			mFPM.fingerCapture(ca);
		}

		@Override
		public void unregisterCaptureCallBack() throws RemoteException {
			// TODO Auto-generated method stub
			//mAuthenticationList.unregister(ma);
			Log.d("JTAG", "unregisterCaptureCallback");
			mFPM.fingerCancleCapture();
			
			if (!Sensor.getInstance().fingerDetecting) mFPM.fingerDetect(null);
		}
    }
}
