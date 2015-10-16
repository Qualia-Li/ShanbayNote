import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NoteList {
	
	ArrayList<Note>noteList=new ArrayList<Note>();
//	String word;
//	String wordId;
	int fileId;
	
	public static void main(String args[]) throws IOException{
		String filename = null;
		StringBuffer content;
		for (int i = 1; i <= 3067; i++) {
//			System.out.println(i);
			filename = String.format("NoteUrl/%4d.txt", i);
			filename = filename.replaceAll(" ", "0");
			content = Tools.openFile(filename);
			new NoteList(content,i);
		}
	}
	
	@SuppressWarnings("rawtypes")
	class Note implements Comparable{
		
		String username;
		String userid;
		String nickname;
		int likes;
		int dislikes;
		int pureLikes;
		String content;
		
		Note(String noteContent){
			JSONObject jsonContent = JSONObject.fromObject(noteContent);
			username=jsonContent.getString("username");
			userid=jsonContent.getString("userid");
			likes=jsonContent.getInt("likes");
			dislikes=jsonContent.getInt("unlikes");
			pureLikes=likes-dislikes;
			nickname=jsonContent.getString("nickname");
			content=jsonContent.getString("content");
			content=content.replaceAll(",", "，");
			content=content.replaceAll("\n", "，");
		}

		@Override
		public int compareTo(Object arg0) {
			Note that = (Note) arg0;
			return that.pureLikes-pureLikes;
		}
	}
	
	@SuppressWarnings("unchecked")
	NoteList(StringBuffer content, int fileId) throws IOException{
		this.fileId=fileId;
		String strContent=content.toString();
		JSONObject jsonContent=JSONObject.fromObject(strContent);
		JSONArray jsonDataArray=jsonContent.getJSONArray("data");
		String note;
		for(int i=0;i<jsonDataArray.size();i++){
			note=jsonDataArray.getString(i);
			noteList.add(new Note(note));
		}
		Collections.sort(noteList);
		
		File file = new File("notes.csv");
		OutputStream os = new FileOutputStream(file, true);
		String write = String.format("\n%d,", fileId);
		os.write(write.getBytes());
		for(int i=0;i<5;i++){
			try{
				write=String.format("%s,%d,",noteList.get(i).content,noteList.get(i).pureLikes);
				write=write.replaceAll("\n", "");
				os.write(write.getBytes());
			}
			catch (Exception e){}
		}
		System.out.println(fileId + "已添加信息。");
		
		//TODO: Output the topnotes
	}
}
