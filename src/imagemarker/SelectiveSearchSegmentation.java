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
 * ����ɶ�RGB��ͨ��ͼ�����SelectiveSearch
 * Ŀǰ֧��runBasic����
 * @author ene
 *
 */
public class SelectiveSearchSegmentation {
	private Vector<Bin> RGBBINS = new Vector<Bin>();
	private Vector<Bin> HSVBINS = new Vector<Bin>();
	private Vector<Bin> TEXBINS = new Vector<Bin>();
	private int imgSize;
	/**
	 * �������diff
	 */
	private Set<Diff> diffs = new TreeSet<Diff>();
	
	SelectiveSearchSegmentation(){
		for(int index = 0;index < 3;index++) {
			RGBBINS.addElement(new Bin(25,0,256));
			//����Ѱ˸������ֽ���[0,3]������
			TEXBINS.addElement(new Bin(30,0,3));
		}
		HSVBINS.addElement(new Bin(25,0,360));	
		HSVBINS.addElement(new Bin(25,0,1));	
		HSVBINS.addElement(new Bin(25,0,1));	
	}
	
	public class Region implements Comparator<Region> {
		/**
		 * ������x���ϵ���Сֵ
		 */
		public int minX;
		/**
		 * ������y���ϵ���Сֵ
		 */
		public int minY;
		/**
		 * ������x���ϵ����ֵ
		 */
		public int maxX;
		/**
		 * ������y���ϵ����ֵ
		 */
		public int maxY;
		/**
		 * ����ı��
		 */
		public int index;
		/**
		 * ����ӵ�е����ص����
		 */
		public int size;
		/**
		 * ��ɫֱ��ͼ(bins = 25)
		 */
		public PointDataMap colorMap;
		/**
		 * ����ֱ��ͼ
		 */
		public PointDataMap textureMap;
		/**
		 * ���ڵ�������
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
		 * ��ʼ�����µ���������ɫ�ռ�
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
		 * ��������ֵ
		 * ֵ \in [0,1]
		 * ���ø�˹�ֲ����� \sigma  == 1
		 * @param p1
		 * @param p2
		 * @param mod ����(����Ϊ0������Ϊ1��б��Ϊ2)
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
		 * ��һ������ϲ���������
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
		 * ��ʼ������
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
		 * ���mask�����أ���ִ����Ӧ����:
		 * ����RGB��ɫ�ռ��������
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
		 * �ϲ�diff�涨����������,diff.regionFirst�̲���diff,regionSecond
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
		 * ��ʼ��ʱ����֤regionFirst > regionSecond;
		 * ��ɫ���ƶ� \in color;
		 * �������ƶ� \in texture;
		 * ��С���ƶ� \in [0,1];
		 * �Ǻ����ƶ� \in [0,1];
		 * diffԽ�����ƶ�Խ��
		 * @param regionFirst
		 * @param regionSecond
		 */
		Diff(Region regionFirst,Region regionSecond){
			this.regionFirst = Math.max(regionFirst.index, regionSecond.index);
			this.regionSecond = Math.min(regionFirst.index, regionSecond.index);
			//��ɫ���ƶ�
			//ʹHSV��ɫ�ռ�H����[0,1]
			for(int indexY = 0;indexY < HSVBINS.get(0).binNum;indexY++)
				this.diff += Math.min(regionFirst.colorMap.data.get(0).get(indexY), regionSecond.colorMap.data.get(0).get(indexY)) / 360;
			for(int index = 1;index < HSVBINS.size();index++)
				for(int indexY = 0;indexY < HSVBINS.get(index).binNum;indexY++)
					this.diff += Math.min(regionFirst.colorMap.data.get(index).get(indexY), regionSecond.colorMap.data.get(index).get(indexY));
			//������
			this.diff *= SegmentationOptionsSetter.KCOLOR;
			//����(����)���ƶ�
			for(int index = 0;index < TEXBINS.size();index++)
				for(int indexY = 0;indexY < TEXBINS.get(index).binNum;indexY++)
					this.diff += Math.min(regionFirst.textureMap.data.get(index).get(indexY), regionSecond.textureMap.data.get(index).get(indexY)) * SegmentationOptionsSetter.KTEXTURE;
			//��С���ƶ�
			this.diff += (1 - (regionFirst.size + regionSecond.size) / imgSize);
			//�Ǻ����ƶ�
			this.diff += (1 - ((Math.max(regionFirst.maxX, regionSecond.maxX) - Math.min(regionFirst.minX, regionSecond.minX)) * (Math.max(regionFirst.maxY, regionSecond.maxY) - Math.min(regionFirst.minY, regionSecond.minY)) - regionFirst.size - regionSecond.size) / imgSize);
		}
		
		/**
		 * ��������
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
		 * ʹ��String��hashcode
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
	 * ������ֵ��ʼ��
	 */
	private void setDiff(RegionEneginer eneginer) {
		for(Region region : eneginer.regions) {
			for(int regionIndex : region.neighbours)
				diffs.add(new Diff(region,eneginer.regions.get(regionIndex)));		
		}
	}
	
	public Vector<Region> runBasic(Image img,int k,int min) {
		//��ȡͼ����������
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
		 * ���� 
		 * min = 30
		 */
		Vector<Vector<Integer>> mask = new GraphBasedImageSegmentation().runBasic(img, k, min);
		/*
		 * mask�е��������±��
		 */
		RegionEneginer eneginer = new RegionEneginer(this.reflashMask(mask));
		eneginer.setRegions(mask,imageDataRGB,imageDataHSV);
		/*
		 * ��ʼ��diff
		 */
		this.setDiff(eneginer);
		/*
		 * ȡ��diffs��һһ��
		 * (��diff����һ��)
		 * ����ѭ������
		 * ֱ��diffs.size() == 0
		 */
		while(!diffs.isEmpty()) {
			//System.out.println(String.valueOf(diffs.size()));
			Iterator<Diff> it = diffs.iterator();
			boolean check = true;
			/*
			 * �����¼��������
			 */
			int regionIn = -1;
			/*
			 * ���޳�������
			 */
			int regionOut = -1;
			/*
			 * �ϲ���ȥ��
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
			 * ��regionIndexָ�������������¼��������ֵ
			 */
			for(int regionIndex : eneginer.regions.get(regionIn).neighbours)
				this.diffs.add(new Diff(eneginer.regions.get(regionIn),eneginer.regions.get(regionIndex)));
		}
		return eneginer.regions;
	}
	
	/**
	 * ����mask����֤mask��������Ű���[0,max)
	 * ��������ĳ�ʼ��
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

