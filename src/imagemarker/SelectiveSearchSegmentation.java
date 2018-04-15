package imagemarker;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * 该类可对RGB三通道图像进行SelectiveSearch
 * 目前支持runBasic方法
 * @author ene
 *
 */
public class SelectiveSearchSegmentation {
	private Vector<Bin> RGBBINS = new Vector<Bin>();
	private Vector<Bin> HSVBINS = new Vector<Bin>();
	private Vector<Bin> TEXBINS = new Vector<Bin>();
	private int imgSize;
	/**
	 * 升序插入diff
	 */
	private Set<Diff> diffs = new TreeSet<Diff>();
	
	SelectiveSearchSegmentation(){
		for(int index = 0;index < 3;index++) {
			RGBBINS.addElement(new Bin(25,0,256));
			//这里把八个向量分解至[0,3]的区间
			TEXBINS.addElement(new Bin(30,0,3));
		}
		HSVBINS.addElement(new Bin(25,0,360));	
		HSVBINS.addElement(new Bin(25,0,1));	
		HSVBINS.addElement(new Bin(25,0,1));	
	}
	
	public class Region implements Comparator<Region> {
		/**
		 * 区域在x轴上的最小值
		 */
		public int minX;
		/**
		 * 区域在y轴上的最小值
		 */
		public int minY;
		/**
		 * 区域在x轴上的最大值
		 */
		public int maxX;
		/**
		 * 区域在y轴上的最大值
		 */
		public int maxY;
		/**
		 * 区域的编号
		 */
		public int index;
		/**
		 * 区域拥有的像素点个数
		 */
		public int size;
		/**
		 * 颜色直方图(bins = 25)
		 */
		public PointDataMap colorMap;
		/**
		 * 纹理直方图
		 */
		public PointDataMap textureMap;
		/**
		 * 相邻的区域编号
		 */
		public Set<Integer> neighbours;
		
		@Override  
	    public int hashCode() {  
	        return this.index; 
	    }  
		
		Region(int index,int x,int y){
			minX = x;
			maxX = x;
			minY = y;
			maxY = y;
			this.index = index;
			size = 1;
			colorMap = new PointDataMap();
			colorMap.setBins(HSVBINS);
			textureMap = new PointDataMap();
			textureMap.setBins(TEXBINS);
			neighbours = new  HashSet<Integer>();
		}
		
		Region(int index){
			minX = Integer.MAX_VALUE;
			maxX = Integer.MIN_VALUE;
			minY = Integer.MAX_VALUE;
			maxY = Integer.MIN_VALUE;
			this.index = index;
			size = 0;
			colorMap = new PointDataMap();
			colorMap.setBins(HSVBINS);
			textureMap = new PointDataMap();
			textureMap.setBins(TEXBINS);
			neighbours = new  HashSet<Integer>();
		}
		
		/**
		 * 初始化更新点的坐标和颜色空间
		 * @param x
		 * @param y
		 * @param pixel
		 */
		public void reflashPoint(int x,int y,PixelDataGetable pixel) {
			this.size += 1;
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
			colorMap.addPointData(pixel);
		}
		
		/**
		 * 计算纹理值
		 * 值 \in [0,1]
		 * 运用高斯分布函数 \sigma  == 1
		 * @param p1
		 * @param p2
		 * @param mod 方向(上下为0，左右为1，斜向为2)
		 */
		public void reflashTexture(PixelDataGetable p1,PixelDataGetable p2,int mod) {
			Vector<Double> p1Data = p1.getPixelData();
			Vector<Double> p2Data = p2.getPixelData();
			this.textureMap.addPointData(new PointData(Math.exp(-Math.pow((p1Data.get(0) - p2Data.get(0)), 2)/2)/Math.sqrt(2 * Math.PI) + mod,Math.exp(-Math.pow((p1Data.get(1) - p2Data.get(1)), 2)/2)/Math.sqrt(2 * Math.PI) + mod,Math.exp(-Math.pow((p1Data.get(2) - p2Data.get(2)), 2)/2)/Math.sqrt(2 * Math.PI) + mod));
		}

		@Override
		public int compare(Region o1, Region o2) {
			if(o1.index > o2.index)
				return 1;
			else if(o1.index < o2.index)
				return -1;
			else
				return 0;
		}
		
