package imagemarker;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * 该类可对RGB三通道图像进行GraphBasedImageSegmentation
 * 目前支持runBasic方法
 * @author ene
 *
 */
public class GraphBasedImageSegmentation {
	
	private class RegionEnegine{	
		/**
		 * 顺序编号0~regionNum-1
		 * @param regionNum 初始化区域的数量
		 */
		RegionEnegine(int regionNum){
			for(int index = 0;index < regionNum;index++)
				regions.add(new Region());
		}
		
		/**
		 * 所控制的区域
		 */
		public List<Region> regions = new ArrayList<Region>();
		/**
		 * 吞并区域并在mask上记录
		 * regionFirst,regionSecond为要吞并的区域的编号
		 * diffMax为区域间最大差异
		 */
		/*
		public void mergeRegion(Line line,int regionFirst,int regionSecond, Vector<Vector<Integer>> mask,double diffMax) {
			//交换值，保证regions.get(regionFirst).num >= regions.get(regionSecond).num
			if(regions.get(regionFirst).num < regions.get(regionSecond).num) {
				regionFirst ^= regionSecond ^= regionFirst ^= regionSecond;
				line.x1 ^= line.x2 ^= line.x1 ^= line.x2;
				line.y1 ^= line.y2 ^= line.y1 ^= line.y2;
			}	
			regions.get(regionFirst).addRegion(regions.get(regionSecond),diffMax);
			boolean mode = true;
			boolean mod = false;
			for(int x = 0;x < mask.size();x++){
				for(int y = 0;y < mask.get(x).size();y++)
					if(mask.get(x).get(y) == regionSecond) {
						mask.get(x).set(y, regionFirst);
						mode = false;
					}
				if(mode && mod)break;
				if(!mode) {
					mode = true;
					mod = true;
				}
			}
		}
		*/
		
		/**
		 * 更快的合并两个区域，并在mask上记录
		 * (运用递归)
		 * @param line 线
		 * @param regionFirst
		 * @param regionSecond
		 * @param mask
		 * @param diffMax
		 */
		public void mergeRegionFast(Line line,int regionFirst,int regionSecond, Vector<Vector<Integer>> mask,double diffMax) {
			//交换值，保证regions.get(regionFirst).num >= regions.get(regionSecond).num
			if(regions.get(regionFirst).num < regions.get(regionSecond).num) {
				regionFirst ^= regionSecond;
				regionSecond ^= regionFirst;
				regionFirst ^= regionSecond;
				regions.get(regionFirst).addRegion(regions.get(regionSecond),diffMax);
				mergeRegionFastIterator(line.x2,line.y2,regionFirst,regionSecond,mask);	
			}
			else {
				regions.get(regionFirst).addRegion(regions.get(regionSecond),diffMax);
				mergeRegionFastIterator(line.x1,line.y1,regionFirst,regionSecond,mask);	
			}
				
		}
		/**
		 * 
		 * @param posiX  坐标X
		 * @param posiY	 坐标Y
		 * @param regionFirst
		 * @param regionSecond
		 * @param mask
		 */
		public void mergeRegionFastIterator(int posiX,int posiY,int regionFirst,int regionSecond, Vector<Vector<Integer>> mask) {
			mask.get(posiX).set(posiY, new Integer(regionFirst));
			if(posiX > 0 && mask.get(posiX - 1).get(posiY).intValue() == regionSecond)mergeRegionFastIterator(posiX - 1,posiY,regionFirst,regionSecond,mask);
			if(posiY > 0 && mask.get(posiX).get(posiY - 1).intValue() == regionSecond)mergeRegionFastIterator(posiX,posiY - 1,regionFirst,regionSecond,mask);
			if(posiX < mask.size() - 1 && mask.get(posiX + 1).get(posiY).intValue() == regionSecond)mergeRegionFastIterator(posiX + 1,posiY,regionFirst,regionSecond,mask);
			if(posiY < mask.get(posiX).size() - 1 && mask.get(posiX).get(posiY + 1).intValue() == regionSecond)mergeRegionFastIterator(posiX,posiY + 1,regionFirst,regionSecond,mask);
		}
		
	}	
	private class Region implements Comparable<Region>{
		/**
		 * 类内差异
		 */
		public double diff;
		/**
		 * 区域大小
		 */
		public int num;
		/**
		 * 是否存活
		 */
		public boolean alive = true;
		Region(){
			diff = 0.0;
			this.num = 1;
		}
		/**
		 * 吞并区域
		 */
		public void addRegion(Region r,double diffMax) {
			r.alive = false;
			this.num += r.num;
			/*
			 * 目前仅以色差和区域大小关联权重
			 */
			this.diff = diffMax;
		}
		@Override
		public int compareTo(Region o) {
			// TODO Auto-generated method stub
			if (this.num > o.num)
				return 1;
			else if (this.num < o.num)
				return -1;
			else
				return 0;
		}
	}
	
	private class Line implements Comparable<Line>{

		public int x1;
		public int y1;
		public int x2;
		public int y2;
		public double diff;
		
		Line(int x1 , int y1 , int x2 ,int y2 , double diff){
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.diff = diff;
		}
		
