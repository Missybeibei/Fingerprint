package ma.fprint;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.text.TextUtils;
import android.view.View;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import ma.library.EnrollAction;
import ma.library.FingerEnroll;
import ma.library.PhoneStatusBroadcastReceiver;
import ma.release.Fprint;
import ma.service.IFingerprintService;
import ma.view.FingerProcess;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.WindowManager.LayoutParams;
import android.app.ActionBar;
import android.view.MenuItem;

public class AddFpActivity extends Activity implements OnClickListener {

	private static final String TAG = "AddFpActivity --->";
	
	private static final long CANCEL_TIME_INTERVAL = 30000;
	private static final long RELEASE_TIME_INTERVAL = 100;
	
	private EditText mNamEditText = null;
	private ImageView mGuideImageView = null;
	private Button mCancle = null;
	private Button mFinish = null; 
	private Button mRename = null;
	private TextView mGuideTitle = null;
    private TextView mGuideTitleSecond = null; 
    private TextView mGuideContent = null;
    private TextView mGuideContentNext = null;
	private FingerProcess mFingerProcessView = null;
	private LinearLayout mEndLayout = null;
	
	private Vibrator vibrator = null;
	private PowerManager mPowerManager = null;
	private WakeLock mWakeLock = null;
	
	//private EnrollSession mSession;
	private int mFingerUri = -1;
	
	private Handler mCancelHandler = new Handler();
	private CancelRunnable mCancelRunable = new CancelRunnable();
	
	private Handler mReleaseFingerHandler = new Handler();
	private ReleaseTouchRunnable mReleaseFingerRunable = new ReleaseTouchRunnable();
	
	private int mPercent = 0;
	
	private Toast mToast = null;
	
    //add by Young Begin
	private boolean enrollSuccess = false;
	private int curSel = 0;
	
	private final int MSG_NEXT = 0x101;
    private final int MSG_SUCC = 0x102;
    private final int MSG_FAIL = 0x103;
    private final int MSG_DUMP = 0x104; // 清除
    private final int MSG_MOVE = 0x105;
    private final int MSG_GOON = 0x106;
    
