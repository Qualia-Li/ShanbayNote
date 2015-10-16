import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Test {
	public static void main(String[] args) throws FileNotFoundException {
		
		String testString=openFile("test.txt");

		JSONObject jsonObject=JSONObject.fromObject(testString);
		String data=jsonObject.getString("data");
		JSONObject jsonData=JSONObject.fromObject(data);
		String id=jsonData.getString("content_id");
		System.out.println(id);
		//JSONArray jsonArray = JSONArray.fromObject(JsonContext);
		//int size = jsonArray.size();
		//System.out.println("Size: " + size);
//		for (int i = 0; i < size; i++) {
//			JSONObject jsonObject = jsonArray.getJSONObject(i);
//			System.out.println("[" + i + "]name=" + jsonObject.get("name"));
//			System.out.println("[" + i + "]package_name="
//					+ jsonObject.get("package_name"));
//			System.out.println("[" + i + "]check_version="
//					+ jsonObject.get("check_version"));
//		}
	}
	
	public static String openFile(String fileName) {
		try {
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(fileName)), "UTF-8"));
			String content = "";
			String temp;
	
			while ((temp = bis.readLine()) != null) {
				content += temp + "\n";
			}
			bis.close();
			return content;
		} catch (Exception e) {
			System.err.println(e);
			return "";
		}
	}
}


