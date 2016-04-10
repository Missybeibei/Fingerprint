package ma.statusMachine;

import ma.release.Fprint;

public class SensorHardCheck implements SensorStates {

	@Override
	public int check() {
		wakeUp();
		Sensor.getInstance().setStates(Sensor.getInstance().getSensorSoftCheck());
		return Fprint.check(1);

	}

	@Override
	public int match() {
		return -1;
	}

	public int wakeUp() {
		return Fprint.power(Fprint.POWER_WAKE);
	}

	@Override
	public void reset() {
		System.out.println("hello iam in hardcheck state reset");
		Sensor.getInstance().setStates(Sensor.getInstance().getSensorIdle());
	}

	@Override
	public void refresh() {
		System.out.println("hello hardcheck regresh");
       Fprint.start();
		wakeUp();
		while(Fprint.power(1) != 1);
	}

}