		/**
		 * 把一个区域合并到该区域
		 */
		public void addRegion(Region other) {
			this.minX = Math.min(this.minX,other.minX);
			this.maxX = Math.max(this.maxX,other.maxX);
			this.minY = Math.min(this.minY,other.minY);
			this.maxY = Math.max(this.maxY,other.maxY);
			this.size += other.size;
			this.colorMap.addData(other.colorMap);
			this.textureMap.addData(other.textureMap);
			for(Integer neighbour : other.neighbours)
				if(!neighbour.equals(this.index))this.neighbours.add(neighbour);
			Iterator<Integer> it = this.neighbours.iterator();
			while(it.hasNext()) {
				Integer obj = it.next();
				if(obj.equals(other.index)) {
					it.remove();
					break;
				}
			}
		}
		public void removeNeighbour(int regionIndex) {
			Iterator<Integer> it = this.neighbours.iterator();
			while(it.hasNext()) {
				Integer obj = it.next();
				if(obj.equals(regionIndex)) {
					it.remove();
					break;
				}
			}
		}
	}
	
	private class RegionEneginer{
		public Vector<Region> regions = new Vector<Region>();
		RegionEneginer(int num){
			for(int index = 0;index < num;index++)
				regions.addElement(new Region(index));
		}
		/**
		 * 初始化区域
		 * @param result
		 * @param mask 
		 */
		private void setRegions(Vector<Vector<Integer>> mask,Vector<Vector<PointDataRGB>> imageDataRGB,Vector<Vector<PointDataHSV>> imageDataHSV) {
			for(int x = 0;x < mask.size();x++)
				for(int y = 0;y < mask.get(x).size();y++)
					this.regions.get(mask.get(x).get(y)).reflashPoint(x, y,imageDataHSV.get(x).get(y));
			this.checkPointRound(mask, imageDataRGB);
		}
		
		/**
		 * 检查mask的像素，并执行相应操作:
		 * 基于RGB颜色空间的纹理检查
		 */
		private void checkPointRound(Vector<Vector<Integer>> mask,Vector<Vector<PointDataRGB>> imageDataRGB) {
			for(int x = 0;x < mask.size() - 1;x++) {
				for(int y = 0;y < mask.get(x).size() - 1;y++) {
					if(mask.get(x).get(y).equals(mask.get(x + 1).get(y)))
						this.regions.get(mask.get(x).get(y)).reflashTexture(imageDataRGB.get(x).get(y), imageDataRGB.get(x + 1).get(y),1);
					else {
						this.regions.get(mask.get(x).get(y)).neighbours.add(mask.get(x + 1).get(y));
						this.regions.get(mask.get(x + 1).get(y)).neighbours.add(mask.get(x).get(y));
					}
					if(mask.get(x).get(y).equals(mask.get(x + 1).get(y + 1)))
						this.regions.get(mask.get(x).get(y)).reflashTexture(imageDataRGB.get(x).get(y), imageDataRGB.get(x + 1).get(y + 1),2);
					else {
						this.regions.get(mask.get(x).get(y)).neighbours.add(mask.get(x + 1).get(y + 1));
						this.regions.get(mask.get(x + 1).get(y + 1)).neighbours.add(mask.get(x).get(y));
					}
					if(mask.get(x).get(y).equals(mask.get(x).get(y + 1)))
						this.regions.get(mask.get(x).get(y)).reflashTexture(imageDataRGB.get(x).get(y), imageDataRGB.get(x).get(y + 1),0);
					else {
						this.regions.get(mask.get(x).get(y)).neighbours.add(mask.get(x).get(y + 1));
						this.regions.get(mask.get(x).get(y + 1)).neighbours.add(mask.get(x).get(y));
					}
					if(mask.get(x).get(y + 1).equals(mask.get(x + 1).get(y)))
						this.regions.get(mask.get(x).get(y + 1)).reflashTexture(imageDataRGB.get(x).get(y + 1), imageDataRGB.get(x + 1).get(y),2);
					else {
						this.regions.get(mask.get(x).get(y + 1)).neighbours.add(mask.get(x + 1).get(y));
						this.regions.get(mask.get(x + 1).get(y)).neighbours.add(mask.get(x).get(y + 1));
					}
					if(mask.get(mask.size() - 1).get(y).equals(mask.get(mask.size() - 1).get(y + 1)))
						this.regions.get(mask.get(mask.size() - 1).get(y)).reflashTexture(imageDataRGB.get(mask.size() - 1).get(y), imageDataRGB.get(mask.size() - 1).get(y + 1),0);
					else {
						this.regions.get(mask.get(mask.size() - 1).get(y + 1)).neighbours.add(mask.get(mask.size() - 1).get(y));
						this.regions.get(mask.get(mask.size() - 1).get(y)).neighbours.add(mask.get(mask.size() - 1).get(y + 1));
					}
				}
				if(mask.get(x).get(mask.get(x).size() - 1).equals(mask.get(x + 1).get(mask.get(x).size() - 1)))
					this.regions.get(mask.get(x).get(mask.get(x).size() - 1)).reflashTexture(imageDataRGB.get(x).get(mask.get(x).size() - 1), imageDataRGB.get(x + 1).get(mask.get(x).size() - 1),1);
				else {
					this.regions.get(mask.get(x).get(mask.get(x).size() - 1)).neighbours.add(mask.get(x + 1).get(mask.get(x).size() - 1));
					this.regions.get(mask.get(x + 1).get(mask.get(x).size() - 1)).neighbours.add(mask.get(x).get(mask.get(x).size() - 1));
				}
			}
		}
		
