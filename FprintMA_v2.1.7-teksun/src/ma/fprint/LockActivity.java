package ma.fprint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager.LayoutParams;
import android.app.ActionBar;

import ma.library.IMatchAction;
import ma.service.IFingerprintService;
import ma.view.NumberPad;
import ma.view.NumberPad.OnNumberClickListener;
import ma.view.ProcessIndication;



public class LockActivity extends Activity implements OnNumberClickListener, OnClickListener {

	private String TAG = "LockActivity-->";
	
	/* verify success */
	private static final int MSG_VERIFY_SUCCESS = 1;
	/* verify failed */
	private static final int MSG_VERIFY_FAILED = 2;

	private static final int MSG_VERIFY_NO_ENROLL = 3;
	
	private StringBuilder mPassword = null;
	
	private ProcessIndication mIndication = null;
	private NumberPad mNumberPad = null;
	private Button btn_toDigital = null;
	private ViewGroup fingerLayout = null;
	private ViewGroup digitaLayout = null;
	private TextView tv_notice = null;
	
	private Toast mToast = null;
	private String mGlobalPackageNameString = null;
	
	private MyHandler verifyHandler = new MyHandler();
	
	private boolean isFingerServiceRunning = false;
	
	private int numberFailCount = 0;//密码输入次数
	private int fingerFailCount = 0;//指纹对比次数
	
	/*[Finger verify failed vibrator]: TLB liuyang 20150914 begin */ 
	private Vibrator mVibrator = null;
	/*[Finger verify failed vibrator]: TLB liuyang 20150914 end */ 
	
	
	//add by microarray fingerprint service declared begin
	private IFingerprintService mFingerService = null;
	
    ServiceConnection conn = new ServiceConnection() {  
        @Override  
        public void onServiceDisconnected(ComponentName name) {  
        	
        }  
          
        @Override  
        public void onServiceConnected(ComponentName name, IBinder service) {  
            //返回一个MsgService对象  
        	mFingerService = IFingerprintService.Stub.asInterface(service);
        }  
    };

    private IMatchAction.Stub mFingerAuthenticationCallback = new IMatchAction.Stub() {

		@Override
		public void FingerMatchSucess(int i) throws RemoteException {
			// TODO Auto-generated method stub
			Log.d("JTAG", "match success in callback");
			verifyHandler.sendMessage(verifyHandler.obtainMessage(MSG_VERIFY_SUCCESS, 0, 1, null));
			//finish();
		}

		@Override
		public void FingerMatchFail() throws RemoteException {
			// TODO Auto-generated method stub
			//Log.d("JTAG", "match failed in callback");
			vibrateShort();
			verifyHandler.sendMessage(verifyHandler.obtainMessage(MSG_VERIFY_FAILED, 0, 0, null));
		}  
    	   
        
    };
	
    private Handler mAuthenticationHandler = new Handler() {  
		   
        @Override  
        public void handleMessage(Message msg) { 
        	//getCurrentAuthenticationCallback(); 
        	try {
    			mFingerService.registerAuthenticationCallBack(mFingerAuthenticationCallback);
    		} catch (RemoteException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            super.handleMessage(msg);  
       }  
    };
    
  //add by microarray fingerprint service declared end
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGlobalPackageNameString = getIntent().getStringExtra("packagename");
		setContentView(R.layout.activity_lock_ui);
		initViews();
		initListener();
		initActivity();
	}
	
	/*[Finger verify failed vibrator]: TLB liuyang 20150914 begin */ 
	private void vibrateShort() {
		if (mVibrator == null) {
			mVibrator = (Vibrator) getApplicationContext().getSystemService(
					Context.VIBRATOR_SERVICE);
		}
		if (mVibrator != null) {
			mVibrator.vibrate(100);
		}
	}
	
	private void vibrateStop() {
		if (mVibrator != null) {
			mVibrator.cancel();
		}
	}
	/*[Finger verify failed vibrator]: TLB liuyang 20150914 end */ 
	
	private void initViews() {
		btn_toDigital = (Button) findViewById(R.id.toDigital);
		fingerLayout = (ViewGroup) findViewById(R.id.fingerlayout);
		digitaLayout = (ViewGroup) findViewById(R.id.digitallayout);
	    mIndication = (ProcessIndication) findViewById(R.id.id_lockui_indication);
		mNumberPad = (NumberPad) findViewById(R.id.id_lockui_numberpad);
		tv_notice = (TextView) findViewById(R.id.overtime_notice);
	}
	
