package imagemarker;

import java.util.Vector;

public class PointDataRGB implements PixelDataGetable{
	public int R;
	public int G;
	public int B;
	PointDataRGB(){
		//RGB未初始化
	}
	PointDataRGB(int R,int G,int B){
		this.R = R;
		this.G = G;
		this.B = B;
	}
	PointDataRGB(int RGB){
		/*
		 * 该RGB值未BufferedImage.getRGB()获得
		 */
		this.R = (RGB & 0xff0000 ) >> 16 ;
		this.G = (RGB & 0xff00 ) >> 8 ;
		this.B = (RGB & 0xff );	
	}
	
	//获取两个PointData的欧氏距离
	//即L2 norm
	public double getDistance(PointDataRGB p) {
		double distance = 0.0;
		distance += Math.pow(this.R - p.R , 2);
		distance += Math.pow(this.G - p.G , 2);
		distance += Math.pow(this.B - p.B , 2);
		distance = Math.sqrt(distance);
		return distance;
	}
	/**
	 * 获取一个包含像素点颜色数据的Vector
	 * 其中有三个Double对象
	 * 分别代表R，G，B
	 */
	@Override
	public Vector<Double> getPixelData() {
		Vector<Double> data = new Vector<Double>();
		data.addElement(new Double(this.R));
		data.addElement(new Double(this.G));
		data.addElement(new Double(this.B));
		return data;
	}
}

