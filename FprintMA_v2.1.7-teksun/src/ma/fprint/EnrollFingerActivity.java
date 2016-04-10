
package ma.fprint;

//import com.android.settings.R;
import ma.fprint.R;
import ma.library.EnrollAction;
import ma.library.FingerEnroll;
import ma.library.PhoneStatusBroadcastReceiver;
import ma.release.Fprint;
import ma.service.IFingerprintService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class EnrollFingerActivity extends Activity implements OnClickListener {
    private boolean bFirst = true;
    private Button btnCancel;
    private TextView textPlace;
    private TextView textRepeat;
    private TextView textContinue;
    private ImageView imageView;
    private LinearLayout layoutBack;
    private RelativeLayout layoutImage;

    private boolean enrollSuccess = false;
    
    private final int ic_finger[] = {
            R.drawable.ic_ma_finger0,
            R.drawable.ic_ma_finger1, R.drawable.ic_ma_finger2,
            R.drawable.ic_ma_finger3, R.drawable.ic_ma_finger4,
            R.drawable.ic_ma_finger5, R.drawable.ic_ma_finger6,
            R.drawable.ic_ma_finger7, R.drawable.ic_ma_finger8,
            R.drawable.ic_ma_finger9, R.drawable.ic_ma_finger10,
            R.drawable.ic_ma_finger11, R.drawable.ic_ma_finger12,
            R.drawable.ic_ma_finger13, R.drawable.ic_ma_finger14
    };
    private final int MSG_NEXT = 0x101;
    private final int MSG_SUCC = 0x102;
    private final int MSG_FAIL = 0x103;
    private final int MSG_DUMP = 0x104; // 清除
    private final int MSG_MOVE = 0x105;
    private final int MSG_GOON = 0x106;

    private int curSel = 0;
    
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        System.out.println("hello enroll create");
        curSel = getIntent().getIntExtra("sel", 0);
        setContentView(R.layout.ma_enroll_finger);
        btnCancel = (Button) findViewById(R.id.ma_enroll_cancel);
        imageView = (ImageView) findViewById(R.id.ma_finger_view);
        textPlace = (TextView) findViewById(R.id.ma_enroll_place);
        textRepeat = (TextView) findViewById(R.id.ma_enroll_repeat);
        textContinue = (TextView) findViewById(R.id.ma_enroll_continue);
        layoutBack = (LinearLayout) findViewById(R.id.ma_enroll_back);
        layoutImage = (RelativeLayout) findViewById(R.id.ma_enroll_image);
        btnCancel.setOnClickListener(this);
        layoutBack.setOnClickListener(this);
        textContinue.setOnClickListener(this);

        imageView.setMaxHeight(200);
        imageView.setMaxWidth(132);

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

    }

    private void exit() {
        Fprint.start();
        if (!enrollSuccess) Fprint.clear(curSel + 1);
        unregisterReceiver(mReceiver);
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
        int idx;

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_NEXT:
                    textPlace.setText(getResources().getString(
                            R.string.ma_enroll_lift));
                    idx = (msg.arg1 / 10) % 15;
                    if (!bFirst) {
                        imageView.setImageResource(ic_finger[idx]);
                    } else {
                        bFirst = false;
                        layoutImage.removeView(imageView);
                        float density = getResources().getDisplayMetrics().density;
                        LayoutParams lpar = new LayoutParams((int) (132 * density),
                                (int) (200 * density));
                        lpar.addRule(RelativeLayout.CENTER_IN_PARENT,
                                RelativeLayout.TRUE);
                        lpar.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                                RelativeLayout.TRUE);
                        imageView.setLayoutParams(lpar);
                        imageView.setImageResource(ic_finger[idx]);
                        layoutImage.addView(imageView);
                    }
                    break;
                case MSG_SUCC:
                    textPlace.setText(getResources().getString(
                            R.string.ma_enroll_succ));
                    imageView.setImageResource(ic_finger[ic_finger.length - 1]);
                    enrollSuccess = true;
                    exit();
                    break;
                case MSG_GOON:
                    textPlace.setText(getResources().getString(
                            R.string.ma_enroll_adjust));
                    textRepeat.setText(getResources().getString(
                            R.string.ma_enroll_go_obtain));
                    textContinue.setVisibility(View.VISIBLE);
                    break;
                case MSG_MOVE:
                    textPlace.setText(getResources().getString(
                            R.string.ma_enroll_alter));
                    Util.vibrate(getApplicationContext(), 200);
                    break;
                case MSG_DUMP:
                    textPlace.setText(getResources().getString(
                            R.string.ma_enroll_place));
                    break;

                case MSG_FAIL:
                    textPlace.setText(getResources().getString(
                            R.string.ma_dlg_capture_fail));
                    break;

                case PhoneStatusBroadcastReceiver.screen_off:
                case PhoneStatusBroadcastReceiver.key_home:
                    exit();
                    break;

            }
        }
    };

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.ma_enroll_cancel || id == R.id.ma_enroll_back) {
            exit();
        } else if (id == R.id.ma_enroll_continue) {
            textPlace.setText(getResources()
                    .getString(R.string.ma_enroll_place));
            textRepeat.setText(getResources().getString(
                    R.string.ma_enroll_repeat));
            textContinue.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_HOME:
                exit();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}
