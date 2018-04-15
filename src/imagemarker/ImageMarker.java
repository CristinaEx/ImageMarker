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
	 * 记录了每个label代表的int value
	 */
	private SthNumber book = null;
	/*
	 * 已经确认的标记
	 */
	protected Vector<OneSthMarker> markers = new Vector<OneSthMarker>();
	/*
	 * 把markers中的所有记录均记录在path
	 * 记录格式:
	 * {
	 * "things":{"index":序列:{"label":"名字","x1":x1,"y1":y1,"x2":x2,"y2":y2},...},
	 * "data":[[[图片数据]]]
	 */
	public void mark(Image img,String fileName) {
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
		DataRecorder.record(path + "\\" + f.getName().substring(0, f.getName().lastIndexOf('.'))+".json", markers, img);
		this.book.reflashData();
		this.clear();
	}
	/*
	 * 清空markers,marker
	 */
	public void clear() {
		this.markers.clear();
	}
	/*
	 * 修改路径
	 */
	public void setPath(String path) {
		File file = new File(path);
		if(!file.exists())file.mkdirs();
		this.path = path;
		this.book = new SthNumber(path);
	}
	/**
	 * 搜索book中是否记载了该label的int value
	 * 若无则返回Integer.MIN_VALUE
	 */
	public int readBook(String label) {
		if(book.map.containsKey(label))
			return book.map.get(label).intValue();
		return Integer.MIN_VALUE;
	}
	
	/**
	 * 判断文件是否存在在输出文件夹
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
	 * 呈递所有权，即拷贝一份备份，并把自身data清空
	 * @return
	 */
	public ImageMarker presentOwnership() {
		ImageMarker m = new ImageMarker(this);
		this.markers = new Vector<OneSthMarker>();
		return m;
	}
}
