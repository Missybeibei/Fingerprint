package ma.library;

import ma.release.Fprint;
import ma.release.Jnifp;

public class FingerCapture {
	
	FingerThread mFingerThread = FingerThread.getFingerThreadInstance();
	CaptureAction mCaptureAction = null;
	private byte srcBytes[] = null;
	
    private int W = 120; // 位图宽
    private int H = 120; // 位图高
	public FingerCapture() {
		srcBytes = new byte[W * H + 1078];
	}
	
	public void startEnroll(CaptureAction ca) {
		
		mCaptureAction = ca;
        mFingerThread.FingerThreadWait();
        mFingerThread.setAction(mFingerAction);
        Util.sleep(20);
        Fprint.start();
        
        mFingerThread.FingerThreadResume();
	}
	
	public void stopEnroll() {
		srcBytes = null;
        mFingerThread.removeAction(mFingerAction);
        mFingerThread.FingerThreadWait();
        Util.sleep(20);
        Fprint.start();
        
	}
	
	FingerAction mFingerAction = new FingerAction() {

		@Override
		public void FingerDown() {
			if (srcBytes == null)
                return;
            int score = Jnifp.score(srcBytes, srcBytes.length);
            int grade = (int) (score / 12000);
            mCaptureAction.drawPicture(grade, srcBytes);
		}

		@Override
		public void FingerUp() {
			mCaptureAction.drawCancel();
		}
		
	};
	
	
	
}
