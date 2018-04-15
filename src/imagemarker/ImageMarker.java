package imagemarker;

import java.awt.Image;
import java.io.File;
import java.util.Vector;

public class ImageMarker {
	ImageMarker(){}
	ImageMarker(ImageMarker m){
		this.path = m.path;
		this.book = m.book;
		this.markers = m.markers;
	}
	public String path = null;
	/*
	 * ��¼��ÿ��label�����int value
	 */
	private SthNumber book = null;
	/*
	 * �Ѿ�ȷ�ϵı��
	 */
	protected Vector<OneSthMarker> markers = new Vector<OneSthMarker>();
	/*
	 * ��markers�е����м�¼����¼��path
	 * ��¼��ʽ:
	 * {
	 * "things":{"index":����:{"label":"����","x1":x1,"y1":y1,"x2":x2,"y2":y2},...},
	 * "data":[[[ͼƬ����]]]
	 */
	public void mark(Image img,String fileName,boolean addData) {
		if(path == null)return;
		if(markers.isEmpty())return;
		for(OneSthMarker one : markers) {
			if(this.readBook(one.label) == Integer.MIN_VALUE)
				book.map.put(one.label, one.index);
			//System.out.println("$$");
			//System.out.println(one.x1);
			//System.out.println(one.y1);
			//System.out.println(one.x2);
			//System.out.println(one.y2);
			//System.out.println(one.label);
			//System.out.println(one.index);
		}
		File f = new File(fileName);
		DataRecorder.record(path + "\\" + f.getName().substring(0, f.getName().lastIndexOf('.'))+".json", markers, img , addData);
		this.book.reflashData();
		this.clear();
	}
	/*
	 * ���markers,marker
	 */
	public void clear() {
		this.markers.clear();
	}
	/*
	 * �޸�·��
	 */
	public void setPath(String path) {
		File file = new File(path);
		if(!file.exists())file.mkdirs();
		this.path = path;
		this.book = new SthNumber(path);
	}
	/**
	 * ����book���Ƿ�����˸�label��int value
	 * �����򷵻�Integer.MIN_VALUE
	 */
	public int readBook(String label) {
		if(book.map.containsKey(label))
			return book.map.get(label).intValue();
		return Integer.MIN_VALUE;
	}
	
	/**
	 * �ж��ļ��Ƿ����������ļ���
	 * @return
	 */
	public boolean checkFileExist(String fileName) {
		if(this.path == null)return false;
		File file = new File(this.path + "\\" + fileName.substring(0,fileName.lastIndexOf('.')) + ".json");
		return file.exists();
	}
	
	public void add(OneSthMarker one) {
		this.markers.addElement(one);
	}
	
	public boolean isEmpty() {
		return this.markers.isEmpty();
	}
	
	/**
	 * �ʵ�����Ȩ��������һ�ݱ��ݣ���������data���
	 * @return
	 */
	public ImageMarker presentOwnership() {
		ImageMarker m = new ImageMarker(this);
		this.markers = new Vector<OneSthMarker>();
		return m;
	}
}
