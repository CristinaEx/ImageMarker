package imagemarker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 记录各个物体的label->int value
 * @author ene
 *
 */
public class SthNumber {
	public HashMap<String,Integer> map = new HashMap<String,Integer>();
	public static String FILENAME = "sthnumber.json";
	private File file = null;
	SthNumber(String path){
		this.file = new File(path + "\\" + SthNumber.FILENAME);
		if(!this.file.exists())
			try {
				OutputStream out=new FileOutputStream(this.file);
				out.write(("{\n" +
				"	\"sthToNumber\":{\"背景\":-1}\n" + 
						"}").getBytes());
				out.close();
				file.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
	    try {
			JSONObject objectMain = new JSONObject(FileUtils.readFileToString(file, "UTF-8"));
			objectMain = objectMain.getJSONObject("sthToNumber");
			java.util.Iterator<String> it = objectMain.keys();  
			while (it.hasNext()) {
				String key = String.valueOf(it.next());  
				Integer value = (int) objectMain.get(key);  
				this.map.put(key, value); 
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void reflashData() {
		if(this.file == null)return;
		JSONObject jsonObject = new JSONObject(this.map);
		try {
			OutputStreamWriter out=new OutputStreamWriter(new FileOutputStream(this.file), "utf-8");
			out.append("{\n" +
			"	\"sthToNumber\":" + jsonObject.toString() + "\n" + 
					"}");
			out.close();
			file.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
	}
}
