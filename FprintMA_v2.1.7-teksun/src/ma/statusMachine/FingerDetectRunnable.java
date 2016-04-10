package ma.statusMachine;

import java.io.IOException;
import java.io.OutputStream;

import android.app.Instrumentation;
import android.util.Log;
import android.view.KeyEvent;
import ma.library.FingerGestureDetector;
//import ma.release.Fpevent;
import ma.release.Fprint;
import android.content.Context;

import ma.fprint.Util;

public class FingerDetectRunnable implements Runnable {
	private static boolean bRunning = false;
	private static FingerDetectRunnable fr = null;
	
	public long bcTime = 0;
	private static boolean init = true;
    private static FingerGestureDetector mFingerDetecor;
	
	private final int  KEY_STATUS_NONE = 0; 
	private final int  KEY_STATUS_DOWN = 1;
	private final int  KEY_STATUS_UP = 2;
	
	private int keyold_status = KEY_STATUS_NONE;
	private int keycur_status = KEY_STATUS_NONE;
	private int repeat_times = 0;
	private long last_down_time = 0, last_up_time = 0, cur_time = 0, interval = 0;
	private boolean long_press_flag = false;
	
	private boolean mReport = false;
	
	private static Context mContext;	

	public static void start(Context ct) {
		mContext = ct;
		stopRunnable();
		startRunnable();
	}
	
	public static void stopRunnable() {
		bRunning = false;
		Util.sleep(50);
	}
	
	private static void startRunnable() {
		if(fr == null) fr =new FingerDetectRunnable();
		bRunning = true;
		new Thread(fr).start();
	}
	
	public void run() {
		Log.d("JTAG","MatchThread: enter");
		repeat_times = 0;  // 
		while (true) {
			Fprint.power(0);
			//maSleep(100); 
			if(bRunning){ // 检测双击事件
				
				int nRet = Fprint.pressed(0);
				//int nRet = Fprint.check(0);
				//Log.d("AAA","MatchThread: ret = "+nRet); // @return 1长按，0没有, -1采图失败
				cur_time = System.currentTimeMillis();
				keycur_status = (nRet==1?KEY_STATUS_DOWN:KEY_STATUS_UP);
				
				if (nRet==1){ // 连续3次相同的down状态才是有效的，防干扰
					if(repeat_times < 2){   
						repeat_times++;
						continue;
					} else {
						repeat_times = 0;      // repeat_times有效之后清零
					}
				} else {
					repeat_times = 0;
				}
				
				if (keyold_status == KEY_STATUS_NONE){  // 线程刚启动的时候按键状态未知,首先初始化一下
					if (keycur_status == KEY_STATUS_DOWN){
						//Log.d("AAA","MatchThread: First key status KEY_DOWN");
						long_press_flag = false;
						last_down_time = cur_time;
						keyold_status = KEY_STATUS_DOWN;
					} else {
						//Log.d("AAA","MatchThread: First key status KEY_UP");
						keyold_status = KEY_STATUS_UP;
					}
					continue ;
				} else {  // 按键初始化状态已经初始化过,可以正常开始判断单击，长按事件
					if (keycur_status != keyold_status){ // 状态发生变更,需要清空状态和事件处理
						
						if (keycur_status == KEY_STATUS_DOWN){
							//Log.d("AAA","MatchThread: KEY_DOWN ");
							last_down_time = cur_time;
							single_double_key(KEY_STATUS_DOWN, cur_time);
						} else {
							//Log.d("AAA","MatchThread: KEY_UP");
							single_double_key(KEY_STATUS_UP, cur_time);
						} 
						long_press_flag = false;
						keyold_status = keycur_status;
						
					} else {  // 状态没有变化,则只统计是否长按
						if (keycur_status == KEY_STATUS_DOWN){
							if (!long_press_flag){  // 按下之后没有上报过长按事件,则上报长按
								interval = cur_time - last_down_time;
								if (interval > 800){
									Log.d("JTAG","MatchThread: Long Click");
									boolean isLongPress = Util.readXML(mContext,Util.SWITCH_LONG_TAP, 0) == 1 ? true : false;
									if(isLongPress) KeyEventReport(KeyEvent.KEYCODE_HOME);
									// input_key(long key);
									long_press_flag = true;
									//if (mFingerDetecor != null) mFingerDetecor.getFingerListener().onLongPress();
									//Fpevent.sendevent(Fpevent.EV_KEY, Fpevent.LONG_KEY);
								}
							}
						} else {
							single_double_key(KEY_STATUS_UP, cur_time);
						}
					}
					
				}
			} else {
				Log.d("AAA","MatchThread: exit");
				break;
			}
			
			maSleep(50);
		}
	}
	
	int count=0;
	public void single_double_key(int key_status, long time) {
		// TODO Auto-generated method stub
		boolean isSingleKey;
		if (!long_press_flag){  // 在发生按下动作后没有上报过长按,则报单击事件
			if (key_status == KEY_STATUS_DOWN)
			{
				mReport = false;
				count++;
			} else {
				if (count == 2){
					Log.d("JTAG","MatchThread: Double click ");
					//Fpevent.sendevent(Fpevent.EV_KEY, Fpevent.DOUBLE_KEY);
		            isSingleKey = Util.readXML(mContext,Util.SWITCH_SINGLE_TAP, 0) == 1 ? true : false;
					if(isSingleKey) KeyEventReport(KeyEvent.KEYCODE_BACK);
					count=0;
					mReport = true;
				} else if((time - last_down_time > 300) && (mReport == false)){
					Log.d("JTAG","MatchThread: single click ");
					
					isSingleKey = Util.readXML(mContext,Util.SWITCH_SINGLE_TAP, 0) == 1 ? true : false;
					if(isSingleKey) KeyEventReport(KeyEvent.KEYCODE_CAMERA);
					
					//Fpevent.sendevent(Fpevent.EV_KEY, Fpevent.SINGLE_KEY);
					count=0;
					mReport = true;
				}
			}
			
		} else {
			count=0;
		}
	}
	
	public void maSleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {

		}
	}
	
	public static void setFingerDetector(FingerGestureDetector fgd){
		mFingerDetecor = fgd;
	}
	
	public void KeyEventReport(final int keycode){
		new Thread() {
	        @Override
	        public void run() {
	            try {
	                Instrumentation inst = new Instrumentation();
	                inst.sendKeyDownUpSync(keycode);
	            } catch (Exception e) {
	                Log.e("Exception when sendKeyDownUpSync", e.toString());
	            }
	        }

	    }.start();
		
	}
}
