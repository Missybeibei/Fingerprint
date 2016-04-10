package ma.library;

import ma.release.Fprint;

public class FingerEnroll {
	FingerThread mFingerThread = FingerThread.getFingerThreadInstance();
	EnrollAction mEnrollAction = null;
	
	int sel;
    private int curGrade = 0,preGrade = 0;
    
	public void startEnroll(int sel,EnrollAction ea) {
		this.sel = sel;
		this.mEnrollAction = ea;
		curGrade = 0;
		preGrade = 0;
        mFingerThread.FingerThreadWait();
        Util.sleep(20);
        
        boolean nIsStop = Fprint.isStoped();
				if (!nIsStop){
					Fprint.stop();	
				}
        Fprint.start();
        Fprint.power(Fprint.POWER_WAKE);
        
        mFingerThread.setAction(mFingerAction);
        mFingerThread.FingerThreadResume();
	}
	
	public void stopEnroll() {

        mFingerThread.FingerThreadWait();
        Util.sleep(20);
        Fprint.start();
        mFingerThread.removeAction(mFingerAction);
	}
	
	FingerAction mFingerAction = new FingerAction() {

		@Override
		public void FingerDown() {
			curGrade = Fprint.enroll(sel + 1);
            if (curGrade == Fprint.ENROLL_GRADE) { // 注册成功
                Fprint.update(sel + 1);
                mFingerThread.FingerThreadWait();
                Util.sleep(20);
                mEnrollAction.anounceSucessed();
            } else if (curGrade > 0) {
            	if(curGrade - preGrade >0){
            		mEnrollAction.anounceNext(curGrade);
            	}else if(curGrade - preGrade < 10) {
            		mEnrollAction.anounceMove(curGrade);
            	}
                if (curGrade == preGrade) {
                	System.out.println("hello fail");
                	mEnrollAction.fail();
                } else {
                    preGrade = curGrade;
                }
            }
		}

		@Override
		public void FingerUp() {
			mEnrollAction.fingerLeave();
		}
		
	};
}
