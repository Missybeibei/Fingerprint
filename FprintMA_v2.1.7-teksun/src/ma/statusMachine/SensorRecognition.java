package ma.statusMachine;

import android.os.RemoteException;
import ma.library.Util;
import ma.release.Fprint;

public class SensorRecognition implements SensorStates {
	@Override
	public int check() {
		switch (Fprint.check(0)) {
		case 0:
			return 0;
		case 1:
		case 2:
			return 1;
		case 3:
        	Sensor.getInstance().reset();
			return 3;
		}
		return -1;
	}

	@Override
	public int match() {
        int i, matchRet = 0;
        for (i = 1; i < 6; i++) {
            matchRet = Fprint.match(i);
            if (matchRet > 0) {
                break;
            }
        }
        if(matchRet > 0) {
        	Sucessed(i);
        	//while(Fprint.update(i) == -2);
        	Fprint.update(i);
        	Sensor.getInstance().setStates(Sensor.getInstance().getSensorIdle());
        	return 1;
        }else{
        	Failed();
          return 0;
        }

	}

	private void Failed() {
		try {
			Sensor.getInstance().getMatchAction().FingerMatchFail();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void Sucessed(int i) {
    	Util.releasePowerLock();
		try {
			Sensor.getInstance().getMatchAction().FingerMatchSucess(i);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void reset() {
		Sensor.getInstance().setStates(Sensor.getInstance().getSensorIdle());
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

}
