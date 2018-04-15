package imagemarker;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

public class DataRecorder {
	/*
	 * 把markers中的所有记录均记录在path
	 * 记录格式:
	 * {
	 * "things":[{"index":序列,"label":"名字","x1":x1,"y1":y1,"x2":x2,"y2":y2},...],
	 * "data":[[[图片数据]]]
	 * 图片数据格式:
	 * [[第一列],[第二列]...]
	 */
	public static void record(String path, Vector<OneSthMarker> markers,Image img,boolean addData) {
		//获取图像像素数据
		BufferedImage bm = ImageReader.toBufferedImage(img);
		int rgbArray[] = new int[img.getWidth(null)*img.getHeight(null)];
		//数据格式:
		//[(0,0),(0,1)...(0,h-1),(1,0)...(w-1,h-1)]
		bm.getRGB(0, 0, img.getWidth(null), img.getHeight(null), rgbArray, 0, img.getWidth(null));
		Vector<Vector<PointDataRGB>> imageData = ImageReader.arrayToImageDataRGB(rgbArray, img.getWidth(null), img.getHeight(null));
		File file = new File(path);
		try {
			file.createNewFile();
			OutputStreamWriter out=new OutputStreamWriter(new FileOutputStream(file), "utf-8");
			out.append("{\n" +
					"	\"things\":[");
			for(OneSthMarker marker:markers)
				out.append("{\"index\":"+String.valueOf(marker.index)+","+
						"\"label\":\""+marker.label+"\","+
						"\"x1\":"+String.valueOf(marker.x1)+","+
						"\"y1\":"+String.valueOf(marker.y1)+","+
						"\"x2\":"+String.valueOf(marker.x2)+","+
						"\"y2\":"+String.valueOf(marker.y2)+"}"+ (markers.indexOf(marker)<markers.size()-1?",":"")
						);
			//append data start
			if(addData) {
				out.append("],\n"+
						"	\"data\":[");
				for(Vector<PointDataRGB> vp:imageData) {
					out.append("[");
					for(PointDataRGB p : vp) {
						out.append("["+String.valueOf(p.R)+","
								+String.valueOf(p.G)+","+
								String.valueOf(p.B)+"]"+(vp.indexOf(p)<vp.size()-1?",":""));
					}
					out.append("]" + (imageData.indexOf(vp)<imageData.size()-1?",":""));
				}
			}
			//append data end
			out.append("]\n" + 
			"}");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
