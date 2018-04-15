package imagemarker;

import java.util.Vector;

public class PointDataHSV implements PixelDataGetable{
	public double H;
	public double S;
	public double V;
	PointDataHSV(){
		//HSV未初始化
	}
	PointDataHSV(double H,double S,double V){
		this.H = H;
		this.S = S;
		this.V = V;
	}
	/**
	 * 把RGB转为HSV
	 * @param rgb 
	 * H \in [0,360]
	 * S \in [0,1]
	 * V \in [0,1]
	 */
	PointDataHSV(PointDataRGB rgb){
		this.V = Math.max(rgb.R, Math.max(rgb.G,rgb.B));
		double min = Math.min(rgb.R, Math.min(rgb.G, rgb.B));
		if(this.V == 0) {
			this.S = 0;
			this.H = 0;
		}
		else{
			this.S = (this.V - min)/this.V;
			if(this.V == min)this.H = 0;
			else if(this.V == rgb.R)this.H = 60 * (rgb.G - rgb.B)/(this.V - min);
			else if(this.V == rgb.G)this.H = 120 + 60 * (rgb.B - rgb.R)/(this.V - min);
			else this.H = 240 + 60 * (rgb.R - rgb.G)/(this.V - min);
			while(this.H < 0)this.H += 360;
		}	
		this.V /= 256;
	}
	
	/**
	 * 获取一个包含像素点颜色数据的Vector
	 * 其中有三个Double对象
	 * 分别代表H，S，V
	 */
	@Override
	public Vector<Double> getPixelData() {
		Vector<Double> data = new Vector<Double>();
		data.addElement(new Double(this.H));
		data.addElement(new Double(this.S));
		data.addElement(new Double(this.V));
		return data;
	}
}
