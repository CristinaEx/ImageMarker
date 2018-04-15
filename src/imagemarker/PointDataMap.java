package imagemarker;

import java.util.Vector;

/**
 * 颜色直方图
 * 可以调整bins获取不同区间的数据
 * @author ene
 *
 */
public class PointDataMap {
	/**
	 * 存储着颜色直方图的数据
	 * 第一维代表数据类型
	 * 第二维分binNum
	 */
	public Vector<Vector<Integer>> data = new  Vector<Vector<Integer>>();
	/**
	 * bins大小
	 */
	private Vector<Bin> bins;
	PointDataMap(){
		this.bins = new Vector<Bin>();
	}
	PointDataMap(Vector<Bin> bins){
		this.bins = bins;
	}
	/**
	 * 修改bins后数据清空
	 * (可拓展为重新计算数据)
	 * (这里不做拓展)
	 */
	public void setBins(Vector<Bin> bins) {
		this.bins = bins;
		data.clear();
		this.addDimension();
	}
	/**
	 * 增加一个数据
	 * 若数据的维度不符，则不进行该操作
	 * 数据最好为同一类型，不然bins进行分类时可能不准确，甚至异常
	 * @param p
	 */
	public void addPointData(PixelDataGetable p) {
		Vector<Double> d = p.getPixelData();
		if(d.size() != this.bins.size())return;	
		for(int index = 0;index < this.bins.size();index++) {
			int i = (int) (d.get(index) / this.bins.get(index).loss);
			data.get(index).set(i, new Integer(data.get(index).get(i).intValue() + 1));
		}
	}
	
	/**
	 * 增加一个维度Vector的数据
	 * @param p
	 */
	public void addLineData(Vector<PixelDataGetable> p) {
		if(p.isEmpty())return;
		Vector<Double> d = p.get(0).getPixelData();
		if(d.size() != this.bins.size())return;
		for(PixelDataGetable itFirst : p)
				d = itFirst.getPixelData();
				for(int index = 0;index < this.bins.size();index++) {
				int i = (int) (d.get(index) / this.bins.get(index).loss);
				data.get(index).set(i, new Integer(data.get(index).get(i).intValue() + 1));
			}		
	}
	
	/**
	 * 增加数据
	 * @param p
	 */
	public void addData(Vector<Vector<PixelDataGetable>> p) {
		if(p.isEmpty() || p.get(0).isEmpty())return;
		Vector<Double> d = p.get(0).get(0).getPixelData();
		if(d.size() != this.bins.size())return;
		for(Vector<PixelDataGetable> itFirst : p)
			for(PixelDataGetable itSecond : itFirst) {
				d = itSecond.getPixelData();
				for(int index = 0;index < this.bins.size();index++) {
					int i = (int) (d.get(index) / this.bins.get(index).loss);
					data.get(index).set(i, new Integer(data.get(index).get(i).intValue() + 1));
				}
			}		
	}
	
	/**
	 * 把另一个PointDataMap的值加在自己身上
	 * @param map
	 */
	public void addData(PointDataMap map) {
		if(this.data.isEmpty())this.addDimension();
		if(map.data.size() != this.data.size() || map.data.get(0).size() != this.data.get(0).size())
			return;
		for(int indexFirst = 0;indexFirst < this.bins.size();indexFirst++)
			for(int indexSecond = 0;indexSecond < this.bins.get(indexFirst).binNum;indexSecond++)
				data.get(indexFirst).set(indexSecond, new Integer(data.get(indexFirst).get(indexSecond).intValue() + map.data.get(indexFirst).get(indexSecond).intValue()));
	}
	
	/**
	 * 增加data维度数（需在bins设置后）
	 * @param num 维度数量
	 */
	private void addDimension() {	
		for(int dim = 0;dim < this.bins.size();dim++) {
			Vector<Integer> dimension = new Vector<Integer>();
			for(int index = 0;index < this.bins.get(dim).binNum;index++)
				dimension.add(new Integer(0));
			this.data.addElement(dimension);
		}		
	}
	
}

