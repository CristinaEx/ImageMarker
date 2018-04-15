package imagemarker;

/**
 * 对SelectiveSearch的参数设置
 * 对显示的设置
 * @author ene
 *
 */
public class SegmentationOptionsSetter {
	/**
	 * 对于颜色的依赖度，越大则判别越依赖于颜色
	 */
	public static double KCOLOR = 0.0133334;
	/**
	 * 对于纹理的依赖度，越大则判别越依赖于纹理
	 */
	public static double KTEXTURE = 0.0005;
	/**
	 * 显示的区域的最小像素点个数
	 */
	public static int MINPOINT = 4000;
	/**
	 * 显示区域的最大长宽比
	 */
	public static double MAXPORTION = 5;
	/**
	 * 初始化小区域的像素点
	 */
	public static int POINTNUM = 30;
	/**
	 * 初始小区域的K值
	 */
	public static int K = 1000;
}
