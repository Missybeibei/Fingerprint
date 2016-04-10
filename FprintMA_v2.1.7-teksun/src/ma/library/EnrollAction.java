package ma.library;

public interface EnrollAction {

	void anounceNext(int grade);
	void anounceMove(int grade);
	void anounceSucessed();
	void fail();
	
	void fingerLeave();
}