		/**
		 * 合并diff规定的两个区域,diff.regionFirst吞并了diff,regionSecond
		 * @param diff
		 * @return 
		 */
		public void mergeRegion(Diff diff) {
			this.regions.get(diff.regionFirst).addRegion(this.regions.get(diff.regionSecond));
		}
	}
	
	private class Diff implements Comparable<Diff> {
		public double diff;
		public int regionFirst;
		public int regionSecond;
		
		/**
		 * 初始化时，保证regionFirst > regionSecond;
		 * 颜色相似度 \in color;
		 * 纹理相似度 \in texture;
		 * 大小相似度 \in [0,1];
		 * 吻合相似度 \in [0,1];
		 * diff越大，相似度越大
		 * @param regionFirst
		 * @param regionSecond
		 */
		Diff(Region regionFirst,Region regionSecond){
			this.regionFirst = Math.max(regionFirst.index, regionSecond.index);
			this.regionSecond = Math.min(regionFirst.index, regionSecond.index);
			//颜色相似度
			//使HSV颜色空间H处于[0,1]
			for(int indexY = 0;indexY < HSVBINS.get(0).binNum;indexY++)
				this.diff += Math.min(regionFirst.colorMap.data.get(0).get(indexY), regionSecond.colorMap.data.get(0).get(indexY)) / 360;
			for(int index = 1;index < HSVBINS.size();index++)
				for(int indexY = 0;indexY < HSVBINS.get(index).binNum;indexY++)
					this.diff += Math.min(regionFirst.colorMap.data.get(index).get(indexY), regionSecond.colorMap.data.get(index).get(indexY));
			//超参数
			this.diff *= SegmentationOptionsSetter.KCOLOR;
			//纹理(复杂)相似度
			for(int index = 0;index < TEXBINS.size();index++)
				for(int indexY = 0;indexY < TEXBINS.get(index).binNum;indexY++)
					this.diff += Math.min(regionFirst.textureMap.data.get(index).get(indexY), regionSecond.textureMap.data.get(index).get(indexY)) * SegmentationOptionsSetter.KTEXTURE;
			//大小相似度
			this.diff += (1 - (regionFirst.size + regionSecond.size) / imgSize);
			//吻合相似度
			this.diff += (1 - ((Math.max(regionFirst.maxX, regionSecond.maxX) - Math.min(regionFirst.minX, regionSecond.minX)) * (Math.max(regionFirst.maxY, regionSecond.maxY) - Math.min(regionFirst.minY, regionSecond.minY)) - regionFirst.size - regionSecond.size) / imgSize);
		}
		
		/**
		 * 倒叙排列
		 */
		@Override
		public int compareTo(Diff arg0) {
			// TODO Auto-generated method stub
			if (this.diff > arg0.diff)
				return -1;
			else if (this.diff < arg0.diff)
				return 1;
			else
				return 0;
		}
		
		/**
		 * 使用String的hashcode
		 * @return
		 */
		public String hashcode() {
			return String.valueOf(this.regionFirst) + String.valueOf(this.regionSecond);
		}
		