    private PhoneStatusBroadcastReceiver mReceiver;
    private FingerEnroll mFingerEnroll = null;
    
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
	//add by Young end
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_fp);		
		curSel = getIntent().getIntExtra("sel", 0);
		
		initViews();
		initListener();
                initActivity();
		initRegister();
		
		//add by Young Begin
        mReceiver = new PhoneStatusBroadcastReceiver(mHandler);
        registerReceiver(mReceiver, PhoneStatusBroadcastReceiver.getFilter());
        mFingerEnroll = new FingerEnroll();
        mFingerEnroll.startEnroll(curSel, new EnrollAction() {
        	
			@Override
			public void anounceNext(int time) {
				System.out.println("hello next");
				Message msg = new Message();
				msg.what = MSG_NEXT;
				msg.arg1 = time;
				mHandler.sendMessage(msg);
			}

			@Override
			public void anounceMove(int time) {
				System.out.println("hello move");
				Message msg = new Message();
				msg.what = MSG_MOVE;
				msg.arg1 = time;
				mHandler.sendMessage(msg);
			}

			@Override
			public void anounceSucessed() {
				System.out.println("hello sucess");
				Message msg = new Message();
				msg.what = MSG_SUCC;
				mHandler.sendMessage(msg);
			}

			@Override
			public void fail() {
				
			}

			@Override
			public void fingerLeave() {
				Message msg = new Message();
				msg.what = MSG_DUMP;
	          mHandler.sendMessage(msg);
			}
        
        });
        
        Intent intent = new Intent("ma.service.aidl.IFingerprintService");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
		//add by Young end
	}
	
	private void initViews() {
	    mGuideImageView = (ImageView) findViewById(R.id.id_add_guide);
	    mCancle = (Button) findViewById(R.id.id_btn_stop);
	    mFinish = (Button) findViewById(R.id.id_btn_finish);
	    mRename = (Button) findViewById(R.id.id_btn_rename);
	    mGuideTitle = (TextView) findViewById(R.id.id_add_guide_title);
        mGuideTitleSecond = (TextView) findViewById(R.id.id_add_guide_title_second);
        mGuideContent = (TextView) findViewById(R.id.id_add_guide_content);
        mGuideContentNext = (TextView) findViewById(R.id.id_add_guide_content_next);
	    mFingerProcessView = (FingerProcess) findViewById(R.id.id_fp_process);
	    mEndLayout = (LinearLayout) findViewById(R.id.id_ll_setup_end);
	}
	
	private void initListener() {
	    mCancle.setOnClickListener(this);
	    mFinish.setOnClickListener(this);
	    mRename.setOnClickListener(this);
	}

    private void initActivity() {
                ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.fingerprint_add);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true); // 可以显示标题栏
                actionBar.setDisplayShowHomeEnabled(false);//actionBar左侧图标是否显示
		actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);

        mGuideImageView.setBackgroundResource(R.drawable.guide_anim);
		AnimationDrawable anim = (AnimationDrawable) mGuideImageView.getBackground();
		anim.stop();
		anim.start();
		setCurrentStep(0);
		
		this.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		this.mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.mWakeLock = this.mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "my lock");
        startCancelTimer();
    }

    private void initRegister() {
//		if (null == mSession) {
//			mSession = FpApplication.getInstance().getFpServiceManager().newEnrollSession(mEnrollCallback);
//		}
//		mSession.enter();
	}
	
	public void showToast() {
        String text = getString(R.string.register_no_extra_info);

        if (mToast == null) {
	        mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }
        mToast.show();
	}
	
	private void setCurrentStep(int step) {
	    if (step < 4) {
            mGuideTitle.setText(R.string.record_fp_title_positive);
            mGuideTitleSecond.setText(R.string.record_fp_content_first_positive);
        } else if (step == 4) {
            mGuideTitle.setText(R.string.record_fp_title_left);
            mGuideTitleSecond.setText(R.string.record_fp_content_left);
        } else if (step == 5) {
            mGuideTitle.setText(R.string.record_fp_title_left_top);
            mGuideTitleSecond.setText(R.string.record_fp_content_left_top);
        } else if (step == 6) {
            mGuideTitle.setText(R.string.record_fp_title_right_top);
            mGuideTitleSecond.setText(R.string.record_fp_content_right_top);
        } else if (step == 7) {
            mGuideTitle.setText(R.string.record_fp_title_right);
            mGuideTitleSecond.setText(R.string.record_fp_content_right);
        }
		mFingerProcessView.twikcleImg(this, step);
	}

    /*
     *如果用户30秒之后还没有放手指，则注册失败
     */
	private void startCancelTimer() {
		if (null != mCancelHandler && null != mCancelRunable) {
			mCancelHandler.postDelayed(mCancelRunable, CANCEL_TIME_INTERVAL);
		}
	}
	
	private void cancelCancelTimer() {
		Log.v(TAG, "cancelCancelTimer");
		if (null != mCancelHandler && null != mCancelRunable) {
			mCancelHandler.removeCallbacks(mCancelRunable);
		}
	}

	private void resetCancelTimer() {
		Log.v(TAG, "---resetCancelTimer---");
		if (null != mCancelHandler && null != mCancelRunable) {
			mCancelHandler.removeCallbacks(mCancelRunable);
			mCancelHandler.postDelayed(mCancelRunable, CANCEL_TIME_INTERVAL);
		}
	}
	
	/*
     *提示用户请抬起手指
     */
	private void startReleaseFingerTimer() {
		Log.v(TAG, "startReleaseFingerTimer");
		if (null != mReleaseFingerHandler && null != mReleaseFingerRunable) {
			mReleaseFingerHandler.postDelayed(mReleaseFingerRunable, RELEASE_TIME_INTERVAL);
		}
	}

	private void cancelReleaseFingerTimer() {
		Log.v(TAG, "cancelReleaseFingerTimer");
		if (null != mReleaseFingerHandler && null != mReleaseFingerRunable) {
			//此处提示用户继续注册，去掉抬起手指
			mReleaseFingerHandler.removeCallbacks(mReleaseFingerRunable);
			if (mPercent < 100) {
			    mGuideContentNext.setText(R.string.record_fp_content_remove_first);
			}
		}

	}

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            
                if (mPercent >= 100) {
                    CaptureResult(String.format(getResources().getString(R.string.record_default_name), mFingerUri));
                } else {
                    setResult(-100);
                    Util.enablePwd(this, false);
                }
                //exit();
                finish();
	    }
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {  
		switch (v.getId()) {
		case R.id.id_btn_stop:
		    setResult(-100);
		    Util.enablePwd(this, false);
		    //exit();
			finish();
		    break;
		case R.id.id_btn_finish:
    		//CaptureResult(String.format(getResources().getString(R.string.record_default_name), mFingerUri));
    		//exit();
            finish();
		    break;
		case R.id.id_btn_rename:
		    createRenameDialog();
		    break;
		default:
			break;
		}
	}
	
	private void CaptureResult(String name) {
	    Util.enablePwd(this, false);
		exit();
	}

        @Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {

		case android.R.id.home:
			Util.enablePwd(this, false);
                        finish();
			break;

		default:
			break;
		}

		return true;
	}

    private void createRenameDialog() {
		mNamEditText = new EditText(this);
		mNamEditText.setTextColor(0xff000000);
		mNamEditText.setBackgroundResource(R.drawable.edittext_bg);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		mNamEditText.setText(String.format(getResources().getString(R.string.record_default_name), mFingerUri));
		mNamEditText.setGravity(Gravity.CENTER_HORIZONTAL);
		mNamEditText.setPadding(15, 15, 15, 15);
		builder.setTitle(R.string.fp_name).setView(mNamEditText)
				.setPositiveButton(R.string.confirm, confirmListener)
				.setNegativeButton(R.string.cancel, null).create().show();
	}
	
	private DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		    String newName = mNamEditText.getText().toString().trim();
		    /*判断输入的重命名是否为空*/
		    if (!TextUtils.isEmpty(newName)) {
			    CaptureResult(mNamEditText.getText().toString());
		    } else {
		        //showToast();
		    }
		}
	};

    @Override
	protected void onResume() {
		super.onResume();
		this.mWakeLock.acquire();
	}
    
    @Override
	protected void onPause() {
	    Log.v(TAG, "---onPause---");
	    this.mWakeLock.release();//取消屏幕常亮
	    
		//cancelCancelTimer();
	    //cancelReleaseFingerTimer();
	  
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		Log.v(TAG, "---onStop---");
		//cancelCancelTimer();
	    //cancelReleaseFingerTimer();
		super.onStop();
		exit();
                //finish();
	}
	
    @Override
	protected void onDestroy() {
	    Log.v(TAG, "---onDestroy---");
		super.onDestroy();
                unregisterReceiver(mReceiver);
        unbindService(conn);
	}
	
	private class CancelRunnable implements Runnable {

		@Override
		public void run() {
//			try {
//				if (null != mSession) {
//					mSession.exit();
//				}
//				//此处提示注册失败
//				//startWarning(R.string.register_register_failed);
//				doWhenOverTime();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}
	
	private void doWhenOverTime() {
	    Log.v(TAG, "...提示注册失败...");
	    mGuideTitle.setText(R.string.register_register_failed);
		mGuideTitleSecond.setVisibility(View.INVISIBLE);
		mGuideContent.setVisibility(View.INVISIBLE);
	}
	
	private class ReleaseTouchRunnable implements Runnable {

		@Override
		public void run() {
			Log.v(TAG, "ReleaseTouchRunnable:Run...");
			//此处提示请抬起手指
			//startWarning(R.string.register_notice_the_hand);
			mGuideContentNext.setText(R.string.record_fp_content_remove);
			remindUserHangup(mGuideContentNext, mGuideContent);
		}
	}
	
	private void remindUserHangup(TextView enterView,TextView exitView) {
		Animation exitanimation = AnimationUtils.loadAnimation(this, R.anim.register_title_text_exit);
		Animation enteranimation = AnimationUtils.loadAnimation(this, R.anim.register_title_text_enter);
		exitanimation.setAnimationListener(new TitleExitAnimListener(exitView, View.GONE));
		enteranimation.setAnimationListener(new TitleExitAnimListener(enterView, View.VISIBLE));
		exitView.startAnimation(exitanimation);
		enterView.startAnimation(enteranimation);
	}
	
	private class TitleExitAnimListener implements AnimationListener {
	
		TextView mView;
		int mVisible;
		public TitleExitAnimListener(TextView view, int visible) {
			mView = view;
			mVisible = visible;
		}

		@Override
		public void onAnimationEnd(Animation arg0) {
			
			if (null != mView) {
				mView.setVisibility(mVisible);
			}
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
		}

		@Override
		public void onAnimationStart(Animation arg0) {
		}
	}
	
	private void startVibrator() {
        if (vibrator != null) {
            vibrator.vibrate(100);
        }
        if (mToast != null) {
            mToast.cancel();
        }
	}
	
	private void updateViewsWhenFinish() {
        mGuideTitle.setText(R.string.setup_end);
        mGuideTitleSecond.setText("");
        mGuideContentNext.setText(R.string.record_successfully);
        mEndLayout.setVisibility(View.VISIBLE);
        mCancle.setVisibility(View.GONE);
        mFingerProcessView.twikcleImg(this, 8);
	}
	
	private void exit() {
            Fprint.start();
            //if (!enrollSuccess) Fprint.clear(curSel + 1);
            mFingerEnroll.stopEnroll();
        
            if( null != mFingerService) {
           	try {
   				mFingerService.startDetect();
   			} catch (RemoteException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   			}
           	//if (null != mFPM) mFPM.fingerDetect();
            }
        
            finish();
       }
	
	private Handler mHandler = new Handler() {
	
	    private boolean bToutch = false;
	    int idx;
	    
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_NEXT :
				    startVibrator();
				    idx = ((msg.arg1 / 10) % (Fprint.ENROLL_GRADE / 10)) * 5 / 6;
					
					//if (mPercent < 100 && mPercent > 0) {
					    mGuideImageView.setVisibility(View.GONE);
					    mFingerProcessView.setVisibility(View.VISIBLE);
					  
				        setCurrentStep(idx);
				        
				        startReleaseFingerTimer();
					//} else if (mPercent >= 100) {
//					    updateViewsWhenFinish();//更新界面
//					    mFingerUri = msg.arg1;
//					    //setCurrentStep(8);
//						
//						cancelReleaseFingerTimer();
//						cancelCancelTimer();
					//}
				    break;
				case MSG_MOVE :
					//startVibrator();
					showToast();
					break;
				case MSG_DUMP :
				    bToutch = false;
				   // cancelReleaseFingerTimer();
					break;
				case MSG_SUCC:
					enrollSuccess = true;
					updateViewsWhenFinish();//更新界面
				    //mFingerUri = msg.arg1;
				    //setCurrentStep(8);
					
					//cancelReleaseFingerTimer();
					//cancelCancelTimer();
					break;
				case PhoneStatusBroadcastReceiver.screen_off:
                case PhoneStatusBroadcastReceiver.key_home:
                    exit();

				default :
					break;
			}
		}
	};
}