	private void initListener() {
        mNumberPad.setOnNumberClickListener(this);
        btn_toDigital.setOnClickListener(this);
	}
	
	private void initActivity() {
            ActionBar.LayoutParams lp =new ActionBar.LayoutParams(
	    ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.MATCH_PARENT,Gravity.CENTER);

	    View viewTitleBar = getLayoutInflater().inflate(R.layout.lockui_actionbar_layout, null);
	    ActionBar actionBar = getActionBar();
	    actionBar.setCustomView(viewTitleBar, lp);
	    actionBar.setDisplayShowHomeEnabled(false);//去掉导航
            actionBar.setDisplayShowTitleEnabled(false);//去掉标题
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
	    actionBar.setDisplayShowCustomEnabled(true);

	    mPassword = new StringBuilder();
	    numberFailCount = 0;
	    fingerFailCount = 0;
	    
	    Intent intent = new Intent("ma.service.aidl.IFingerprintService");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}
	
	private void startFingerVerify() {
		
		mAuthenticationHandler.sendEmptyMessageDelayed(0, 500);

        isFingerServiceRunning = true;
	}
	
	private void stopFingerVerify() {
        Log.i("wrf1133", "--- stopFingerVerify ---");
      //add by microarray fingerprint service declared begin
        try {
			mFingerService.unregisterAuthenticationCallBack(mFingerAuthenticationCallback);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      //add by microarray fingerprint service declared end
        
        isFingerServiceRunning = false;
    }
    
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            mGlobalPackageNameString = intent.getStringExtra("packagename");
        }
        numberFailCount = 0;
        fingerFailCount = 0;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		fingerLayout.setVisibility(View.VISIBLE);
        digitaLayout.setVisibility(View.GONE);
        startFingerVerify();
        handleOverTime(Settings.System.getLong(getContentResolver(), "microarray_over_time", 0));
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
        case R.id.toDigital:
            fingerLayout.setVisibility(View.GONE);
            digitaLayout.setVisibility(View.VISIBLE);
            break;
            
