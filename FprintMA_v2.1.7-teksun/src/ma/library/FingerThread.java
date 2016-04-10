
package ma.library;

import java.util.ArrayList;

import ma.release.Fprint;

//import android.os.Handler;

public class FingerThread extends Thread {
    public static final int SPI_COMMUNICATE_FAIL = -1;
    public static final int FINGER_NONE = 0;
    public static final int FINGER_HALF_DOWN = 1;
    public static final int FINGER_FULL = 2;
    public static final int FINGER_UP = 3;

    public static final int MATCH_SUCESS = 20;
    public static final int MATCH_FAIL = 21;
    public static final int MATCH_F1_OVER_TIME = 22;

    private boolean isThreadStop = false;
    private boolean hasDown = false;
    private boolean isThreadWait = false;

    private Object fingerThreadLock = new Object();
    private int checkReturn;

//    private ArrayList<Handler> mHandlers;
    private ArrayList<FingerAction> mFingerActions;

    private static FingerThread mFingerThread = null;

    private FingerThread() {
        init();
//        mHandlers = new ArrayList<Handler>();
        mFingerActions = new ArrayList<FingerAction>();
        this.setName("FingerThread");
        start();
    }

    public static FingerThread getFingerThreadInstance() {
        if (mFingerThread == null)
            mFingerThread = new FingerThread();
        return mFingerThread;
    }

    public void FingerThreadWait() {
        if (!isThreadWait)
            isThreadWait = true;
    }

    public void FingerThreadResume() {
        if (isThreadWait) {
            isThreadWait = false;
            synchronized (fingerThreadLock) {
                fingerThreadLock.notify();
            }
        }

    }

    public void stopFingerThread() {
        isThreadStop = true;
    }
//
//    public void setListener(Handler listener) {
//        mHandlers.add(listener);
//    }
//
//    public void removeListener(Handler listener) {
//        mHandlers.remove(listener);
//    }

    public void setAction(FingerAction fingerAction) {
        mFingerActions.add(fingerAction);
    }

    public void removeAction(FingerAction fingerAction) {
        mFingerActions.remove(fingerAction);
    }

    public void run() {

        checkReturn = Fprint.check(1);

        while (!isThreadStop) {
            if (isThreadWait) {
                Fprint.start();
                try {
                    synchronized (fingerThreadLock) {
                        fingerThreadLock.wait();
                    }
                } catch (InterruptedException e) {
                }
                hasDown = false;
                checkReturn = Fprint.check(1);
            }

            switch (checkReturn) {
                case FINGER_NONE:
                case FINGER_HALF_DOWN:
                    hasDown = false;
//                    notifyListener(FINGER_NONE);
                    if (isThreadWait)
                        break;
                    checkReturn = Fprint.check(0);
                    break;
                case FINGER_FULL:
                    if (!hasDown) {
                        hasDown = true;
                        actionsDown();
//                        notifyListener(FINGER_DOWN);
                    	}
                    if (isThreadWait)
                        break;
                    checkReturn = Fprint.check(0);
                    break;
                case FINGER_UP:
                    hasDown = false;
                    actionsUp();
//                    notifyListener(FINGER_UP);
                    if (isThreadWait)
                        break;
                    checkReturn = Fprint.check(1);
                    break;
                case 4:
                	hasDown = false;
                	if (isThreadWait)
                        break;
                    checkReturn = Fprint.check(0);
                    break;
                case SPI_COMMUNICATE_FAIL:
                default:
                    hasDown = false;
                    if (isThreadWait)
                        break;
                    Fprint.start();
                    checkReturn = Fprint.check(1);
                    break;
            }
        }

    }

    private void actionsDown() {
        for (int i = 0; i < mFingerActions.size(); i++) {
            if (isThreadWait)
                return;
            mFingerActions.get(i).FingerDown();
        }
    }

    private void actionsUp() {
        for (int i = 0; i < mFingerActions.size(); i++) {
            if (isThreadWait)
                return;
            mFingerActions.get(i).FingerUp();
        }
    }

//    private void notifyListener(int msg) {
//        for (int i = 0; i < mHandlers.size(); i++) {
//            if (isThreadWait)
//                return;
//            mHandlers.get(i).sendEmptyMessage(msg);
//        }
//    }

    private int init() {
        int ret = Fprint.open();
        if (ret < 0) {

        } else {
            ret = Fprint.load();
            if (ret < 0) {

            }
        }
        return ret;
    }

}
