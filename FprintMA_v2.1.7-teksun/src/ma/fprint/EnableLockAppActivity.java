package ma.fprint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import ma.view.CommonAdapter;
import ma.view.ViewHolder;

import android.app.ActionBar;

public class EnableLockAppActivity extends Activity {

	private static final String TAG = "EnableLockAppActivity";
	private static CommonAdapter<AppInfo> mAdapter;
	private static ListView mAppsListView;
	private List<AppInfo> mAppsData = null;

	private static ProgressBar mProgressBar = null;
	private LoadAppsThread mLoadAppsThread = null;
	private Switch mAppSwictch = null;

	private static Context mContext = null;
	private boolean mBack = false;/*指纹应用加密界面按home键退出，再次点击指纹进入时无需输入密码:mandy.wu 20151009*/
	private boolean mFlag = false;/*[bug-2883]指纹应用加密全部开启后，反复进出应用加密会自动取消里面的部分已开启的应用:mandy.wu 20151017*/
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			if (msg.what == 0) {
				mProgressBar.setVisibility(View.GONE);
				mAppsListView.setAdapter(mAdapter);
				mAdapter.notifyDataSetChanged();
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_enablelock_apps);
		Util.enablePwd(this, false);/*指纹应用加密界面按home键退出，再次点击指纹进入时无需输入密码:mandy.wu 20151009*/
		initViews();
		initActivity();
	}
	
	private void initViews() {
	    mAppSwictch = (Switch) findViewById(R.id.id_manage_applock_off_on_sw);
		mAppSwictch.setOnCheckedChangeListener(appLockCheckedChangeListener);
	    mProgressBar = (ProgressBar) findViewById(R.id.id_enablelock_apps_pb);
	    mAppsListView = (ListView) findViewById(R.id.id_lv_main);
	}
	
	private OnCheckedChangeListener appLockCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Log.e(TAG, "发生变化 ： appLockCheckedChangeListener = " + isChecked);
			Util.setSettingFpAppLockOffOn(mContext, isChecked);
			mAdapter.notifyDataSetChanged();
		}
	};
	
	private void initActivity()  {
           ActionBar actionBar = getActionBar();
           actionBar.setTitle(R.string.lockui_title);
	   actionBar.setHomeButtonEnabled(false);
	   actionBar.setDisplayShowTitleEnabled(true); // 可以显示标题栏
           actionBar.setDisplayShowHomeEnabled(false);//actionBar左侧图标是否显示
	   actionBar.setDisplayHomeAsUpEnabled(true);
           actionBar.setDisplayUseLogoEnabled(false);

	    mAppsData = new ArrayList<AppInfo>();
		mAdapter = new CommonAdapter<AppInfo>(EnableLockAppActivity.this, mAppsData, R.layout.lockapp_item_list) {

			@Override
			public void convert(final ViewHolder holder, final AppInfo item) {
				holder.setText(R.id.id_item_appname_tv, item.appName);
				holder.setImageDrawable(R.id.id_item_appicon_iv, item.appIcon);

				final Switch cb = holder.getView(R.id.id_item_needlock_cb);
				cb.setChecked(item.isChecked());
				cb.setEnabled(Util.isSettingFpAppLockOn(mContext));
				
			  	/*[changed text color]: TLB liuyang 20150914 begin */ 
				if (Util.isSettingFpAppLockOn(mContext)) {
					holder.setTextColor(R.id.id_item_appname_tv,item.isChecked() ? 0xff000000 : 0xff898989);
					android.util.Log.v("liuyang", "on");
				} else {
					holder.setTextColor(R.id.id_item_appname_tv, 0xff898989);
					android.util.Log.v("liuyang", "off");
				}
				/*[changed text color]: TLB liuyang 20150914 end */ 
				
				cb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
					    Log.e("JTAG", item.packageName);
						item.setChecked(cb.isChecked());
			          String lastPkgName = "";//*Settings.System.getString(getContentResolver(), Settings.System.MICROARRAY_LAST_APP_LOCK_PACKAGE_NAME);
            if(lastPkgName ==  null){
                //*Settings.System.putString(getContentResolver(), Settings.System.MICROARRAY_LAST_APP_LOCK_PACKAGE_NAME, "");
                lastPkgName = new String("invalid");
            }
						if (lastPkgName.equals(item.packageName)) {
							if (!cb.isChecked()) {
								//*Settings.System.putString(getContentResolver(), Settings.System.MICROARRAY_LAST_APP_LOCK_PACKAGE_NAME, "");
								//Util.putLastAppLockString(getApplicationContext(), "");
							} else {
								//*Settings.System.putString(getContentResolver(), Settings.System.MICROARRAY_LAST_APP_LOCK_PACKAGE_NAME, item.packageName);
								//Util.putLastAppLockString(getApplicationContext(), lastPkgName);
							}
						}

						/*[changed text color]: TLB liuyang 20150914 begin */ 
						  holder.setTextColor(R.id.id_item_appname_tv, item.isChecked() ? 0xff000000 : 0xff898989);
						  /*[changed text color]: TLB liuyang 20150914 end */ 
					}
				});
				
				RelativeLayout container = (RelativeLayout) holder.getView(R.id.id_app_item_container);
				container.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
					    Log.e(TAG, "---3---");
						if (mAppSwictch.isChecked()) {
							cb.setChecked(!item.isChecked());
							item.setChecked(cb.isChecked());
							/*[changed text color]: TLB liuyang 20150914 begin */ 
							holder.setTextColor(R.id.id_item_appname_tv, cb.isChecked() ? 0xff000000 : 0xff898989);
							/*[changed text color]: TLB liuyang 20150914 end */ 
						}
					}
				});
			}
		};
		mLoadAppsThread = new LoadAppsThread();
		mLoadAppsThread.start();
		
		mAppSwictch.setChecked(Util.isSettingFpAppLockOn(this));
	}

	private class LoadAppsThread extends Thread {
		@Override
		public void run() {
			queryAppInfo();
		}
	}

	// 获得所有启动Activity的信息，类似于Launch界面
	public void queryAppInfo() {
		String[] exclueApps = getResources().getStringArray(R.array.exclude_app);// 需要过滤掉的应用

		List<String> existLockAppsList = new ArrayList<String>();
		List<PackageInfo> allPackages = getPackageManager().getInstalledPackages(0);

		StringBuilder stringBuilder = new StringBuilder();
	/*[bug-2883]指纹应用加密全部开启后，反复进出应用加密会自动取消里面的部分已开启的应用:mandy.wu 20151017 begin*/
		synchronized (getApplicationContext()) {
			String appString = "";//*Settings.System.getString(getContentResolver(), Settings.System.MICROARRAY_FINGERPRINT_NEEDLOCKAPP_PACKAGE_NAME);

			if (appString != null) {
				String[] appsStrings = appString.split("\\|");
				existLockAppsList = Arrays.asList(appsStrings);
			}

			PackageManager pm = this.getPackageManager(); // 获得PackageManager对象
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			// 通过查询，获得所有ResolveInfo对象.
			List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
			if (mAppsData != null) {
				mAppsData.clear();
				for (ResolveInfo reInfo : resolveInfos) {
					String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
					String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
					String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
					Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标

					android.util.Log.i(TAG, "pkgName = " + pkgName);

					// 创建一个AppInfo对象，并赋值
					AppInfo appInfo = new AppInfo();
					appInfo.packageName = pkgName;
					appInfo.appName = appLabel;
					appInfo.appIcon = (icon);

					if (existLockAppsList.contains(pkgName)) {
						appInfo.setChecked(true);
					}

					/*
					 * com.xiami.walkman"音乐要不要过滤呢？ 屏蔽一键清理、语音助手
					 */
					if (!pkgName.equals("com.goodix.fpsetting")
							&& !pkgName.equals("com.bird.flashlight")
							&& !pkgName.equals("com.yunos.camera")
							&& !pkgName.equals("com.android.deskclock")
							&& !pkgName.equals("com.bird.assistant")
							&& !pkgName.equals("com.bird.cleantask")
							/* 去掉没有必要加锁的应用:mandy.wu 20150914 begin */
							&& !pkgName.equals("com.aliyun.SecurityCenter")
							&& !pkgName.equals("com.aliyun.soundrecorder")
							&& !pkgName.equals("com.android.calculator2")
							&& !pkgName.equals("com.bird.manual")
							&& !pkgName.equals("ma.fprint")
							&& !pkgName.equals("com.bird.smart")) {
						/* 去掉没有必要加锁的应用:mandy.wu 20150914 end */
						int mSamePkgName = 0;
						for (int i = 0; i < mAppsData.size(); i++) {
							if (mAppsData.get(i).packageName.equals(pkgName)) {// 过滤相同包名
								mSamePkgName += 1;
								break;
							}
						}
						if (mSamePkgName == 0) {
							mAppsData.add(appInfo); // 添加至列表中
						}
					}
				}
			}
		}

		mFlag = true;
	/*[bug-2883]指纹应用加密全部开启后，反复进出应用加密会自动取消里面的部分已开启的应用:mandy.wu 20151017 end*/
		mHandler.sendEmptyMessage(0);
	}
	/*指纹应用加密界面按home键退出，再次点击指纹进入时无需输入密码:mandy.wu 20151009 begin*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	mBack = true;
        	Util.enablePwd(this, false);
		}else{
			mBack = false;
		}
		return super.onKeyDown(keyCode, event);
	}
	/*指纹应用加密界面按home键退出，再次点击指纹进入时无需输入密码:mandy.wu 20151009 end*/
	@Override
	protected void onStop() {
		super.onStop();
		new SaveLockAppsThread().start();
		//finish();/*反复进入退出，开关有点错乱：mandy.wu 20151009*/
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/*指纹应用加密界面按home键退出，再次点击指纹进入时无需输入密码:mandy.wu 20151009 begin*/
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		boolean mNeedPasswd = Util.isNeedPwd(this);
		if (mNeedPasswd) {
		    gotoPasswordAct();
		}
	}
	private void gotoPasswordAct() {
	    Intent intent = new Intent(this, PasswordActivity.class);
	    startActivity(intent);
	    finish();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(!mBack){
			Util.enablePwd(this, true);
		}
	}
	/*指纹应用加密界面按home键退出，再次点击指纹进入时无需输入密码:mandy.wu 20151009 end*/
	private class SaveLockAppsThread extends Thread {
		@Override
		public void run() {
			/*[bug-2883]指纹应用加密全部开启后，反复进出应用加密会自动取消里面的部分已开启的应用:mandy.wu 20151017 begin*/
			if (mFlag) {
				synchronized (getApplicationContext()) {
					if (mAppsData != null) {
						//*Settings.System.putString(mContext.getContentResolver(), Settings.System.MICROARRAY_FINGERPRINT_NEEDLOCKAPP_PACKAGE_NAME, "");
						//Util.putExistAppLockListString(getApplicationContext(), "");
						StringBuilder stringBuilder = new StringBuilder();
						for (int i = 0; i < mAppsData.size(); i++) {
							if (mAppsData.get(i).isChecked) {
								stringBuilder.append(mAppsData.get(i).packageName + "|");
								Log.v(TAG, "tmpInfo : " + stringBuilder.toString());
							}
						}
						//*Settings.System.putString(mContext.getContentResolver(), Settings.System.MICROARRAY_FINGERPRINT_NEEDLOCKAPP_PACKAGE_NAME, stringBuilder.toString());
						//Util.putExistAppLockListString(getApplicationContext(), stringBuilder.toString());
					}
				}
			}
			/*[bug-2883]指纹应用加密全部开启后，反复进出应用加密会自动取消里面的部分已开启的应用:mandy.wu 20151017 end*/
		}
	}

	class AppInfo {
		public String appName = "";
		public String packageName = "";
		public String versionName = "";
		public int versionCode = 0;
		public Drawable appIcon = null;
		private boolean isChecked = false;

		public boolean isChecked() {
			return isChecked;
		}

		public void setChecked(boolean check) {
			isChecked = check;
		}

		public void print() {
			Log.v(TAG, "Name:" + appName + " |  Package:" + packageName);
		}

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
}