		default:
			break;
		}
	}
	
	@Override
	public void clickNumer(int number) {
		if (number == -2) {
            //do nothing
		} else if (number == -1) {
			// 如果点击删除键
			if (mPassword.length() > 0) {
				mPassword.deleteCharAt(mPassword.length() - 1);
			}
		} else {
			// 如果是其他数字按键
			mPassword.append(number);
			String pwd = mPassword.toString();
			if (pwd.equals(Util.getPwd(this))) {
				mPassword.delete(0, mPassword.length());
				if (mGlobalPackageNameString != null && !mGlobalPackageNameString.equals("com.finger.clearData")) {
				    //*Settings.System.putString(getContentResolver(), Settings.System.MICROARRAY_LAST_APP_LOCK_PACKAGE_NAME, mGlobalPackageNameString);
				    
				    //这里保存已经解锁过的应用包名
				    String allTemp = getUnlockedApp(mGlobalPackageNameString);
				    Log.i(TAG, "allTemp = " + allTemp);
				    //*Settings.System.putString(getContentResolver(), Settings.System.MICROARRAY_ALREADY_UNLOCKED_PACKAGESNAME, allTemp);
				}
				if (mGlobalPackageNameString.equals("com.finger.clearData")) {
				    Settings.System.putInt(getContentResolver(),  "com_bird_fingerprint_need_password", 0);
				}
				finish();
			} else if (pwd.length() == 4) {
                Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);//加载动画资源文件
                mIndication.startAnimation(shake); //给组件播放动画效果
				mPassword.delete(0, mPassword.length());
				showToast(false);
				numberFailCount++;
				if (numberFailCount >= 5) {
				    long  elapsedRealtime = SystemClock.elapsedRealtime() + 30000L;
				    Settings.System.putLong(getContentResolver(), "microarray_over_time", elapsedRealtime);
				    handleOverTime(elapsedRealtime);
				}
			}
		}
		mIndication.setIndex(mPassword.length());
	}
	
	private void handleOverTime(long elapsedRealtimeDeadline) {
	    if (elapsedRealtimeDeadline == 0L || elapsedRealtimeDeadline < SystemClock.elapsedRealtime()) {
	        return;
	    }
	    if (elapsedRealtimeDeadline - SystemClock.elapsedRealtime() > 30000L) {
	        Settings.System.putLong(getContentResolver(), "microarray_over_time", 0L);
	        return;
	    }
	    //此处设置键盘点击无效
	    mNumberPad.setNumberPadClickable(false);
	    new CountDownTimer(elapsedRealtimeDeadline - SystemClock.elapsedRealtime(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //mSummary.setText(getContext().getString(R.string.kgd_passwd_input_error_wait, millisUntilFinished / 1000));
                //此处给用户提示
                tv_notice.setText(getString(R.string.passwd_input_error_wait, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                //此处设置键盘可以点击
                mNumberPad.setNumberPadClickable(true);
                tv_notice.setText(getString(R.string.digital_notice));
                Settings.System.putLong(getContentResolver(), "microarray_over_time", 0L);
                numberFailCount = 0;
            }
        }.start();
	}
	
	public void showToast(boolean flag) {
        String text = null;
		if (flag) {
            text = getString(R.string.fp_tryagain);
		} else {
		    text = getString(R.string.pwd_tryagain);
		}

        if (mToast == null) {
	        mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);  
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
	}
	
	private String getUnlockedApp(String current) {
	    List<String> existLockAppsList = new ArrayList<String>();
	    String temp = "";//*Settings.System.getString(getContentResolver(), Settings.System.MICROARRAY_ALREADY_UNLOCKED_PACKAGESNAME);
	    if (temp != null) {
	        String[] appsStrings = temp.split("\\|");
			existLockAppsList = Arrays.asList(appsStrings);
			if (existLockAppsList.contains(current)) {
			    return temp;
			} else {
			    return temp + current + "|";
			}
	    } else {
	        return "";
	    }
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		    Intent i= new Intent(Intent.ACTION_MAIN); 
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
            i.addCategory(Intent.CATEGORY_HOME); 
            startActivity(i);  
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	protected void onStop() {
        if (isFingerServiceRunning) {
            stopFingerVerify();
        }
        super.onStop();
    }
	
	/*[Finger verify failed vibrator]: TLB liuyang 20150914 begin */ 
	@Override
	protected void onDestroy() {
//		if (null != mVerifySession) {
//			mVerifySession.exit();
//		}
		vibrateStop();
		this.unbindService(conn);
		super.onDestroy();
	}
	/*[Finger verify failed vibrator]: TLB liuyang 20150914 begin */ 
    
    private void handleFingerOvertime() {
        fingerFailCount++;
        if (fingerFailCount >= 3) {
            fingerLayout.setVisibility(View.GONE);
            digitaLayout.setVisibility(View.VISIBLE);
        }
    }
    
    
    private class MyHandler extends Handler {
    
        public void handleMessage(Message msg) {
		    switch (msg.what) {
		    case MSG_VERIFY_SUCCESS:
			    if (msg.arg2 > 0) {
			        Log.i(TAG, "---对比成功---");
			        if (mGlobalPackageNameString != null && !mGlobalPackageNameString.equals("com.finger.clearData")) {
				        //*Settings.System.putString(getContentResolver(), Settings.System.MICROARRAY_LAST_APP_LOCK_PACKAGE_NAME, mGlobalPackageNameString);
				        
				        //这里保存已经解锁过的应用包名
					    String allTemp = getUnlockedApp(mGlobalPackageNameString);
					    Log.i("JTAG", "allTemp = " + allTemp);
					    //*Settings.System.putString(getContentResolver(), Settings.System.MICROARRAY_ALREADY_UNLOCKED_PACKAGESNAME, allTemp);
			        }
				    if (mGlobalPackageNameString.equals("com.finger.clearData")) {
				        Settings.System.putInt(getContentResolver(),  "com_bird_fingerprint_need_password", 0);
				    }
			        finish();
			    } else {
				    Log.i(TAG, "---对比失败---");
				    showToast(true);
				    handleFingerOvertime();
			    }
			    break;
			
		    case MSG_VERIFY_FAILED:
			    Log.i(TAG, "---对比失败---");
			    showToast(true);
			    Animation shake = AnimationUtils.loadAnimation(LockActivity.this,R.anim.shake);
                            findViewById(R.id.id_lockui_fp_iv).startAnimation(shake);
			    handleFingerOvertime();
			    break;
			
		    case MSG_VERIFY_NO_ENROLL:
			    Log.i(TAG, "---没有可用指纹---");
			    break;
			
		    default:
			    break;
		    }
        }
	}
}

