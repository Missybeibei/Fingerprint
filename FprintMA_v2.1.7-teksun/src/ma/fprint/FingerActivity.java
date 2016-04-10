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
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.app.admin.DevicePolicyManager;
import android.os.Message;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.widget.Toast;
import ma.release.Fprint;

import ma.service.IFingerprintService;
import ma.service.*;
/**
 * <p>
 * Title: FingerActivity
 * </p>
 * <p>
 * Description:
 * </p>
 */
public class FingerActivity extends Activity implements OnClickListener {

	private ListView mListView = null;
	private Switch mScreenSwitch = null;
	private Switch mCallScreenSwitch = null;/*长按指纹接听电话：mandy.wu 20151029*/
	private LinearLayout mFingerAct = null;

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
 
    private Handler calibrationHandler = new Handler() {  
		   
        @Override  
        public void handleMessage(Message msg) { 
                Intent calibrateIntent = new Intent(FingerActivity.this,
                    FactoryActivity.class);
                startActivity(calibrateIntent);

                Util.writeXML(getApplicationContext(), "calibrated", 1);

        }  
    };  

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finger);
                ActionBar actionBar = getActionBar();
           actionBar.setHomeButtonEnabled(false);
          // actionBar.setDisplayShowTitleEnabled(true); // 可以显示标题栏
           actionBar.setDisplayShowHomeEnabled(false);//actionBar左侧图标是否显示
          // actionBar.setDisplayHomeAsUpEnabled(true);
           actionBar.setDisplayUseLogoEnabled(false);

		initViews();
		initListener();
		/* go back Home：mandy.wu 20151029 begin */
		boolean isLongPress = Util.readXML(getApplicationContext(),Util.SWITCH_LONG_TAP, 0) == 1 ? true : false;
		mCallScreenSwitch.setChecked(isLongPress);
		
		/* go back Home：mandy.wu 20151029 end */
		boolean isSingleKey = Util.readXML(getApplicationContext(),Util.SWITCH_SINGLE_TAP, 0) == 1 ? true : false;
		mScreenSwitch.setChecked(isSingleKey);

		Intent it = new Intent(FingerActivity.this, MatchService.class);
        startService(it);
		//Intent intent = new Intent("ma.service.aidl.IFingerprintService");
        //bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

        @Override
        protected void onResume() {
            super.onResume();
            int mRet = Fprint.open();

            mRet = Fprint.load();
            if (mRet == -1) {
                String text = getString(R.string.ma_factory_prompt);
                Toast mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                mToast.show();

                calibrationHandler.sendEmptyMessageDelayed(0, 1000);
            }


        }

	@Override 
   	protected void onDestroy() {
   		// TODO Auto-generated method stub
   		super.onDestroy(); 
       	//unbindService(conn);
   		
   	}

	private void initViews() {
		mFingerAct = (LinearLayout) findViewById(R.id.manage_finger_act);
		mScreenSwitch = (Switch) findViewById(R.id.manage_screen_off_on_phone);
		mCallScreenSwitch = (Switch) findViewById(R.id.manage_screen_off_on_call);/* 长按指纹接听电话：mandy.wu20151029*/
	}

	private void initListener() {
		mFingerAct.setOnClickListener(this);
		mScreenSwitch.setOnCheckedChangeListener(mScreenSwitchCheckedListener);
		mCallScreenSwitch
				.setOnCheckedChangeListener(mScreenSwitchCheckedListener);/* 长按指纹接听电话：mandy.wu20151029*/
	}

	@Override
	public void onClick(View v) {
		Log.d("kcw121", "5465");
		switch (v.getId()) {
		case R.id.manage_finger_act:
			Intent intent = new Intent(this, PasswordActivity.class);
			startActivityForResult(intent, 2);
			break;

		default:
			break;
		}
	}

	private OnCheckedChangeListener mScreenSwitchCheckedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (buttonView == mCallScreenSwitch) {
				if (isChecked) {
					//Settings.System.putInt(getContentResolver(), "fingercall",1);
					Util.writeXML(getApplicationContext(), Util.SWITCH_LONG_TAP, 1);
				} else {
					//Settings.System.putInt(getContentResolver(), "fingercall",0);
					Util.writeXML(getApplicationContext(), Util.SWITCH_LONG_TAP, 0);
				}
			} else if(buttonView == mScreenSwitch){
				if (isChecked) {
					//Settings.System.putInt(getContentResolver(), "fingerphone",1);
					Util.writeXML(getApplicationContext(), Util.SWITCH_SINGLE_TAP, 1);
				} else {
					//Settings.System.putInt(getContentResolver(), "fingerphone",0);
					Util.writeXML(getApplicationContext(), Util.SWITCH_SINGLE_TAP, 0);
				}
			}
			
		}
	};
}
