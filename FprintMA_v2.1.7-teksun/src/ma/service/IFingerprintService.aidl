package ma.service;
import ma.library.FingerGestureDetector;
import ma.library.IMatchAction;
import ma.library.ICaptureAction;

interface IFingerprintService { 
    void startDetect();
    void cancleDetect();
    void registerAuthenticationCallBack(in IMatchAction ma);
    void unregisterAuthenticationCallBack(in IMatchAction ma);
    void registerCaptureCallBack(in ICaptureAction ca);
    void unregisterCaptureCallBack();
}