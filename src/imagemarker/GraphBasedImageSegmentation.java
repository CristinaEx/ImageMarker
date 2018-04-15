package imagemarker;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * ����ɶ�RGB��ͨ��ͼ�����GraphBasedImageSegmentation
 * Ŀǰ֧��runBasic����
 * @author ene
 *
 */
public class GraphBasedImageSegmentation {
	
	private class RegionEnegine{	
		/**
		 * ˳����0~regionNum-1
		 * @param regionNum ��ʼ�����������
		 */
		RegionEnegine(int regionNum){
			for(int index = 0;index < regionNum;index++)
				regions.add(new Region());
		}
		
		/**
		 * �����Ƶ�����
		 */
		public List<Region> regions = new ArrayList<Region>();
		/**
		 * �̲�������mask�ϼ�¼
		 * regionFirst,regionSecondΪҪ�̲�������ı��
		 * diffMaxΪ�����������
		 */
		/*
		public void mergeRegion(Line line,int regionFirst,int regionSecond, Vector<Vector<Integer>> mask,double diffMax) {
			//����ֵ����֤regions.get(regionFirst).num >= regions.get(regionSecond).num
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
		 * ����ĺϲ��������򣬲���mask�ϼ�¼
		 * (���õݹ�)
		 * @param line ��
		 * @param regionFirst
		 * @param regionSecond
		 * @param mask
		 * @param diffMax
		 */
		public void mergeRegionFast(Line line,int regionFirst,int regionSecond, Vector<Vector<Integer>> mask,double diffMax) {
			//����ֵ����֤regions.get(regionFirst).num >= regions.get(regionSecond).num
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
		 * @param posiX  ����X
		 * @param posiY	 ����Y
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
		 * ���ڲ���
		 */
		public double diff;
		/**
		 * �����С
		 */
		public int num;
		/**
		 * �Ƿ���
		 */
		public boolean alive = true;
		Region(){
			diff = 0.0;
			this.num = 1;
		}
		/**
		 * �̲�����
		 */
		public void addRegion(Region r,double diffMax) {
			r.alive = false;
			this.num += r.num;
			/*
			 * Ŀǰ����ɫ��������С����Ȩ��
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
	 * imgΪ����ͼ��,����û�н��и�˹�˲�����min != -1ʱ�������ٶȻ����ۿ�
	 * Ŀǰimg����ΪRGB��ͨ��ͼ
	 * k�����źϲ������ȣ�kԽ����Խ�����ںϲ�
	 * minΪ���������Сֵ��������С�ڸ�ֵ������������������С������ϲ���Ϊ-1�򲻿��Ǹ���
	 * ����mask[0-x][0-y]
	 * ��ֵ�������ڵ�����[0,max)
	 */
	public Vector<Vector<Integer>> runBasic(Image img ,int k ,int min){
		Vector<Vector<Integer>> mask = new Vector<Vector<Integer>>();
		/*
		 * ��ÿ�����ص���
		 * ÿ����Ŵ����ʼ������
		 */
		for(int wIndex = 0 ;wIndex < img.getWidth(null);wIndex++) {
			mask.addElement(new Vector<Integer>());
			for(int hIndex = 0; hIndex < img.getHeight(null);hIndex++)
				mask.get(wIndex).addElement(wIndex * img.getHeight(null) + hIndex);
		}
		//��ȡͼ����������
		BufferedImage bm = ImageReader.toBufferedImage(img);
		int rgbArray[] = new int[img.getWidth(null)*img.getHeight(null)];
		bm.getRGB(0, 0, img.getWidth(null), img.getHeight(null), rgbArray, 0, img.getWidth(null));
		Vector<Vector<PointDataRGB>> imageData = ImageReader.arrayToImageDataRGB(rgbArray, img.getWidth(null), img.getHeight(null));
		/*
		 * ��ʼ����������Ĺ���Ա
		 * ����Ա������:
		 * ���ڲ����С(�����ƶ�����һ����)
		 * ����Ĵ�С
		 * ����ϲ���������ļ�¼(��������һ���̲�С��һ��)
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
		 * ����С������Ҫ����ֱ�ӷ��� 
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
	 * ��ʼ��Line
	 * ÿ�����ص㵱���ڵ�
	 * @param imageData ͼ������
	 * @return ������������
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
