package imagemarker;

import java.util.Vector;

/**
 * 三维数据的读取和输出
 * 实现PixelDataGetable接口
 * @author ene
 *
 */
public class PointData implements PixelDataGetable{

	Vector<Double> data;
	PointData(double d1,double d2,double d3){
		data = new Vector<Double>();
		this.data.addElement(new Double(d1));
		this.data.addElement(new Double(d2));
		this.data.addElement(new Double(d3));
	}
	
	@Override
	public Vector<Double> getPixelData() {
		// TODO Auto-generated method stub
		return data;
	}

}
