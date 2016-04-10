package ma.release;

import ma.library.Util;

public class Fprint {     	
	public static final int PLAT_CMM = 0x10;		// 通用平台
	public static final int PLAT_MTK = PLAT_CMM+1;	// MTK平台
	public static final int STUFF_TSV = 1; 		// 陶瓷
	public static final int STUFF_PLC = 2;		// 塑封
	public static final int POWER_SLEEP = 1; 	// 休眠
	public static final int POWER_WAKE = 0;		// 唤醒
	
	public static final int ENROLL_GRADE = 100;  // 注册最大分数
	public static final int MATCH_TIMES = 3;	 // 匹配次数
		
	// 取平台DMA数据大小
	public static int size(int plat) {                                      
		final int W = 120;
		final int H = 120;
		int size;
		switch (plat) {
		case PLAT_MTK:
			size = 1024 * 15;
			break; 
		default: 
			size = (W + 1) * (H + 1) + 1;
		}
		return size;
	}   
	
	public static int open() {           
		String path = Util.getPath();		
		String dev = "/dev/madev0";
		int ret = Jnifp.open(dev, path);  
		if (ret >= 0) {  
			Jnifp.ioctl(Jnifp.JNI_DEBUG, 0x01);  // 显示调试信息
			//Jnifp.ioctl(Jnifp.DRV_STNUM, STUFF_PLC); 
			Jnifp.ioctl(Jnifp.DRV_RDLEN, size(PLAT_MTK));
			//Jnifp.ioctl(Jnifp.DRV_RDLEN, size(PLAT_CMM));
		}			
		return ret; 
	}    
		
	public static int close() {		
		return Jnifp.close();
	}
	
	public static int ioctl(int cmd, int arg) {
		return Jnifp.ioctl(cmd, arg);
	}
	
	public static int calibrate() {   
		return Jnifp.initFactory(); 
	}

	public static int load() {
		return Jnifp.initBoot(ENROLL_GRADE, MATCH_TIMES);
	}
		
	public static int check(int timeout) {
		return Jnifp.check(timeout);
	}
	
	public static int enroll(int fid) { 
		return Jnifp.enroll(fid);
	} 
	
	public static int match(int fid) {	  
		return Jnifp.match(fid);		
	}	
	
	public static int clear(int fid) {  
		return Jnifp.clear(fid);
	}
		
	public static int doState(byte []state) {
		return Jnifp.doState(state, state.length);
	}
	
	public static int update(int fid) { 
		return Jnifp.update(fid);
	}
	
	public static int power(int val) {
		return Jnifp.power(val);
	}
	
	public static int score(byte []buf, int len) {
		return Jnifp.score(buf, len);
	}
	
	public static int start() {
		return Jnifp.start();
	}
		
	public static int stop() {
		return Jnifp.stop();
	}	
	
	public static boolean isStoped() {
		return Jnifp.isStoped()==1? true: false;
	}
	
	public static int setINT() {
		return Jnifp.setINT();
	}
	
	 //按压事件
	public static int pressed(int time) {
		return Jnifp.pressed(time);
	}
	
	//////////////////// 日志 ///////////////////	
	public int lopen(String path) {
		return Jnifp.lopen(path);
	}
		
	public int lclose() {
		return Jnifp.lclose();
	}
		
	public int lwrite(byte[] buf, int len) {
		return Jnifp.lwrite(buf, len);
	}
	
}
