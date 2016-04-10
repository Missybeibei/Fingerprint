package ma.library;

interface ICaptureAction {
	
	void FingerCaptureSucess(out byte[] srcBytes, int grade);
	void FingerCaptureFail(out byte[] srcBytes);
	
}