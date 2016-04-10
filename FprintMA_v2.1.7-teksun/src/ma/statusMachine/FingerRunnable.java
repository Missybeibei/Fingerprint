package ma.statusMachine;

import ma.library.Util;
import ma.release.Fprint;

public class FingerRunnable implements Runnable {
	private static boolean isStop = false;
	private static FingerRunnable fr = null;
	
	public static void start() {
		stopRunnable();
		startRunnable();
	}
	public static void stopRunnable() {
		isStop = true;
		Util.sleep(50);
	}
	private static void startRunnable() {
		if(fr == null) fr =new FingerRunnable();
		isStop = false;
		new Thread(fr).start();
	}
	public void run() {
		Fprint.start();
		while (!isStop) {
			switch (Sensor.getInstance().check()) {
			case 0:
				break;
			case 1:
				if (Sensor.getInstance().match() == 1) {
					System.out.println("hello sucess match");
					return;
				} else {
					while(!isStop){
						if(Sensor.getInstance().check() == 3) break;
					}
					System.out.println("hello failed match");
					break;
				}
			case 2:
				System.out.println("hello set interrupt");
				return;
			}
		}
	}
}
