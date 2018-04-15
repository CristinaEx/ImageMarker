package imagemarker;

import java.util.Vector;

public interface PixelDataGetable {
	/**
	 * 获取像素数据
	 * 可以是任意维度
	 * @return
	 */
	public Vector<Double> getPixelData();
}
