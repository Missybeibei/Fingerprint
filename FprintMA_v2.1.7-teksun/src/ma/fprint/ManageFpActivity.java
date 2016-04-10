package ma.fprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.content.Context;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import ma.release.Fprint;
import ma.service.IFingerprintService;
import ma.service.MatchService;
//*import com.android.internal.widget.LockPatternUtils;
import android.app.admin.DevicePolicyManager;
import android.widget.Toast;
import android.app.ActionBar;

public class ManageFpActivity extends Activity implements OnClickListener{
	
	private String TAG = "ManageFpActivity-->";
	
	/*add git supporrt*/
	
	private ListView mListView = null;
	//private Switch mScreenSwitch = null;
	private ToggleButton mScreenSwitch = null;
    private Switch mPMScreenSwitch = null;
	private TextView mModifyPassTv = null;
	//add by kuangchaowei 20151008 begin//
	
	//add by kuangchaowei 20151008 end
	private TextView mAppLockTv = null;
	private TextView mAddFpTv = null;
	
	//add by Young begin
	private byte[] bState = new byte[5];
	private ListView listView = null;
	private boolean bUnlock = false;
	private boolean isPMlock = false;
	private ArrayList<HashMap<String, Object>> mList;
	private int mRet = 0;
	private int curSel;
	private TextView mFingerCalibration = null;
	
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
	
	//private static CommonAdapter<Fingerprint> mListAdapter;
	
	/* Message handler */
	//private MyHandler mHandler;
	
	/* The list of fingerprint */
	//private ArrayList<Fingerprint> mDataList = new ArrayList<Fingerprint>();
	
	//private FpHandleServiceConnection mHandleServiceConn;
	
	/* Database service */
	//private FingerprintHandleService mFingerPrintHandleService;
	
	//*private LockPatternUtils mLockPatternUtils;
	
	/* remote service connected */
	private static final int MSG_SERVICE_CONNECTED = 1;
	/* fingerprint data is ready */
	private static final int MSG_DATA_IS_READY = 2;
	/* verify success */
	private static final int MSG_VERIFY_SUCCESS = 3;
	/* verify failed */
	private static final int MSG_VERIFY_FAILED = 4;

