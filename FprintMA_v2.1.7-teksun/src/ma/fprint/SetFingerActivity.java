
package ma.fprint;

//import com.android.settings.R;
import ma.fprint.R;
import ma.library.*;
import ma.release.Fprint;
import ma.service.*;

import java.util.ArrayList;
import java.util.HashMap;

import ma.fprint.FpDetail;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.app.AlertDialog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;

public class SetFingerActivity extends Activity implements OnClickListener {
    private int curSel;
    private int mRet = 0;
    private byte[] bState = new byte[5];
    private boolean bUnlock = false;
    private ListView listView = null;
    private RelativeLayout layoutAdd;
    private RelativeLayout layoutApplock;
    private RelativeLayout layoutUnlock;
    private RelativeLayout layoutCalibrate;
    private ArrayList<HashMap<String, Object>> mList;

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
        System.out.println("hello create setFingerActivity");
        setContentView(R.layout.ma_set_finger);
        layoutAdd = (RelativeLayout) findViewById(R.id.ma_set_finger_add);
        layoutApplock = (RelativeLayout) findViewById(R.id.ma_set_app_lock);
        layoutUnlock = (RelativeLayout) findViewById(R.id.ma_set_finger_unlock);
        layoutCalibrate = (RelativeLayout) findViewById(R.id.ma_set_finger_calibrate);
        layoutAdd.setOnClickListener(this);
        layoutApplock.setOnClickListener(this);
        layoutUnlock.setOnClickListener(this);
        layoutCalibrate.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bUnlock = (Util.readXML(getApplicationContext(), "lock", 0) == 1) ? true
                : false;
        setButtonStatus(bUnlock);
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
            Intent calibrateIntent = new Intent(SetFingerActivity.this,
                    FactoryActivity.class);
            startActivity(calibrateIntent);
            Util.writeXML(getApplicationContext(), "calibrated", 1);
        } else if (mRet == -3) {
            Prompt.w(this,
                    getResources().getString(R.string.ma_dlg_not_capture));
        }

        if (mRet >= 0)
            initList();
            
        //Intent it = new Intent(SetFingerActivity.this, MatchService.class);
        //startService(it);
        Intent intent = new Intent("ma.service.aidl.IFingerprintService");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        
        fingerDetectResume();
    }

    @Override
   	protected void onDestroy() {
   		// TODO Auto-generated method stub
   		super.onDestroy(); 
       	unbindService(conn);
   		
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
    
    @Override
    public void onClick(View arg0) {

        int i, id = arg0.getId();
        switch (id) {
            case R.id.ma_set_finger_unlock:
                if ((Util.readXML(getApplicationContext(), "calibrated", 0) == 1)) {
                    if (!hasFingerData()) {
                        Prompt.w(this,
                                getResources().getString(R.string.ma_dlg_not_finger_template));
                        return;
                    }
                    Intent it = new Intent(SetFingerActivity.this, MatchService.class);
                    it.putExtra("isScreenOn", "true");
                    bUnlock = (Util.readXML(getApplicationContext(), "lock", 0) == 1) ? true
                            : false;
                    bUnlock = !bUnlock;
                    if (bUnlock) {
                        Util.writeXML(getApplicationContext(), "lock", 1);
                        startService(it);
                    } else {
                        Util.writeXML(getApplicationContext(), "lock", 0);
                        //stopService(it);
                    }
                    setButtonStatus(bUnlock);
                } else {
                    Prompt.w(this, getResources()
                            .getString(R.string.ma_set_finger_please_calibrate));
                }

                break;

            case R.id.ma_set_finger_calibrate:
            	fingerDetectPause();
            	
                Util.writeXML(getApplicationContext(), "lock", 0);
                Intent it = new Intent(SetFingerActivity.this, MatchService.class);
                stopService(it);
                Intent calibrateIntent = new Intent(SetFingerActivity.this,
                        FactoryActivity.class);
                startActivity(calibrateIntent);
                break;
            case R.id.ma_set_finger_add:
                for (i = 0; i < bState.length; i++) {
                    if ((bState[i] & 0xff) == 0)
                        break;
                }
                if (i < bState.length) {
                	fingerDetectPause();
                	
                    Intent enrollIntent = new Intent(SetFingerActivity.this,
                            EnrollFingerActivity.class);
                    enrollIntent.putExtra("sel", i);
                    startActivity(enrollIntent);
                } else {
                    Prompt.w(this,
                            getResources().getString(R.string.ma_dlg_finger_full));
                }
                break;
            case R.id.ma_set_app_lock:
            	Intent intent = new Intent();
        		intent.setClass(this, EnableLockAppActivity.class);
        		startActivity(intent);
                break;
            case R.id.ma_set_finger_back:
                finish();
                break;
            default:
                break;
        }
        return;
    }

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_HOME
                || keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

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
        listView = (ListView) findViewById(R.id.ma_set_finger_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                RelativeLayout layout = (RelativeLayout) listView
                        .getChildAt(arg2);
                TextView txt = (TextView) layout.getChildAt(2);
                String str = txt.getText().toString();
                char num = str.charAt(str.length() - 1);
                int fid = num - '0';
                txt = (TextView) layout.getChildAt(1);
                str = txt.getText().toString();
                if ((bState[fid - 1] & 0xff) > 0) {
                    curSel = arg2;
                    //add for bird begin
                    Intent intent = new Intent(SetFingerActivity.this,FpDetail.class);
                    intent.putExtra("finger_ID", fid);
            		intent.putExtra("finger_name",str);
            		startActivityForResult(intent, 1);
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
            int rid = (bState[i] & 0xff) > 0 ? R.drawable.ic_ma_checked
                    : R.drawable.ic_ma_unchecked;
            item.put("btn", rid);
            String name = Util.getFingerName(getApplicationContext(), i + 1);
            item.put("name", name);
            String info = getResources().getString(R.string.ma_enroll_fid)
                    + Integer.toString(i + 1);
            item.put("info", info);
            list.add(item);
        }
    }

    private void promptC(String content) {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        Window wnd = dlg.getWindow();
        wnd.setContentView(R.layout.ma_dialog);
        ImageView iv = (ImageView) wnd.findViewById(R.id.ma_dlg_ticon);
        iv.setBackgroundResource(R.drawable.ic_ma_warn);
        TextView tt = (TextView) wnd.findViewById(R.id.ma_dlg_ttext);
        tt.setText(getResources().getString(R.string.ma_dlg_warn));
        TextView tv = (TextView) wnd.findViewById(R.id.ma_dlg_content);
        tv.setText(content);
        Button yes = (Button) wnd.findViewById(R.id.ma_dlg_confirm);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                RelativeLayout layout = (RelativeLayout) listView
                        .getChildAt(curSel);
                TextView txt = (TextView) layout.getChildAt(1);
                String str = txt.getText().toString();
                char num = str.charAt(str.length() - 1);
                int fid = num - '0';
                Fprint.clear(fid);
                Fprint.doState(bState);
                mList.remove(curSel);
                SimpleAdapter adapter = (SimpleAdapter) listView.getAdapter();
                adapter.notifyDataSetChanged();
                
                if(!hasFingerData()) {
                    Util.writeXML(getApplicationContext(), "lock", 0);
                    setButtonStatus(false);
                    FingerThread.getFingerThreadInstance().FingerThreadWait();
                    Util.sleep(20);
                    //Intent it = new Intent(SetFingerActivity.this, MatchService.class);
                    //stopService(it);
                }
                dlg.dismiss();
            }
        });
        Button no = (Button) wnd.findViewById(R.id.ma_dlg_cancel);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dlg.dismiss();
            }
        });
        no.setVisibility(View.VISIBLE);
    }
    
    private boolean hasFingerData() {
        int count = 0;
        Fprint.doState(bState);
        for (int i = 0; i < bState.length; i++) {
            count += (bState[i] & 0xff);
        }
        return (count > 0) ? true : false;
    }

    private void setButtonStatus(boolean enable) {
        ImageView iv = (ImageView) layoutUnlock.getChildAt(1);
        if (enable) {
            iv.setImageResource(R.drawable.ic_ma_switch_on);
        } else {
            iv.setImageResource(R.drawable.ic_ma_switch_off);
        }
    }
}
