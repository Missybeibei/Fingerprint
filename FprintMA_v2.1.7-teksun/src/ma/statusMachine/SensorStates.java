package ma.statusMachine;

public interface SensorStates {
	//Sensor interface
	int check();
	int match();
	void reset();
	void refresh();
	
}