	private static final int MSG_DEBUG_INFO = 5;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_fp);
        initViews();
        initListener();
		initActivity();
	}
	
	@Override
	protected void onResume() {
	    Log.i("JTAG", "---onResume---");
		super.onResume();
		boolean mNeedPasswd = Util.isNeedPwd(this);
		if (mNeedPasswd) {
		    gotoPasswordAct();
		} else {
		}
		
		 Util.writeXML(getApplicationContext(), "pmlock", 1);
		
		//add by Young Begin
		bUnlock = (Util.readXML(getApplicationContext(), "lock", 0) == 1) ? true
                : false;
        mScreenSwitch.setChecked(bUnlock);

		isPMlock = (Util.readXML(getApplicationContext(), "pmlock", 0) == 1) ? true
                : false;
        mPMScreenSwitch.setChecked(isPMlock);
 
        mRet = Fprint.open();
        if (mRet == -1) {
            Prompt.e(this, getResources().getString(R.string.ma_dlg_device));
        } else if (mRet == -2) {
            Prompt.e(this, getResources().getString(R.string.ma_dlg_memory));
        } else if (mRet == -3) {
            Prompt.e(this, getResources().getString(R.string.ma_dlg_dbase));
        } else if (mRet == -4) {
            Prompt.e(this, getResources().getString(R.string.ma_dlg_table));
        }
        
        mRet = Fprint.load();
        if (mRet == -1) { 
            Intent calibrateIntent = new Intent(ManageFpActivity.this,
                    FactoryActivity.class);
            startActivity(calibrateIntent);

            String text = getString(R.string.ma_factory_prompt);
            Toast mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            mToast.show();

            Util.writeXML(getApplicationContext(), "calibrated", 1);
        } else if (mRet == -3) {
            Prompt.w(this,
                    getResources().getString(R.string.ma_dlg_not_capture));
        }

        if (mRet >= 0)
            initList();
        
        
        Intent intent = new Intent("ma.service.aidl.IFingerprintService");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        
        fingerDetectResume();
      //add by Young end
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	private void gotoPasswordAct() {
	    Intent intent = new Intent(this, PasswordActivity.class);
	    startActivity(intent);
	    finish();
	}
	
	private void initActivity() {
           ActionBar actionBar = getActionBar();
           actionBar.setTitle(R.string.fingerprint_manager);
           actionBar.setHomeButtonEnabled(false);
           actionBar.setDisplayShowTitleEnabled(true); // 可以显示标题栏
           actionBar.setDisplayShowHomeEnabled(false);//actionBar左侧图标是否显示
           actionBar.setDisplayHomeAsUpEnabled(true);
           actionBar.setDisplayUseLogoEnabled(false);

	   //*mLockPatternUtils = new LockPatternUtils(this);
	}
	
	private void initViews() {
		//add by kuangchaowei 20151008 begin
		mFingerCalibration = (TextView) findViewById(R.id.id_fp_calibration);
		//add by kuangchaowei 20151008 end
		mModifyPassTv = (TextView) findViewById(R.id.id_manage_pwd_tv);
		mAppLockTv = (TextView) findViewById(R.id.id_manage_applock_tv);
		mAddFpTv = (TextView)findViewById(R.id.id_manage_addfp_tv);
		
		mListView = (ListView) findViewById(R.id.id_lv_finger);
		mScreenSwitch = (ToggleButton) findViewById(R.id.id_manage_screen_off_on_sw);
		mScreenSwitch.setChecked(Util.isSettingFpScreenLockOn(this));

		mPMScreenSwitch = (Switch) findViewById(R.id.id_manage_screen_off_on_pm);
		isPMlock = (Util.readXML(getApplicationContext(), "pmlock", 0) == 1) ? true
                : false;
        mPMScreenSwitch.setChecked(isPMlock);
	}
	
	
	private OnClickListener switchClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (!hasFingerData()) {
				Intent intent = new Intent(ManageFpActivity.this, AddFpActivity.class);
                startActivityForResult(intent, 4);
			}
		}
	};
	
	private OnCheckedChangeListener mPMScreenSwitchCheckedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Util.setSettingFpScreenLockOffOn(ManageFpActivity.this, isChecked);
			if (isChecked) {
                Util.writeXML(getApplicationContext(), "pmlock", 1);
            } else {
                Util.writeXML(getApplicationContext(), "pmlock", 0);
             
            }
		}
	};

	private OnCheckedChangeListener mScreenSwitchCheckedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Util.setSettingFpScreenLockOffOn(ManageFpActivity.this, isChecked);
			if (isChecked) {
                Log.d(TAG, "onCheckedChanged");
                Util.writeXML(getApplicationContext(), "lock", 1);
                updateLockPass();/*密码解锁*/
				mPMScreenSwitch.setEnabled(true);
            } else {
                Log.d(TAG, "onCheckedChanged ---else---");
                Util.writeXML(getApplicationContext(), "lock", 0);
                //*mLockPatternUtils.clearLock(false);/*无密码解�?*/
				mPMScreenSwitch.setEnabled(false);
            }
		}
	};
	
	private void updateLockPass() {
	    String existPass = Util.getPwd(this);
        if (existPass != null && existPass != Util.VALUE_ERROR_PWD) {
            boolean isFallback = false;
            int mRequestedQuality = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
            //*mLockPatternUtils.clearLock(isFallback);
            //*mLockPatternUtils.saveLockPassword(existPass, mRequestedQuality, isFallback);
        }
	}
	
	private void initListener() {
		//add by kuangchaowei 20151008 begin
		mFingerCalibration.setOnClickListener(this);
		//add by kuangchaowei 20151008 end
	    mModifyPassTv.setOnClickListener(this);
	    mAppLockTv.setOnClickListener(this);
	    mAddFpTv.setOnClickListener(this);
	    mScreenSwitch.setOnClickListener(switchClickListener);
	    mScreenSwitch.setOnCheckedChangeListener(mScreenSwitchCheckedListener);
     
	    mPMScreenSwitch.setOnCheckedChangeListener(mPMScreenSwitchCheckedListener);
	}
	
	/*private void startDetailActivity(final Fingerprint item) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putParcelable("ITEM_KEY_FINGERPRINT", item);
		intent.putExtras(bundle);
		intent.setClass(this, FpDetail.class);
		startActivityForResult(intent, 1);
	}*/
	
	private void handleFromAddFp(Intent data) {
	   /* int mKey = getKey(mDataList);
	    Bundle bundle = data.getExtras();
	    String name = bundle.getString("FINGER_NAME");;
	    String description = "bird_description";
	    String uri = bundle.getInt("FINGER_URI") + "";
	    Fingerprint fp = new Fingerprint(mKey, name, description, uri);
	    if (mFingerPrintHandleService != null && mFingerPrintHandleService.insert(fp)) {
			mDataList.add(fp);
	        startInitFingerprintThread();
		} else {
			Toast.makeText(this, getResources().getString(R.string.addfinger_faied_toast), Toast.LENGTH_SHORT).show();
		}*/
	}
	
	
    private void gotoModifyPw() {
        Util.setSafety(this, 1);
        Intent intent = new Intent(this, PasswordActivity.class);
		startActivity(intent);
    }

    private void startLoadAllApp() {
    	Intent intent = new Intent();
		intent.setClass(this, EnableLockAppActivity.class);
		startActivity(intent);
    }
    
    private void loadAllApp() {
        Intent intent = new Intent();
		intent.setClass(this, EnableLockAppActivity.class);
		startActivity(intent);
    }

    @Override
	public void onClick(View v) {
    	String text;
        Toast mToast;

        switch (v.getId()) {
        case R.id.id_fp_calibration:
        	fingerDetectPause();
        	
            //Util.writeXML(getApplicationContext(), "lock", 0);
            Intent it = new Intent(ManageFpActivity.this, MatchService.class);
            stopService(it);
            Intent calibrateIntent = new Intent(ManageFpActivity.this,
                    FactoryActivity.class);
            startActivity(calibrateIntent);

             text = getString(R.string.ma_factory_prompt);
             mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
             mToast.show();

              break;    
        case R.id.id_manage_pwd_tv:
            gotoModifyPw();
            break;
        
        case R.id.id_manage_applock_tv://应用加密
            startLoadAllApp();
            break;
        
        case R.id.id_manage_addfp_tv://新建指纹
        	int i;
        	for (i = 0; i < bState.length; i++) {
                if ((bState[i] & 0xff) == 0)
                    break;
            }
            if (i < bState.length) {
            	fingerDetectPause();
            	
                Intent enrollIntent = new Intent(ManageFpActivity.this,
                        AddFpActivity.class);
                enrollIntent.putExtra("sel", i);
                startActivity(enrollIntent);
            } else {
             //   Prompt.w(this,
             //           getResources().getString(R.string.ma_dlg_finger_full));
                 text = getString(R.string.ma_dlg_finger_full);

                 mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                 mToast.show();
            }
            break;
        
        default:
            break;    
        }		
	}

        @Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {

		case android.R.id.home:
                        Util.enablePwd(this, true);
			finish();
			break;

		default:
			break;
		}

		return true;
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		Log.e(TAG, "onActivityResult---requestCode:" + requestCode + "---resultCode:" + resultCode);
		if (requestCode == 1 && resultCode == RESULT_OK) {//从FpDetail返回到界�?		    
              //startInitFingerprintThread();
		} else if (requestCode == 2 && resultCode == RESULT_OK) {//新建指纹---从注册界面返�?		    
              handleFromAddFp(data);
		} else if (requestCode == 3 && resultCode == RESULT_OK) {//应用加密---从注册界面返�?		    
            handleFromAddFp(data);
		    loadAllApp();
		} else if (requestCode == 4) {//屏幕解锁---从注册界面返�?		    
			if (resultCode == RESULT_OK) {
		        handleFromAddFp(data);
		        mScreenSwitch.setChecked(Util.isSettingFpScreenLockOn(this));
		    } else if (resultCode == -100) {
		        mScreenSwitch.setChecked(false);
		    }
		}
	}
	
	private void updateFingerprintItemsView() {
		//if (null == mDataList) {
		//	return;
		//}
		//initAdapter();
		/*int count = mDataList.size();
        Log.i(TAG, "---updateFingerprintItemsView---" + count);
        for(int i = 0; i<count; i++) {
           // Fingerprint item = mDataList.get(i);
            Log.i(TAG, i + "---name---" + item.name);
        }
        
        //mListAdapter.notifyDataSetChanged();//更新listview数据
       
         if (count == 0) {
            doWhenNoFingerCount();    /*[bug-2499]设置--应用安裑与管�?-应用管理里面把指纹识别应用清除数据后，之前建立的指纹没有被清�?mandy.wu 20150919
        }
        
        if (count >= 4) {
            mAddFpTv.setEnabled(false);
            mAddFpTv.setTextColor(Color.GRAY);
        } else {
            mAddFpTv.setEnabled(true);
            mAddFpTv.setTextColor(Color.RED);
        }*/
	}
	/*[bug-2499]设置--应用安裑与管�?-应用管理里面把指纹识别应用清除数据后，之前建立的指纹没有被清�?mandy.wu 20150919 begin*/
	private void doWhenNoFingerCount() {
       // Settings.System.putString(getContentResolver(), Settings.System.BIRD_LAST_APP_LOCK_PACKAGE_NAME, "");
        mScreenSwitch.setChecked(false);
        //Preferences.setSettingFpAppLockOffOn(this, false);
        //Preferences.setSettingFpScreenLockOffOn(this, false);
       //* mLockPatternUtils.clearLock(false);
	}
	/*[bug-2499]设置--应用安裑与管�?-应用管理里面把指纹识别应用清除数据后，之前建立的指纹没有被清�?mandy.wu 20150919 end*/
	private class MyHandler extends Handler {

		public MyHandler() {
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SERVICE_CONNECTED:
//				if (null != mFingerPrintHandleService) {
//					startInitFingerprintThread();
//				}
				break;
			case MSG_DATA_IS_READY: {
				if (msg.obj instanceof ArrayList) {
				    //mDataList.clear();
				    //mDataList.addAll((ArrayList<Fingerprint>) msg.obj);
					/* update view */
					updateFingerprintItemsView();
					/*if (null == mSession) {
						mSession = FpApplication.getInstance().getFpServiceManager().newVerifySession(mVerifyCallBack);
						mSession.enter();
					}*//*[bug-2499]设置--应用安裑与管�?-应用管理里面把指纹识别应用清除数据后，之前建立的指纹没有被清�?mandy.wu 20150919 begin*/
				} else if (msg.obj == null) {
				    //if (mDataList != null && mDataList.size() > 0) {
				     //   mDataList.clear();
				    //}
				    if (mListView != null) {
				        mListView.removeAllViewsInLayout();
				    }
				    
				    doWhenNoFingerCount();
				}
				/*[bug-2499]设置--应用安裑与管�?-应用管理里面把指纹识别应用清除数据后，之前建立的指纹没有被清�?mandy.wu 20150919 end*/
			}
				break;
			/*case MSG_VERIFY_SUCCESS:
				showMatchedMessage(mIsEnableShow,msg.arg2, msg.arg1);
				if (msg.arg2 > 0) {
					showMatchedAnimation(msg.arg2);
				} else {
					unmatchAnimation(mTouchIDListPanel);
				}
				break;

			case MSG_VERIFY_FAILED:
				showMatchedMessage(mIsEnableShow,msg.arg2, msg.arg1);
				unmatchAnimation(mTouchIDListPanel);
				break;

			case MSG_DEBUG_INFO:
				byte[] data = (byte[]) msg.obj;
				if (data != null) {
					String str = new String(data);
					if (AlgoResult.isFilePath(str)) {
						mTopView.setText(AlgoResult.bulidLog(str, AlgoResult.FILTER_RECOGNIZE,getViewCount()));
					}
					mBehandView.setText(AlgoResult.bulidLog(str, AlgoResult.FILTER_RECOGNIZE,getViewCount()));

					int index = str.indexOf("=");

					if (-1 != index) {
						String fileName = null;
						fileName = str.substring(index + 1, str.length() - 1);
						File file = new File(fileName);
						try {
							InputStream in = new FileInputStream(file);
							Bitmap map = BitmapFactory.decodeStream(in);
							mImageOne.setImageBitmap(map);
							in.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
            */
			default:
				break;
			}
		}
	}
	
	private void startInitFingerprintThread() {
		new InitFingerprintThread().start();
	}

    private class InitFingerprintThread extends Thread {
		public void run() {
//			if (null != mFingerPrintHandleService && null != FpApplication.getInstance().getFpServiceManager()) {
//				int flag = FpApplication.getInstance().getFpServiceManager().query();
//				ArrayList<Fingerprint> dataList = loadData(mFingerPrintHandleService.query(), flag);
//				mHandler.sendMessage(Message.obtain(mHandler, MSG_DATA_IS_READY, 0, 0, dataList));
			//}
		}
	}
	
	/*private ArrayList<Fingerprint> loadData(ArrayList<Fingerprint> dataList, int fpFlag) {
		if (null == dataList || (fpFlag >> 16) <= 0) {
			return null;
		}
		ArrayList<Fingerprint> tempList = new ArrayList<Fingerprint>();
		int mKey = getKey(dataList);
		int count = (fpFlag >> 16 & 0xFFFF);
		/*[bug-2499]设置--应用安裑与管�?-应用管理里面把指纹识别应用清除数据后，之前建立的指纹没有被清�?mandy.wu 20150919 begin
		if (dataList.size() == 0) {
		    for (int i = 0; i < count; i++) {
                try {
                    FpApplication.getInstance().getFpServiceManager().delete(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
		    }
		    return null;
		}
		/*[bug-2499]设置--应用安裑与管�?-应用管理里面把指纹识别应用清除数据后，之前建立的指纹没有被清�?mandy.wu 20150919 end
		boolean[] bRegister = new boolean[count];
		int index = -1;

		for (int i = 0; i < count; i++) {
			bRegister[i] = (((fpFlag >> i) & 0x1) > 0) ? true : false;
			if (bRegister[i] == true) {
				boolean bFind = false;
				for (int j = 0; j < dataList.size(); j++) {
				    index = Integer.parseInt(dataList.get(j).getUri());
					if (index == i + 1) {
						Fingerprint fp = dataList.remove(j);
						tempList.add(fp);
						bFind = true;
						break;
					}
				}
				if (bFind == false) {
				    String fingerName = String.format(getResources().getString(R.string.record_default_name), i + 1);
					Fingerprint fp = new Fingerprint(mKey, fingerName, fingerName, Integer.toString(i + 1));
					mKey++;
					tempList.add(fp);
					this.mFingerPrintHandleService.insert(fp);
				}
			}
		}

		for (int i = 0; i < dataList.size(); i++) {
			this.mFingerPrintHandleService.delete(dataList.get(i).getKey());
		}
		return tempList;
	}*/
	
