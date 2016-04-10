package ma.statusMachine;

import ma.library.Util;
import ma.release.Fprint;

public class SensorSoftCheck implements SensorStates {
	boolean is = true;
	int counts;
	boolean down = false;

	@Override
	public int check() {
		counts++;
		int ret = Fprint.check(0);
		switch (ret) {
		case 0:
			System.out.println("hello 0 " + counts);
			if(counts >100) {
				if (Fprint.power(Fprint.POWER_SLEEP) == 0) {
					Sensor.getInstance().setStates(
							Sensor.getInstance().getSensorHardCheck());
					// add by cpy @ 1017_12.06
					System.out.println("hello refresh 1");
					Util.releasePowerLock();
					Util.setAlarm(FingerPrintMachine.getContext(), 1,
							FingerPrintMachine.getSender());
					// add by cpy @ 1017_12.06
					Sensor.getInstance().disableInterrupt = false;
					counts = 0;
					return 2;
				} else {
					Fprint.power(0);
				}
			}
			return 0;
		case 1:
		case 2:
			System.out.println("hello 1 2 " + counts);
			Sensor.getInstance().setStates(
					Sensor.getInstance().getSensorRecognition());
			counts = 0;
			return 1;
		case 4:
			System.out.println("hello 4 " + counts);
			counts = 0;
			return 0;
		case 3:
		default:
			Fprint.check(1);
			System.out.println("hello 0ther");
			counts = 0;
			break;
		}
		return -1;
	}

	@Override
	public int match() {
		return -1;
	}

	@Override
	public void reset() {
		counts = 0;
		Sensor.getInstance().setStates(Sensor.getInstance().getSensorIdle());
	}

	@Override
	public void refresh() {

	}

}
