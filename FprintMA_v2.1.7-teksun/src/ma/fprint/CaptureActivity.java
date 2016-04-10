
package ma.fprint;

import ma.library.CaptureAction;
import ma.library.FingerAction;
import ma.library.FingerCapture;
import ma.library.FingerThread;
import ma.release.Fprint;
import ma.library.PhoneStatusBroadcastReceiver;
import ma.library.Util;
import ma.release.Jnifp;
import ma.service.IFingerprintService;
//import com.android.settings.R;
import ma.fprint.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CaptureActivity extends Activity {

    private FingerAction mFingerActioninCapture;
    private PhoneStatusBroadcastReceiver mReceiver;
    FingerCapture mFC = new FingerCapture();

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
    
    private Handler captureHandler = new Handler() {  
		   
        @Override  
        public void handleMessage(Message msg) { 
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
             super.handleMessage(msg);  
       }  
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(new CaptureView(this));
        init();
        
        Intent intent = new Intent("ma.service.aidl.IFingerprintService");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        
        captureHandler.sendEmptyMessageDelayed(0, 500);
    }

    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
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

	private void init() {
        Fprint.start();
        mReceiver = new PhoneStatusBroadcastReceiver(mHandler);
        registerReceiver(mReceiver, PhoneStatusBroadcastReceiver.getFilter());
    }

    private void exit() {
        unregisterReceiver(mReceiver);
        FingerThread.getFingerThreadInstance().FingerThreadWait();
        Util.sleep(20);
        FingerThread.getFingerThreadInstance().removeAction(mFingerActioninCapture);
        finish();
    }

	class CaptureView extends SurfaceView implements SurfaceHolder.Callback {  	 
		private SurfaceHolder surHolder;
	//	private MyThread mThread = new MyThread();
		private Paint mScorePaint = new Paint(); //分数的画图句柄
		private Paint mTipsPaint = new Paint(); //提示的画图句柄n 
		private Paint mTitlePaint = new Paint();  //标题的画图句柄n 
		private Paint TitleRectPaint = new Paint();  //标题区域的画图句柄
	    private Paint mPaint;
		private int W = 120; // 位图宽
		private int H = 120; // 位图高
		
		private int tipsHeight = 30; // 文字高
		private int tipsWidth = tipsHeight*3; // 文字宽
		
		private int titleHeight = 35; // 标题文字高
		private int titleWidth = titleHeight*2; // 标题文字宽

private Rect titleRect; // 标题区域
        private byte srcBytes[] = null;
        private Canvas mCanvas = null;
        private float mScale = 1.8f;
        private int mWidth;
        private int startY;
        private int bmpW;
        private int bmpH;
		
		private Point userTipPoint = new Point();
		private Point scorePoint = new Point();
		private Point imagePoint = new Point();
		private Point titlePoint = new Point();

		private final int MIN_GRADE = 70;

		public CaptureView(Context context) {    
			super(context);
			
			Log.d("JTAG", "CaptureView CaptureView()");
			this.setKeepScreenOn(true);
			this.setFocusable(true);
			
			surHolder = this.getHolder();
			surHolder.addCallback(this);
			
			mScorePaint.setAntiAlias(true);
			mScorePaint.setTextSize(tipsHeight);
			mScorePaint.setColor(Color.RED);

			mTipsPaint.setAntiAlias(true);
			mTipsPaint.setTextSize(tipsHeight);
			mTipsPaint.setColor(Color.BLUE);
			
			mTitlePaint.setAntiAlias(true);
			mTitlePaint.setTextSize(titleHeight);
			mTitlePaint.setColor(Color.BLACK);

			TitleRectPaint.setColor(Color.GRAY);
			
			mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(20);
            mPaint.setColor(Color.RED);
            srcBytes = new byte[W * H + 1078];
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mWidth = this.getWidth();

imagePoint.set((getWidth() - ((int)(W*mScale)))/2, (getHeight() - ((int)(H*mScale)))/2);
			userTipPoint.set(32, getHeight()-32);			
			scorePoint.set((getWidth() - tipsWidth)/2, (getHeight() + ((int)(H*mScale)))/2+tipsHeight);			
			titlePoint.set((getWidth() - titleWidth)/2, titleHeight+12);

	titleRect = new Rect(0,0,getWidth(), titleHeight+32);

            mScale = 1.5f;
            bmpW = (int) (W * mScale);
            bmpH = (int) (H * mScale);
            startY = (getHeight() - bmpH) / 2;

            drawBackground();
            
            mFingerActioninCapture = mFingerAction;
            FingerThread.getFingerThreadInstance().FingerThreadWait();
            Util.sleep(20);
            FingerThread.getFingerThreadInstance().setAction(mFingerAction);
            FingerThread.getFingerThreadInstance().FingerThreadResume();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        private FingerAction mFingerAction = new FingerAction() {

            @Override
            public void FingerDown() {
            	
            	
                if (srcBytes == null)
                    return;
                int score = Jnifp.score(srcBytes, srcBytes.length);
                int grade = (int) (score / 12000);
                mCanvas = surHolder.lockCanvas();
                if (mCanvas == null)
                    return;
                mCanvas.drawColor(Color.WHITE);
                int x = (mWidth - bmpW) / 2;
                drawBmp(srcBytes, x, startY);
                String str = "score:" + Integer.toString(grade);
                mCanvas.drawText(str, 32, 96, mPaint);
                if (grade > MIN_GRADE) {
                        str = new String("Success");
                        mCanvas.drawText(str, 300, 996, mPaint);
                        surHolder.unlockCanvasAndPost(mCanvas);   
    				    Util.sleep(1000);
                       // exit();
    				} else {
                        str = new String("Failed");
                        mCanvas.drawText(str, 300, 996, mPaint);
                        surHolder.unlockCanvasAndPost(mCanvas);
                    }
                //surHolder.unlockCanvasAndPost(mCanvas);
            }

            @Override
            public void FingerUp() {
                drawBackground();
            }
        };

        // 绘制位图
        private boolean drawBmp(byte bytes[], int x, int y) {
            Bitmap obmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (obmp == null) {
                mCanvas.drawText("Bmp NULL", x, y, mPaint);
                return false;
            } else {
                Bitmap sbmp = scaleBmp(obmp, mScale);
                Bitmap tbmp = toGreyBmp(sbmp);
                mCanvas.drawBitmap(tbmp, x, y, mPaint);
                if (!obmp.isRecycled())
                    obmp.recycle();
                if (!tbmp.isRecycled())
                    tbmp.recycle();
                if (!sbmp.isRecycled())
                    sbmp.recycle();
                return true;
            }
        }

        public void drawBackground() {
            mCanvas = surHolder.lockCanvas();
            if (mCanvas == null)
                return;
            mCanvas.drawColor(Color.WHITE);
			
			mCanvas.drawRect(titleRect, TitleRectPaint);
			mCanvas.drawText(getResources().getString(R.string.ma_capture_tip_init), userTipPoint.x,userTipPoint.y, mTipsPaint);
			mCanvas.drawText(getResources().getString(R.string.ma_capture_title), titlePoint.x,titlePoint.y, mTitlePaint);
           
		    surHolder.unlockCanvasAndPost(mCanvas);
        }

        public void drawText(int x, int y, String str) {
            mCanvas = surHolder.lockCanvas();
            mCanvas.drawText(str, x, y, mPaint);		
            surHolder.unlockCanvasAndPost(mCanvas);
        }

        /**
         * 将彩色图转换为灰度图
         * 
         * @bmp 位图
         * @return 返回转换好的位图
         */
        private Bitmap toGreyBmp(Bitmap bmp) {
            int width = bmp.getWidth(); // 获取位图的宽
            int height = bmp.getHeight(); // 获取位图的高
            int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
            bmp.getPixels(pixels, 0, width, 0, 0, width, height);
            int alpha = 0xFF << 24;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int grey = pixels[width * i + j];
                    int red = ((grey & 0x00FF0000) >> 16);
                    int green = ((grey & 0x0000FF00) >> 8);
                    int blue = (grey & 0x000000FF);
                    grey = (red + green + blue) / 3;
                    grey = alpha | (grey << 16) | (grey << 8) | grey;
                    pixels[width * i + j] = grey;
                }
            }
            Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
            result.setPixels(pixels, 0, width, 0, 0, width, height);
            return result;
        }

        private Bitmap scaleBmp(Bitmap bmp, float scale) {
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap result = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                    bmp.getHeight(), matrix, true);
            return result;
        }
    }

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PhoneStatusBroadcastReceiver.screen_off:
                case PhoneStatusBroadcastReceiver.key_home:
                    exit();
                    break;

            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_HOME
                || keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
        }
        return super.onKeyDown(keyCode, event);
    }

}
