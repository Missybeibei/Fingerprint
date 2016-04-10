package ma.statusMachine;

import java.io.IOException;
import java.io.OutputStream;

import android.app.Instrumentation;
import android.graphics.Color;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import ma.library.FingerAction;
import ma.library.FingerGestureDetector;
import ma.library.FingerThread;
import ma.library.ICaptureAction;
import ma.library.Util;
import ma.release.Fprint;
import ma.release.Jnifp;

public class FingerCaptureController {
	private final int MIN_GRADE = 70;
	
	private static boolean bRunning = false;
	private static FingerDetectRunnable fr = null;
	private byte[] srcBytes = null;
	private static ICaptureAction mCaptureAction;
	
	private int W = 120; // 位图宽
	private int H = 120; // 位图高

	public FingerCaptureController() {
		srcBytes = new byte[W * H + 1078];
	}
	
	public void setAction(ICaptureAction ca ) {
		mCaptureAction = ca;
	}
	
	public void stopCapture() {
		Log.d("JTAG", "Capture stop");
		FingerThread.getFingerThreadInstance().FingerThreadWait();
        Util.sleep(20);
        FingerThread.getFingerThreadInstance().removeAction(mFingerAction);
	}
	
	public void startCapture() {
		Log.d("JTAG", "Capturing");
		FingerThread.getFingerThreadInstance().FingerThreadWait();
        Util.sleep(20);
        FingerThread.getFingerThreadInstance().setAction(mFingerAction);
        FingerThread.getFingerThreadInstance().FingerThreadResume();
	}

	private FingerAction mFingerAction = new FingerAction() {

        @Override
        public void FingerDown() {

            if (srcBytes == null)
                return;
            int score = Jnifp.score(srcBytes, srcBytes.length);
            int grade = (int) (score / 12000);
            if (grade > MIN_GRADE) {
            	    try {
						mCaptureAction.FingerCaptureSucess(srcBytes, grade);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				    Util.sleep(100);
                   // exit();
			} else {
					try {
						mCaptureAction.FingerCaptureFail(srcBytes);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
             }
            
        }

        @Override
        public void FingerUp() {
            
        }
    };
	
}