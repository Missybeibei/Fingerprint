package ma.statusMachine;

import ma.library.Util;
import ma.release.Fprint;

public class SensorIdle implements SensorStates {
	
	@Override
	public int check() {
		System.out.println("hello iam in idel state checking");
		// add by cpy @ 1017_12.06
		Util.cancelAlarm(FingerPrintMachine.getContext(),FingerPrintMachine.getSender());
		// add by cpy @ 1017_12.06
		Sensor.getInstance().disableInterrupt = true;
		Fprint.start();
		wakeUp();
		Util.sleep(50);
		System.out.println("hello check 1" + Fprint.check(1));
		Sensor.getInstance().setStates(Sensor.getInstance().getSensorSoftCheck());
		return 0;
	}

	@Override
	public int match() {
		return -1;
	}

	private int wakeUp() {
		return Fprint.power(Fprint.POWER_WAKE);
	}
	@Override
	public void reset() {
		Sensor.getInstance().setStates(Sensor.getInstance().getSensorIdle());
	}

	@Override
	public void refresh() {
		
	}

}
