package ma.release; 

/**************************** release v2.1 ****************************/
public class Jnifp {   
	// 驱动命令
	public static final int DRV_DEBUG = 0x100;		 // 驱动信息
	public static final int DRV_IRQEN = DRV_DEBUG+1; // 驱动使能
	public static final int DRV_SPEED = DRV_DEBUG+2; // SPI速度
	public static final int DRV_RDLEN = DRV_DEBUG+3; // 读长度
	public static final int DRV_LINKD = DRV_DEBUG+4; // 连接设备
	public static final int DRV_STNUM = DRV_DEBUG+5; // 材料编号
	public static final int DRV_VDATE = DRV_DEBUG+6; // 驱动日期
		
	//JNI命令
	public static final int JNI_DEBUG   = 0x200;	// 应用调试信息	
	public static final int JNI_DB_CLR	= 0x201;	// 清除表数据
	public static final int JNI_DB_SYNC = 0x202; 	// 数据库同步：0 FULL，1 NORMAL, 2 OFF
	
	public static final int JNI_PMTC = 0x220; 		//设置TC
								
	static {   		
		System.loadLibrary("fprint-r");
	}
		
	/* 打开设备
	 * @dev 设备名
	 * @path 路径	
	 * @return >=0 文件指针
	 *      -1 打开设备失败
	 *	   	-2 内存分配失败
	 *	   	-3 打开数据库失败
	 *      -4 创建数据库表失败
	 */
	public static native int open(String dev, String path);           
	
	/* 关闭 */
	public static native int close();   
	
	public static native int ioctl(int cmd, int arg);	   
	 	
	/* 指纹校准	
	 * @return 0成功，-1内存分配失败，-2采集数据失败，
	 * 		-3保存数据失败，-4初始化库失败, -5 SPI通讯错误
	 */
	public static native int initFactory();      
	
	/* 加载数据	
	 * @grade 注册满分分数
	 * @times 匹配次数
	 * @return 0成功，-1未指纹校准, -2加载数据失败,-3 SPI通讯错误
	 */
	public static native int initBoot(int grade, int times);  	 	

	/* 手指检测
	 * @flag 复位标志 1复位，0不
	 * @return 0没有，1部分接触, 2完全接触, 3已移开, -1采图失败
	 */
	public static native int check(int flag);     
	
	/* 注册
	 * @fid 指纹ID, 范围：1～5
	 * @timeout 超时时间ms, 默认5s
	 * @return 评分为100注册成功，>0继续注册，-1注册失败
	 */
	public static native int enroll(int fid);	    

	/* 匹配
	 * @fid 手指id
	 * @return 匹配成功1, 0失败, -1参数错误
	 */
	public static native int match(int fid); 	  
			
	/* 清除指纹
	 * @fid 指纹ID
	 * @return 0成功，-1参数有误，-2保存失败
	 */
	public static native int clear(int fid); 	     
	
	/* 检测注册状态
	 * @opt 每个字节代表每个手指，1已注册，0未注册
	 * @len 长度 >5多余部分无效
	 * @return -1失败，否则0
	 */
	public static native int doState(byte[] opt, int len); // 注册手指状态	
		
	/* 更新数据
	 * @fid 指纹ID
	 * @return 0成功，否则失败
	 */
	public static native int update(int fid);        
	
	/* power 
	 * @val 0:唤醒，1休眠
	 * @return 1成功，0失败
	 */
	public static native int power(int val);
	
	/* 整机测试指纹评分
	 * @bmp 位图数据
	 * @len 位图大小
	 * @return 分数值
	 */
	public static native int score(byte[] buf, int len); 
	
	/* 开始
	 * @return 0成功, <0失败
	 */
	public static native int start();    
	
	/* 停止
	 * @return 0成功, <0失败
	 */
	public static native int stop();
			
	/* 检测是否已经停止
	 * @return 1停止，0运行
	 */
	public static native int isStoped();    
	
	/* 设置中断
	 * @return 0成功, -1失败
	 */
	public static native int setINT();	 
	
	/* 长按
	 * @time 按压时间
	 * @return 1长按，0没有, -1采图失败
	 */
	public static native int pressed(int time);
	///////////////////////////////////////////////////////////////	
	/* 打开日志文件
	 * @path 路径
	 * @return -1 打开失败, 否则成功
	 */
	public static native int lopen(String path); 
	
	// 关闭日志文件
	public static native int lclose(); 
	
	/* 写入log(调试信息)
	 * @buf 数据
	 * @len 长度
	 * @return 
	 */
	public static native int lwrite(byte[] buf, int len);   
} 


