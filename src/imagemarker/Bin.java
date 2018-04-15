package imagemarker;

public class Bin{
	protected int binNum;
	protected double max;
	/**
	 * max - min = loss / bins
	 */
	protected double loss;
	Bin(int binNum,double min,double max){
		this.binNum = binNum;
		this.max = max;
		//��ֹ���ֵ/loss������Χ
		this.loss = (max - min) / binNum * 1.0001;
	}
}