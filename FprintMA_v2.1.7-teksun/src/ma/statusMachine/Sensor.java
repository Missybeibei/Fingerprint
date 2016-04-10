package ma.statusMachine;

import android.os.IBinder;
import ma.library.IMatchAction;
import ma.library.MatchAction;
import ma.release.Fprint;

public class Sensor {
	
	enum Phone{
		SCREEN_ON,
		SCREEN_OFF
	}
	private SensorStates sensorIdle;
	private SensorStates sensorSoftCheck;
	private SensorStates sensorHardCheck;
	private SensorStates sensorRecognition;
	
	private SensorStates mStates;
	private Phone mPhoneStates;
	
	public boolean disableInterrupt = true;
	public boolean fingerDetecting = false;
	
	private IMatchAction mMatchAction = new IMatchAction() {
		public void FingerMatchSucess(int i) {}
		public void FingerMatchFail() {}
		@Override
		public IBinder asBinder() {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	private static Sensor mSensor;
	public static Sensor getInstance() {
		if(mSensor == null) {
			mSensor = new Sensor();
		}
		return mSensor;
	}
	
	private Sensor(){
		sensorIdle = new SensorIdle();
		sensorSoftCheck = new SensorSoftCheck();
		sensorHardCheck = new SensorHardCheck();
		sensorRecognition = new SensorRecognition();
		
		//TODO err 
		Fprint.open();
		Fprint.load();
		
		mStates = sensorIdle;
	}
	
	public int check() {
		return mStates.check();
	}
	
	public int match() {
		return mStates.match();
	}
	
	public void reset() {
		mStates.reset();
	}
	
	public void refresh() {
		mStates.refresh();
	}
	
	public IMatchAction getMatchAction() {
		return mMatchAction;
	}
	public void setMatchAction(IMatchAction fa) {
		mMatchAction = fa;
	}
	public void setStates(SensorStates ss) {
		this.mStates = ss;
	}
	public void setPhoneState(Phone p) {
		mPhoneStates = p;
	}
	public Phone getPhoneState() {
		return mPhoneStates;
	}
	public SensorStates getSensorIdle() {
		return sensorIdle;
	}
	public SensorStates getSensorSoftCheck() {
		return sensorSoftCheck;
	}
	public SensorStates getSensorHardCheck() {
		return sensorHardCheck;
	}
	public SensorStates getSensorRecognition() {
		return sensorRecognition;
	}
}
