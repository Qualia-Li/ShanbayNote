import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Tools {
	
	// ��ӡBufferedReader
	public static void printBufferedReader(BufferedReader bis) {
		try {
			String szTemp;
			while ((szTemp = bis.readLine()) != null) {
				System.out.println(szTemp);
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	// ����String
	public static void saveStringToFile(String string, String filename) {
		if(filename==null||filename=="")
			filename="string.html";
		try {
			FileWriter fw = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(string);
			bw.close();
			fw.close();
//			System.out.println("�ѱ����ļ���");
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	// ��BufferedReaderת�����ַ���
	public static StringBuffer openBufferedReader(BufferedReader bis) {
		try {
			StringBuffer content = new StringBuffer();
			String temp;
			while ((temp = bis.readLine()) != null) {
				content.append(temp + "\n");
			}
			bis.close();
			return content;
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}
		
	public static StringBuffer openFile(String fileName) {
		try {
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(fileName)), "UTF-8"));
			StringBuffer content = new StringBuffer();
			String temp;

			while ((temp = bis.readLine()) != null) {
				content.append(temp + "\n");
			}
			bis.close();
			return content;
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}
	
	static public ArrayList<String> readList(String fileName) throws IOException{
		ArrayList<String> list=new ArrayList<String>();
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		String record;
		while ((record = br.readLine()) != null) {
			list.add(record);
		}
		System.out.println("�Ѷ�ȡ"+list.size()+"����Ŀ��");
		return list;
	}
}