		@SuppressWarnings("unlikely-arg-type")
		public boolean equals(Object obj){
			if (this == obj)
	            return true;
	        if (obj == null)
	            return false;
	        if(this.hashcode().equals(obj.hashCode()))
	        	return true;
	        else if(this.hashcode().equals(obj))
	        	return true;
	        else
	        	return false;
		}
	}
	
	/**
	 * 区域间差值初始化
	 */
	private void setDiff(RegionEneginer eneginer) {
		for(Region region : eneginer.regions) {
			for(int regionIndex : region.neighbours)
				diffs.add(new Diff(region,eneginer.regions.get(regionIndex)));		
		}
	}
	
	public Vector<Region> runBasic(Image img,int k,int min) {
		//获取图像像素数据
		this.imgSize = img.getWidth(null)*img.getHeight(null);
		BufferedImage bm = ImageReader.toBufferedImage(img);
		int rgbArray[] = new int[this.imgSize];
		bm.getRGB(0, 0, img.getWidth(null), img.getHeight(null), rgbArray, 0, img.getWidth(null));
		Vector<Vector<PointDataRGB>> imageDataRGB = ImageReader.arrayToImageDataRGB(rgbArray, img.getWidth(null), img.getHeight(null));
		Vector<Vector<PointDataHSV>> imageDataHSV = new Vector<Vector<PointDataHSV>>();
		for(int x = 0;x < imageDataRGB.size();x++) {
			imageDataHSV.addElement(new Vector<PointDataHSV>());
			for(int y = 0;y < imageDataRGB.get(x).size();y++)
				imageDataHSV.get(x).addElement(new PointDataHSV(imageDataRGB.get(x).get(y)));
		}
		/*
		 * 参数 
		 * min = 30
		 */
		Vector<Vector<Integer>> mask = new GraphBasedImageSegmentation().runBasic(img, k, min);
		/*
		 * mask中的区域重新编号
		 */
		RegionEneginer eneginer = new RegionEneginer(this.reflashMask(mask));
		eneginer.setRegions(mask,imageDataRGB,imageDataHSV);
		/*
		 * 初始化diff
		 */
		this.setDiff(eneginer);
		/*
		 * 取出diffs第一一个
		 * (即diff最大的一个)
		 * 进行循环操作
		 * 直至diffs.size() == 0
		 */
		while(!diffs.isEmpty()) {
			//System.out.println(String.valueOf(diffs.size()));
			Iterator<Diff> it = diffs.iterator();
			boolean check = true;
			/*
			 * 需重新计算的区域
			 */
			int regionIn = -1;
			/*
			 * 需剔除的区域
			 */
			int regionOut = -1;
			/*
			 * 合并＋去重
			 */
			for(int index = 0;index < diffs.size();index++) {
				Diff diff = it.next();
				if(check) {	
					check = false;
					eneginer.mergeRegion(diff);
					regionIn = diff.regionFirst;
					regionOut = diff.regionSecond;
					it.remove();
					index--;
				}
				else if(diff.regionFirst == regionIn||diff.regionFirst == regionOut||diff.regionSecond == regionIn||diff.regionSecond == regionOut) {
						it.remove();
						index--;
					}
			}
			for(Region region : eneginer.regions)
				region.removeNeighbour(regionOut);
			/*
			 * 对regionIndex指向的区域进行重新计算区间差值
			 */
			for(int regionIndex : eneginer.regions.get(regionIn).neighbours)
				this.diffs.add(new Diff(eneginer.regions.get(regionIn),eneginer.regions.get(regionIndex)));
		}
		return eneginer.regions;
	}
	
	/**
	 * 更新mask，保证mask里的区域编号按照[0,max)
	 * 用以区域的初始化
	 * @param mask
	 */
	private int reflashMask(Vector<Vector<Integer>> mask) {
		TreeMap<Integer,Integer> tp = new TreeMap<Integer,Integer>();
		int newIndex = 0;
		for(int x = 0;x < mask.size();x++)
			for(int y = 0;y < mask.get(x).size();y++) {
				int index = mask.get(x).get(y).intValue();
				if(tp.containsKey(new Integer(index)))
					mask.get(x).set(y,tp.get(new Integer(index)));
				else {
					tp.put(new Integer(index), newIndex);
					mask.get(x).set(y, new Integer(newIndex++));
				}	
			}
		return newIndex;
	}
}