//	private int getKey(ArrayList<Fingerprint> dataList) {
//		int mKey = 0;
//		for (int i = 0; i < dataList.size(); i++)
//			mKey = Math.max(dataList.get(i).getKey(), mKey);
//		return ++mKey;
//	}
	
        @Override
	protected void onDestroy() {
	    super.onDestroy();
            Util.enablePwd(this, true);
            unbindService(conn);
	}
    
    //add by Young Begin
    private void initList() {
        Fprint.doState(bState);
        mList = new ArrayList<HashMap<String, Object>>();
        setListItem(mList);
        SimpleAdapter adapter = new SimpleAdapter(this, mList,
                R.layout.ma_set_finger_item, new String[] {
                        "img", "name", "info",
                        "btn"
                }, new int[] {
                        R.id.ma_item_img,
                        R.id.ma_item_name,
                        R.id.ma_item_info, R.id.ma_item_cbox
                });
        listView = (ListView) findViewById(R.id.id_lv_finger);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
				RelativeLayout layout = (RelativeLayout)arg1;
                //RelativeLayout layout = (RelativeLayout) listView
                //        .getChildAt(arg2);
                TextView txt = (TextView) layout.getChildAt(2);
                String str = txt.getText().toString();
                char num = str.charAt(str.length() - 1);
                int fid = num - '0';
                txt = (TextView) layout.getChildAt(1);
                str = txt.getText().toString();
                if ((bState[fid - 1] & 0xff) > 0) {
                    curSel = arg2;
                    //add for bird begin
                    Intent intent = new Intent(ManageFpActivity.this,FpDetail.class);
                    intent.putExtra("finger_ID", fid);
            		intent.putExtra("finger_name",str);
            		startActivity(intent);
            		//add for bird end
            		
                    //promptC(getResources().getString(R.string.ma_dlg_del_touch));
                    return;
                }
            }
        });
    }
    
    
    private void setListItem(ArrayList<HashMap<String, Object>> list) {
        for (int i = 0; i < bState.length; i++) {
            if ((bState[i] & 0xff) == 0)
                continue;
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("img", R.drawable.ic_ma_touch_id);
            int rid = R.drawable.arrow_right;//(bState[i] & 0xff) > 0 ? R.drawable.ic_ma_checked : R.drawable.ic_ma_unchecked;
            item.put("btn", rid);
            String name = Util.getFingerName(getApplicationContext(), i + 1);
            item.put("name", name);
            String info = getResources().getString(R.string.ma_enroll_fid)
                    + Integer.toString(i + 1);
            item.put("info", info);
            list.add(item);
        }
    }
    
    public void fingerDetectResume(){
    	if( null != mFingerService) {
           	try {
   				mFingerService.startDetect();
   			} catch (RemoteException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   			}
           	//if (null != mFPM) mFPM.fingerDetect();
        }
    }
    
    public void fingerDetectPause(){
    	if( null != mFingerService) {
        	try {
				mFingerService.cancleDetect();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	//if (null != mFPM) mFPM.fingerCancleDetect();
        	//else Log.d("JTAG", "fingerservice == null");
        } else {
        	Log.d("JTAG", "fingerservice == null");
        }
    }

	private boolean hasFingerData() {
        int count = 0;
        Fprint.doState(bState);
        for (int i = 0; i < bState.length; i++) {
            count += (bState[i] & 0xff);
        }
        return (count > 0) ? true : false;
    }    

    //add by Young end
    
}
