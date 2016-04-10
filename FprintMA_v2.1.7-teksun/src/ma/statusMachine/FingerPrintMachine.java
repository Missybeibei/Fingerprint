package ma.statusMachine;

import ma.library.FingerGestureDetector;
import ma.library.ICaptureAction;
import ma.library.PhoneStatusBroadcastReceiver;
import ma.library.Util;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ma.release.Fprint;

/* 
 * authentication调用指纹验证流程 
 * 在接收到解锁成功广播时候调用unLocked() 函数
 */
public class FingerPrintMachine {

	private static Context mContext;
	private static PendingIntent sender ;
	
	FingerCaptureController mCaptureController;
	
	public FingerPrintMachine(Context ct) {
		mContext = ct;  
		if(sender == null) {
	    	Intent intent = new Intent(PhoneStatusBroadcastReceiver.REFRESH_INT);
	    	sender = PendingIntent.getBroadcast(ct, 0, intent, 0);
		}
	}
	
	public void unLocked() { 
		Util.cancelAlarm(mContext, getSender());
		//Sensor.getInstance().setStates(Sensor.getInstance().getSensorIdle());
		FingerRunnable.stopRunnable(); 
		Util.releasePowerLock();
		Sensor.getInstance().disableInterrupt = true;
		
		
	}
	public void authentication() {
		FingerRunnable.stopRunnable();
		Util.releasePowerLock();
		Util.getPowerLock(mContext);
		Util.cancelAlarm(mContext, getSender());
		Sensor.getInstance().reset();
		FingerRunnable.start();
	}
	
	public void fingerDetect(FingerGestureDetector fgd) {
//		Log.d("JTAG", " finger detect");
//		//FingerDetectRunnable.setFingerDetector(fgd);
//		Sensor.getInstance().fingerDetecting = true;
//		FingerDetectRunnable.stopRunnable();
//		FingerDetectRunnable.start(mContext);
	}
	
	public void fingerCancleDetect() {
//		Log.d("JTAG", "cancle detect");
//		Sensor.getInstance().fingerDetecting = false;
//		FingerDetectRunnable.stopRunnable();
	}
	
	public void fingerCapture(ICaptureAction ca) {
		Log.d("JTAG", " finger capture");
		mCaptureController = new FingerCaptureController();
		mCaptureController.setAction(ca);
		mCaptureController.startCapture();
	}
	
	public void fingerCancleCapture() {
		Log.d("JTAG", "cancle capture");
		mCaptureController.stopCapture();
	}
	
	public static Context getContext(){
		return mContext;
	}

	public static PendingIntent getSender() {
		return sender;
	}
}
