package ma.fprint;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import ma.view.NumberPad;
import ma.view.NumberPad.OnNumberClickListener;
import ma.view.ProcessIndication;
import android.util.Log;
import android.net.Uri;
import android.provider.Settings;
//*import com.android.internal.widget.LockPatternUtils;
import android.app.admin.DevicePolicyManager;
import android.os.RemoteException;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;

import ma.service.IFingerprintService;
/**
 * <p>
 * Title: PasswordActivity
 * </p>
 * <p>
 * Description:
 * </p>
 */


// add some commetn to comfirm change


public class PasswordActivity extends Activity implements OnNumberClickListener{

	public static final String CHECK_PASSWORD = "check.password";
	public static final String CHECK_PASSWORD_RESULT = "check.password.result";
	public static final String CHANGE_PASSWORD = "change.password";
	public static final String CHECK_RETURN = "check.return";
	public static final String START_TYPE_KEY = "start_type";

	private static final String TAG = "PasswdActivity";
	
	private int mIsSafety = 0;
	private StringBuilder mPassword = null;
	private ProcessIndication mIndication = null;
	private NumberPad mNumberPad = null;
	
	private TextView mTextView = null;
	private TextView mTitleTextView = null;
	
	private int mEditPwdCount = 0;//编辑（创建或更新）密码的次数
	private int numberFailCount = 0;//输入密码次数
	private String mFirstPwd = null;
	//*private LockPatternUtils mLockPatternUtils;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passwd);
		initViews();
		initActivity();

		Intent intent = new Intent("ma.service.aidl.IFingerprintService");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	private void initActivity() {
                ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);

		View viewTitleBar = getLayoutInflater().inflate( R.layout.pwd_actionbar_layout, null);
		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(viewTitleBar, lp);
		actionBar.setDisplayShowHomeEnabled(false);// 去掉导航
		actionBar.setDisplayShowTitleEnabled(false);// 去掉标题
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayShowCustomEnabled(true);	

	
		mPassword = new StringBuilder();
		//*mLockPatternUtils = new LockPatternUtils(this);
		numberFailCount = 0;
		
		mIsSafety = Util.isSafety(this);
		if (mIsSafety == 0) {
			//未存在密码，要求新建密码
			mTitleTextView.setText(R.string.input_pwd_title_new);
		} else if (mIsSafety == 1){
			//已经存在密码，要求更新密码
			mTitleTextView.setText(R.string.input_pwd_title_update);
	    } else {
			//已经存在密码，要求输入密码
			mTextView.setText(getString(R.string.input_pwd));
		    handleOverTime(Settings.System.getLong(getContentResolver(), "microarray_over_time_pass", 0));
		}
	}

	private void initViews() {
		mIndication = (ProcessIndication) findViewById(R.id.id_lockui_indication);
		mIndication.mCircleColor = Color.BLACK;
		mNumberPad = (NumberPad) findViewById(R.id.id_lockui_numberpad);
		mNumberPad.setOnNumberClickListener(this);
		
		mTextView = (TextView) findViewById(R.id.id_tv_input_pwd);
		mTitleTextView = (TextView) findViewById(R.id.id_tv_input_pwd_title);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	private void updateLockPass() {
	    String existPass = Util.getPwd(this);
        if (existPass != null && existPass != Util.VALUE_ERROR_PWD) {
            boolean isFallback = false;
            int mRequestedQuality = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
           //* mLockPatternUtils.clearLock(isFallback);
           //*mLockPatternUtils.saveLockPassword(existPass, mRequestedQuality, isFallback);
        }
	}

	@Override
	public void clickNumer(int number) {
		Log.e(TAG, "number : " + number );
		if (number == -2) {
            //just do nothing
        } else if (number == -1) {// 如果点击删除键
            if (mPassword.length() > 0) {
                mPassword.deleteCharAt(mPassword.length() - 1);
            }
		} else {
			if (true) {
				// 如果是其他数字按键
				mPassword.append(number);
				String pwd = mPassword.toString();
				
				if (mIsSafety == 0 || mIsSafety == 1) {//如果是新建密码或者修改密码
					if (pwd.length() == 4) {
						if (mEditPwdCount == 0) {//第一次输入密码
							mFirstPwd = pwd;
							
							mTextView.setText(getString(R.string.input_confirm_pwd));
							mIndication.setIndex(0);
							mPassword.delete(0, mPassword.length());
							mEditPwdCount += 1;
						} else if (mEditPwdCount == 1){//确认密码
							if (mFirstPwd != null && pwd.equals(mFirstPwd)) {
								Util.putPwd(this, pwd);
								Settings.System.putString(getContentResolver(), "com_microarray_fingerprint_password_value", pwd);
								if (true/*Util.isSettingFpScreenLockOn(this)*/) {
								    updateLockPass();//更新安全解锁密码
								}
								mFirstPwd = null;
								Util.setSafety(this, 2);
								Log.e(TAG, "PasswdActivity设置无需密码");
								goToMainActivity();
							} else {
								mIndication.setIndex(0);
								mPassword.delete(0, mPassword.length());
								mTextView.setText(getString(R.string.input_confirm_pwd_error));
							}
						}
					}
				} else {//如果是确认密码输入
					if (pwd.equals(Util.getPwd(this))) {//密码输入正确
						mPassword.delete(0, mPassword.length());
						Log.e(TAG, "PasswdActivity设置无需密码");
						goToMainActivity();
					} else if (pwd.length() == 4) {//密码输入错误
						Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);// 加载动画资源文件
						mIndication.startAnimation(shake); // 给组件播放动画效果
						mPassword.delete(0, mPassword.length());
						numberFailCount++;
						if (numberFailCount >= 5) {
				            long  elapsedRealtime = SystemClock.elapsedRealtime() + 30000L;
				            Settings.System.putLong(getContentResolver(), "microarray_over_time_pass", elapsedRealtime);
				            handleOverTime(elapsedRealtime);
				        }
					}
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
	        Settings.System.putLong(getContentResolver(), "microarray_over_time_pass", 0L);
	        return;
	    }
	    
	    //此处设置键盘点击无效
	    mNumberPad.setNumberPadClickable(false);
	    new CountDownTimer(elapsedRealtimeDeadline - SystemClock.elapsedRealtime(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //此处给用户提示
                mTextView.setText(getString(R.string.passwd_input_error_wait, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                //此处设置键盘可以点击
                mNumberPad.setNumberPadClickable(true);
                mTextView.setText(getString(R.string.input_pwd));
                Settings.System.putLong(getContentResolver(), "microarray_over_time_pass", 0L);
                numberFailCount = 0;
            }
        }.start();
	}
	
	private void goToMainActivity() {
	    Util.enablePwd(this, false);
        Intent intent = new Intent(PasswordActivity.this, ManageFpActivity.class);
        startActivity(intent);
        finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (Util.isSafety(this) == 1) {
		        Util.enablePwd(this, false);
		    }
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onStop() {
		if (!Util.getPwd(this).equals(Util.VALUE_ERROR_PWD)) {
		    Util.setSafety(this, 2);
		}
		
		if (mIsSafety == 0 || mIsSafety == 1) {
		    finish();
		}
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if( null != mFingerService) {
        	try {
				mFingerService.startDetect();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	//if (null != mFPM) mFPM.fingerDetect();
        } 
    	unbindService(conn);
	}

}
