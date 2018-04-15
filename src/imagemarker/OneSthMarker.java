package imagemarker;

/**
 * 记录一件物体的信息(位置和标签)
 * @author ene
 *
 */
public class OneSthMarker {
	public int x1;
	public int y1;
	public int x2;
	public int y2;
	public String label;
	public int index;
	OneSthMarker(){
		this.x1 = 0;
		this.y1 = 0;
		this.x2 = 0;
		this.y2 = 0;
		this.label = "";
		this.index = Integer.MIN_VALUE;
	}
	public void clear() {
		this.x1 = 0;
		this.y1 = 0;
		this.x2 = 0;
		this.y2 = 0;
		this.label = "";
		this.index = Integer.MIN_VALUE;
	}
	
	/**
	 * 判断其数据是否满足
	 * @return
	 */
	public boolean checkFilled() {
		if(this.label.length() == 0)return false;
		else if(this.index == Integer.MIN_VALUE)return false;
		else if(this.x1 == 0 && this.y1 == 0 && this.x2 == 0 && this.y2 == 0)return false;
		return true;
	}
}
