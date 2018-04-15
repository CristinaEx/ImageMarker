package imagemarker;

import java.util.Vector;

/**
 * ��ɫֱ��ͼ
 * ���Ե���bins��ȡ��ͬ���������
 * @author ene
 *
 */
public class PointDataMap {
	/**
	 * �洢����ɫֱ��ͼ������
	 * ��һά������������
	 * �ڶ�ά��binNum
	 */
	public Vector<Vector<Integer>> data = new  Vector<Vector<Integer>>();
	/**
	 * bins��С
	 */
	private Vector<Bin> bins;
	PointDataMap(){
		this.bins = new Vector<Bin>();
	}
	PointDataMap(Vector<Bin> bins){
		this.bins = bins;
	}
	/**
	 * �޸�bins���������
	 * (����չΪ���¼�������)
	 * (���ﲻ����չ)
	 */
	public void setBins(Vector<Bin> bins) {
		this.bins = bins;
		data.clear();
		this.addDimension();
	}
	/**
	 * ����һ������
	 * �����ݵ�ά�Ȳ������򲻽��иò���
	 * �������Ϊͬһ���ͣ���Ȼbins���з���ʱ���ܲ�׼ȷ�������쳣
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
	 * ����һ��ά��Vector������
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
	 * ��������
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
	 * ����һ��PointDataMap��ֵ�����Լ�����
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
	 * ����dataά����������bins���ú�
	 * @param num ά������
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

