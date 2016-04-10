package ma.fprint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.RelativeLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.widget.Button;

import ma.view.PackagesConstant;
import ma.view.ViewHolder;
import ma.fprint.Util;
import ma.view.CommonAdapter;
import ma.fprint.R;

public class SelectAppActivity extends Activity {

	private static final String TAG = "SelectAppActivity";
	private static CommonAdapter<AppInfo> mAdapter;
	private static ListView mAppsListView;
	private List<AppInfo> mAppsData = null;
	private PackageManager mPackageManager;

	private static ProgressBar mProgressBar = null;
	private LoadAppsThread mLoadAppsThread = null;
	
    private static Context mContext = null;
    
    public Button mBtnConfirm;
    public String mPackageName="";
    public String oldmPackageName="";
	private int curSel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.selectapppackage);
		
		curSel = getIntent().getIntExtra("finger_ID", 9);
		
        mAppsData = new ArrayList<AppInfo>();
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.lunchapp_title);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true); // 可以显示标题栏
        actionBar.setDisplayShowHomeEnabled(false);//actionBar左侧图标是否显示
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        
		String packagenameString = getPackageName();
		
		oldmPackageName = getIntent().getStringExtra("launchPkgName");
		
		mBtnConfirm = (Button)findViewById(R.id.id_manage_applunch_confirm);
		mBtnConfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
				Intent intent = getIntent();
				
				boolean bFindSet =false;
							for(int i =0; i < mAdapter.getCount(); i ++)
							{
								AppInfo info = mAdapter.getItem(i);
								if( info.isChecked())
								{
									mPackageName = info.packageName;
									bFindSet = true;
									break;
								}
							}
							

				if( bFindSet ==  false )
				{
					mPackageName = "";
					Util.updateFingerLaunchAppFlag(getApplicationContext(), 0, curSel);	
				    Util.updateFingerLaunchAppName(getApplicationContext(),mPackageName,curSel);
				}
				if(!mPackageName.equals("")){
					//Log.d("JTAG", "ID:" + curSel + "  SelectApp:" + mPackageName);
				    Util.updateFingerLaunchAppFlag(getApplicationContext(), 1, curSel);	
				    Util.updateFingerLaunchAppName(getApplicationContext(),mPackageName,curSel);
				}
				//intent.putExtra("packagename", mPackageName); 
				//setResult(RESULT_OK, intent);
				Util.enablePwd(getApplicationContext(), false);
	           
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			 finish();
			}
		});
		
		mProgressBar = (ProgressBar) findViewById(R.id.id_enablelock_apps_pb);
		mPackageManager = getPackageManager();

		mAppsListView = (ListView) findViewById(R.id.id_lv_main);

		mAdapter = new CommonAdapter<AppInfo>(SelectAppActivity.this, mAppsData, R.layout.selectapp_item_list) {

			@Override
			public void convert(ViewHolder holder, final AppInfo item) {
				holder.setText(R.id.id_item_appname_tv, item.appName);
				holder.setImageDrawable(R.id.id_item_appicon_iv, item.appIcon);
				
				final Switch cb = holder.getView(R.id.id_item_needlock_cb);
				cb.setChecked(item.isChecked());
				cb.setEnabled(true);
				cb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						item.setChecked(cb.isChecked());
						
						if( cb.isChecked() == true)
						{
							for(int i =0; i < mAdapter.getCount(); i ++)
							{
								AppInfo info = mAdapter.getItem(i);
								if( info.packageName.compareTo(item.packageName) != 0)
								{
									info.setChecked(false);
								}
							}
							mAdapter.notifyDataSetChanged();
							
							mPackageName = item.packageName;
							
							Log.i(TAG, ""+item.packageName);
							Log.i(TAG, ""+item.appName);
						}
					}
				});
				
				RelativeLayout container = (RelativeLayout) holder.getView(R.id.id_app_item_container);
				container.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						cb.setChecked(!item.isChecked());
						item.setChecked(cb.isChecked());
						
						if( cb.isChecked() == true)
						{
							for(int i =0; i < mAdapter.getCount(); i ++)
							{
								AppInfo info = mAdapter.getItem(i);
								if( info.packageName.compareTo(item.packageName) != 0)
								{
									info.setChecked(false);
								}
							}
							mAdapter.notifyDataSetChanged();
							
							mPackageName = item.packageName;
							
			                Log.i(TAG, ""+item.packageName);
							Log.i(TAG, ""+item.appName);
						}
					}
				});
			}
		};

		mLoadAppsThread = new LoadAppsThread();
		mLoadAppsThread.start();
	}

	private class LoadAppsThread extends Thread {
		@Override
		public void run() {
			queryAppInfo();
		}
	}

    private boolean mFlag = false;
	private Handler mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {

		    if (msg.what == 0) {
				mProgressBar.setVisibility(View.GONE);
				mAppsListView.setAdapter(mAdapter);
				
				
				for(int i =0; i < mAdapter.getCount(); i ++)
				{
					AppInfo info = mAdapter.getItem(i);
					if( info.packageName.compareTo(oldmPackageName) != 0)
					{
						info.setChecked(false);
					}
					else
					{
						info.setChecked(true);
					}
				}
				mAdapter.notifyDataSetChanged();

		    }

	    }
	};

	// 获得所有启动Activity的信息，类似于Launch界面
    public void queryAppInfo() {
    	String[] exclueApps = getResources().getStringArray(R.array.exclude_app);//需要过滤掉的应用
    	
		List<String> existLockAppsList = new ArrayList<String>();
		List<PackageInfo> allPackages = getPackageManager().getInstalledPackages(0);

		StringBuilder stringBuilder = new StringBuilder();
		String appString = "";//*Settings.System.getString(getContentResolver(), Settings.System.MICROARRAY_FINGERPRINT_NEEDLOCKAPP_PACKAGE_NAME);
		
		if (appString != null) {
			String[] appsStrings = appString.split("\\|");
			existLockAppsList = Arrays.asList(appsStrings);
		}
		
        PackageManager pm = this.getPackageManager(); //获得PackageManager对象
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
       // Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
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
    			
    			for(int i = 0; i < exclueApps.length; i ++)
    			{
    				if(pkgName.compareTo(exclueApps[i]) == 0)
    				{
    					continue;
    				}
    			}
    			
    			/*
    			 *com.xiami.walkman"音乐要不要过滤呢？
    			 * 屏蔽一键清理、语音助手
    			 */
    			if (!pkgName.equals("ma.fprint")) {
    				int mSamePkgName = 0;
    				for (int i = 0; i < mAppsData.size(); i++) {
						if (mAppsData.get(i).packageName.equals(pkgName)) {//过滤相同包名
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
        
        mFlag = true;
        mHandler.sendEmptyMessage(0);
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	setResult(111);
        	Util.enablePwd(getApplicationContext(), false);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case android.R.id.home:
			setResult(111);
			Util.enablePwd(getApplicationContext(),false);
			finish();
			break;

		default:
			break;
		}

		return true;
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
	
}
