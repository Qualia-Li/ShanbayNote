import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Word {
	static final String wordUrl = "http://www.shanbay.com/api/v1/bdc/search/?word=";
	static ArrayList<String> wordList = new ArrayList<String>();

	String word;
	String wordId;// contend_id | object_id |
	String learningId;

	String pronunciation;// pronunciation
	String audioUrl;// audio
	String cnDefinition;// cn_definition/defn
	// EnDefinitions enDefinitions=new EnDefinitions();//en_definition
	String enDefinition;

	static int invalidId=1;
	
	Word(StringBuffer content) throws IOException {
		// parse

		String strContent = content.toString();
		JSONObject jsonContent = JSONObject.fromObject(strContent);
		JSONObject jsonData = jsonContent.getJSONObject("data");
		word = String.format("INVALID%4d",invalidId);
		word=word.replaceAll(" ","0");
		try {
			word = jsonData.getString("audio_name");
			wordId = jsonData.getString("content_id");
			JSONObject jsonCnDefinition = jsonData
					.getJSONObject("cn_definition");
			cnDefinition = jsonCnDefinition.getString("defn");
			cnDefinition = cnDefinition.replaceAll(",", "，");
			cnDefinition = cnDefinition.replaceAll("\n", "，");
			pronunciation = jsonData.getString("pron");
			audioUrl = jsonData.getString("audio");
			// learningId=jsonData.getString("learning_id");
			enDefinition = jsonData.getString("en_definitions");
			enDefinition = enDefinition.replaceAll(",", ".");
		} catch (Exception e) {
			e.printStackTrace();
			Tools.saveStringToFile(strContent, word + ".json");
			invalidId++;
			// System.out.println(word+"已保存json文件。");
		}
		// JSONObject
		// jsonEnDefinitions=jsonData.getJSONObject("en_definitions");
		// if(jsonEnDefinitions.containsKey("v")){
		// JSONArray vDefns = jsonEnDefinitions.getJSONArray("v");
		// }
		synchronized (this) {//Seems needless to sync
			String write = String.format("\n%s,%s,%s,%s,%s,%s", word, wordId,
					cnDefinition, enDefinition, audioUrl, pronunciation);
			write=write.replaceAll("\n","");
			File file = new File("result.csv");
			OutputStream os = new FileOutputStream(file, true);
			os.write(write.getBytes());
			System.out.println(word + "已添加信息。");
		}
	}

	@SuppressWarnings("resource")
	static public void readWordList() throws IOException {
		wordList.add("apple");
		wordList.add("banana");
		// System.out.println("Please input the ID file name");
		// Scanner in=new Scanner(System.in);
		// String fileName=in.next();
		// FileReader fr = new FileReader(fileName+".txt");
		// BufferedReader br = new BufferedReader(fr);
		// String record;
		// while ((record = br.readLine()) != null) {
		// wordList.add(record);
		// }
		// System.out.println("已读取"+wordList.size()+"个单词信息。");
	}

	public static void main(String args[]) throws IOException {
		String filename = null;
		StringBuffer content;
		for (int i = 1; i <= 3069; i++) {
			System.out.println(i);
			filename = String.format("wordUrl/%4d.txt", i);
			filename = filename.replaceAll(" ", "0");
			content = Tools.openFile(filename);
			new Word(content);
		}

	}

	class EnDefinitions {

		ArrayList<EnDefinition> definitionSetList;

		class EnDefinition {
			String pos;
			ArrayList<String> definitionList;
		}
	}

}