		@Override
		public int compareTo(Line o) {
			// TODO Auto-generated method stub
			if (this.diff > o.diff)
				return 1;
			else if (this.diff < o.diff)
				return -1;
			else
				return 0;
		}
		
	}
	/**
	 * img为输入图像,若其没有进行高斯滤波，则当min != -1时，运行速度会大打折扣
	 * 目前img必须为RGB三通道图
	 * k控制着合并的力度，k越大，则越趋向于合并
	 * min为区域面积最小值，若区域小于该值，则把它与和它差异最小的区域合并，为-1则不考虑该项
	 * 返回mask[0-x][0-y]
	 * 其值代表属于的区域[0,max)
	 */
	public Vector<Vector<Integer>> runBasic(Image img ,int k ,int min){
		Vector<Vector<Integer>> mask = new Vector<Vector<Integer>>();
		/*
		 * 给每个像素点编号
		 * 每个编号代表初始的区域
		 */
		for(int wIndex = 0 ;wIndex < img.getWidth(null);wIndex++) {
			mask.addElement(new Vector<Integer>());
			for(int hIndex = 0; hIndex < img.getHeight(null);hIndex++)
				mask.get(wIndex).addElement(wIndex * img.getHeight(null) + hIndex);
		}
		//获取图像像素数据
		BufferedImage bm = ImageReader.toBufferedImage(img);
		int rgbArray[] = new int[img.getWidth(null)*img.getHeight(null)];
		bm.getRGB(0, 0, img.getWidth(null), img.getHeight(null), rgbArray, 0, img.getWidth(null));
		Vector<Vector<PointDataRGB>> imageData = ImageReader.arrayToImageDataRGB(rgbArray, img.getWidth(null), img.getHeight(null));
		/*
		 * 初始化各个区域的管理员
		 * 管理员控制着:
		 * 类内差异大小(不相似度最大的一条边)
		 * 区域的大小
		 * 区域合并的新区域的记录(区域最大的一方吞并小的一方)
		 */
		RegionEnegine eneginer = new RegionEnegine(img.getHeight(null) * img.getWidth(null));
		List<Line> lines = this.getLines(imageData);
		Collections.sort(lines);
		List<Line> restLines = new ArrayList<Line>();
		for(Line line : lines) {
			int regionFirst = mask.get(line.x1).get(line.y1).intValue();
			int regionSecond = mask.get(line.x2).get(line.y2).intValue();
			if(regionFirst == regionSecond)continue;
			if(line.diff <= Math.min(eneginer.regions.get(regionFirst).diff + k/eneginer.regions.get(regionFirst).num,eneginer.regions.get(regionSecond).diff + k/eneginer.regions.get(regionSecond).num))
				eneginer.mergeRegionFast(line,regionFirst, regionSecond, mask, line.diff);
			else
				restLines.add(line);
		}
		/*
		 * 对最小区域无要求，则直接返回 
		 */
		if(min == -1)return mask;
		for(Line line : restLines) {
			int regionFirst = mask.get(line.x1).get(line.y1).intValue();
			int regionSecond = mask.get(line.x2).get(line.y2).intValue();
			if(regionFirst == regionSecond || (eneginer.regions.get(regionFirst).num > min && eneginer.regions.get(regionSecond).num > min))continue;
			if(eneginer.regions.get(regionFirst).alive && eneginer.regions.get(regionSecond).alive)
				eneginer.mergeRegionFast(line,regionFirst, regionSecond, mask, line.diff);
		}
		return mask;
	}
	
	/**
	 * 初始化Line
	 * 每个像素点当做节点
	 * @param imageData 图像数据
	 * @return 所有线类数据
	 */
	private List<Line> getLines(Vector<Vector<PointDataRGB>> imageDataRGB){
		List<Line> lines = new ArrayList<Line>();
		for(int x = 0;x < imageDataRGB.size() - 1;x++) {
			for(int y = 0;y < imageDataRGB.get(x).size() - 1;y++) {
				lines.add(new Line(x,y,x+1,y,imageDataRGB.get(x).get(y).getDistance(imageDataRGB.get(x+1).get(y))));
				//lines.add(new Line(x,y,x+1,y+1,imageDataRGB.get(x).get(y).getDistance(imageDataRGB.get(x+1).get(y+1))));
				lines.add(new Line(x,y,x,y+1,imageDataRGB.get(x).get(y).getDistance(imageDataRGB.get(x).get(y+1))));
				//lines.add(new Line(x+1,y,x,y+1,imageDataRGB.get(x+1).get(y).getDistance(imageDataRGB.get(x).get(y+1))));
				lines.add(new Line(imageDataRGB.size()-1,y,imageDataRGB.size()-1,y+1,imageDataRGB.get(imageDataRGB.size()-1).get(y).getDistance(imageDataRGB.get(imageDataRGB.size()-1).get(y+1))));
			}
			lines.add(new Line(x,imageDataRGB.get(x).size()-1,x+1,imageDataRGB.get(x).size()-1,imageDataRGB.get(x).get(imageDataRGB.get(x).size()-1).getDistance(imageDataRGB.get(x+1).get(imageDataRGB.get(x).size()-1))));
		}	
		return lines;		
	}
}
