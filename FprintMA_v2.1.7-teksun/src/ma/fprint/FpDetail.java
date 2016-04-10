package ma.fprint;

import ma.fprint.R;
import ma.fprint.Util;
import ma.release.Fprint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.Toast;


public class FpDetail extends Activity implements OnClickListener{

    private String TAG = "FpDetail";
	
	private TextView mTvFpName = null;
	private EditText mEdtFpNewName = null;
	private Button mBtnRename = null;
	private Button mBtnConfirmRename = null;
	private Button mBtnDelete = null;
        private Button mBtnAddlaunch = null;
	private int curSel;
	
	//private FpHandleServiceConnection mHandleServiceConn;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fp_detail);
		initViews();
		initListener();
		initActivity();
	}
	
	private void initViews() {
	    mEdtFpNewName = (EditText)findViewById(R.id.id_edt_fp_rename);
		mBtnConfirmRename = (Button)findViewById(R.id.id_btn_confirm_rename);
		mTvFpName = (TextView)findViewById(R.id.id_tv_fp_rename);
		mBtnRename = (Button)findViewById(R.id.id_btn_rename);
		mBtnDelete = (Button)findViewById(R.id.id_btn_delete);
                mBtnAddlaunch = (Button)findViewById(R.id.id_btn_addlaunch);
	}
	
	private void initListener() {
	    mBtnRename.setOnClickListener(this);
		mBtnDelete.setOnClickListener(this);
		mBtnConfirmRename.setOnClickListener(this);
                mBtnAddlaunch.setOnClickListener(this);
	}
	
	private void initActivity() {
                ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.fingerprint_mag);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true); // 可以显示标题栏
                actionBar.setDisplayShowHomeEnabled(false);//actionBar左侧图标是否显示
		actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);            

		curSel = getIntent().getIntExtra("finger_ID", 9);
		String name = getIntent().getStringExtra("finger_name");
		mTvFpName.setText(name);
	    
	   
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
         case R.id.id_btn_addlaunch:
            boolean hasLauchApp = Util.getFingerLaunchAppFlag(getApplicationContext(),curSel) == 1 ? true : false;
			String lauchPackage = new String("");
            lauchPackage = Util.getFingerLaunchAppName(getApplicationContext(),curSel);
            Log.d("JTAG", "Fpdetail---Pkg:" + lauchPackage + "  curSel:" + curSel + "isLaunch:" + lauchPackage);
            Intent intent = new Intent(FpDetail.this, SelectAppActivity.class);
			if(hasLauchApp && !lauchPackage.equals("") ) {
			    intent.putExtra("launchPkgName", lauchPackage); 
			} else {
			    intent.putExtra("launchPkgName", "");
            }
            intent.putExtra("finger_ID", curSel); 
			startActivity(intent);
            break;
		case R.id.id_btn_rename:
			mBtnConfirmRename.setVisibility(View.VISIBLE);
			mEdtFpNewName.setVisibility(View.VISIBLE);
			mEdtFpNewName.setText(mTvFpName.getText().toString());
			mTvFpName.setVisibility(View.GONE);
			mBtnRename.setVisibility(View.GONE);
			break;
			
		case R.id.id_btn_delete:
			//createDeleteDialog();
			Util.updateFingerName(getApplicationContext(), getResources().getString(R.string.ma_enroll_fid) + curSel, curSel);
			Util.updateFingerLaunchAppFlag(getApplicationContext(), 0, curSel);	
			Util.updateFingerLaunchAppName(getApplicationContext(),"",curSel);
			
			Fprint.clear(curSel);
            Fprint.update(curSel);
			finish();
			break;
			
		case R.id.id_btn_confirm_rename:
			/*判断输入的重命名是否为空*/
			String newName = mEdtFpNewName.getText().toString().trim();
			if (!TextUtils.isEmpty(newName)) {
                Log.i(TAG, "name = " + mEdtFpNewName.getText().toString());
//                mFingerPrintItem.setName(mEdtFpNewName.getText().toString());
//                if (mFingerPrintHandleService != null) {
//                    mFingerPrintHandleService.update(mFingerPrintItem);
//                }
//                Preferences.enablePwd(FpDetail.this, false);
                if(curSel != 9) {
					Util.updateFingerName(getApplicationContext(), newName, curSel);
				}
                setResult(RESULT_OK);
			    finish();
			} else {
			    showToast();
			}
			
			break;
		default:
			break;
		}
	}
	
	Toast mToast = null;
	private void showToast() {
	    if (mToast == null) {
	        mToast = Toast.makeText(FpDetail.this, getString(R.string.new_name_warning), Toast.LENGTH_SHORT);
        }
        mToast.show();
	}
	
	public void createDeleteDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.fp_detail_delete)
		    .setPositiveButton(R.string.confirm, confirmListener)
		    .setNegativeButton(R.string.cancel, null)
		    .create().show();
	}
	
	private DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
//			Log.e(TAG, "Index : "+mFingerPrintItem.getName());
//			if (mFingerPrintHandleService != null) {
//			    mFingerPrintHandleService.delete(mFingerPrintItem.getKey());
//			    try {
//				    int index = Integer.parseInt(mFingerPrintItem.getUri());
//				    FpApplication.getInstance().getFpServiceManager().delete(index);
//			    } catch (Exception e) {
//				    e.printStackTrace();
//			    }
//			    Preferences.enablePwd(FpDetail.this, false);
//			    setResult(RESULT_OK);
//			    finish();
//			}
		}
	};
	
	protected void onStop() {
	    super.onStop();
	    finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	    //getApplicationContext().unbindService(mHandleServiceConn);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (mBtnConfirmRename.getVisibility() == View.GONE) {
        		//setResult(RESULT_OK);
        		//Preferences.enablePwd(this, false);
        		return super.onKeyDown(keyCode, event);
			} else {
				mTvFpName.setVisibility(View.VISIBLE);
				mBtnRename.setVisibility(View.VISIBLE);
				
				mBtnConfirmRename.setVisibility(View.GONE);
				mEdtFpNewName.setVisibility(View.GONE);
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {

		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}

		return true;
	}

       /* @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.d("JTAG", "onActivityResult");
		try{
			if(data.getExtras().containsKey("packagename")){
				if(resultCode==RESULT_OK)
				{
					String PackageName = data.getStringExtra("packagename");
                                        Log.d("JTAG","i:"+ PackageName);
				}
			}
		}catch(Exception e)
		{
		}
		super.onActivityResult(requestCode, resultCode, data);
	}*/
}
