import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class Login {
	static ArrayList<String> uncrawledUrlList=new ArrayList<String>();
	static int fileCount=0;
	static String folderName;
	
	
	public static void main(String[] args) throws IOException {
		Scanner in = new Scanner(System.in);
		System.out.println("Please input the url file name:");
		String filename=in.nextLine();
		uncrawledUrlList=Tools.readList(filename);
		folderName=filename.substring(0, filename.lastIndexOf("."));
		File folder=new File(folderName);
		folder.mkdirs();
		
		Login login=new Login();
		for(int i=0;i<1;i++){
			NoteThread noteThread=login.new NoteThread();
			new Thread(noteThread, "Thread-"+i).start();
		}
		
		if (uncrawledUrlList.isEmpty())
			System.exit(1);
	}
	
	class NoteThread implements Runnable {
		public void run(){
			CloseableHttpClient httpclient = new DefaultHttpClient();
			login(httpclient);
			String url;
			while(true){
				url=getUrl();
				if(url==null){
					Thread.currentThread().interrupt();
					System.out.println(Thread.currentThread().getName()+"has finished.");
				}
				StringBuffer sb=crawl(httpclient,url);
				saveToFile(sb);
			}
		}
	}
	
	synchronized void saveToFile(StringBuffer sb){
		fileCount++;
		String filename=String.format("%s/%4d.txt", folderName, fileCount);
		filename=filename.replaceAll(" ", "0");
		Tools.saveStringToFile(sb.toString(),filename);
		System.out.println(""+filename+" finished");
	}
	
	synchronized String getUrl(){
		if(!uncrawledUrlList.isEmpty()){
			String url=uncrawledUrlList.get(0);
			uncrawledUrlList.remove(0);
			return url;
		}
		else return null;
	}
	
	void addUrl(String url){
		uncrawledUrlList.add(url);
	}


	@SuppressWarnings("deprecation")
	public static void login(CloseableHttpClient httpclient) {

		// Get Veripic
		try{
			HttpGet httpGet = new HttpGet("http://shanbay.com/accounts/login/");
//			System.out.println("请求: " + httpGet.getRequestLine());
			CloseableHttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
//			System.out.println("响应：" + response.getStatusLine());
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
			StringBuffer sb = Tools.openBufferedReader(reader);
			
			String token=getToken(sb);
	
			// Post Login Information
			HttpPost httpPost = new HttpPost("http://shanbay.com/accounts/login/");
			List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
			//Post Parameters
			nvps.add(new BasicNameValuePair("csrfmiddlewaretoken",token)); 
			nvps.add(new BasicNameValuePair("username", "PENE")); 
			nvps.add(new BasicNameValuePair("password", "bill1995"));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
	
//			System.out.println("请求: " + httpPost.getRequestLine());
			CloseableHttpResponse postResponse = httpclient.execute(httpPost);
			entity=postResponse.getEntity();
//			System.out.println("响应：" + response.getStatusLine());
			reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));

			// Close
			response.close();
			postResponse.close();
			if (entity != null) {
				entity.consumeContent();
			}
		}
		catch (Exception e){
			System.out.println("Error in Login:");
			e.printStackTrace();
		}
	}

	public static String getToken(StringBuffer sb){
		//TODO
		String token="";
		String content=sb.toString();
		String regex="\\w*?(?=' />)";
		Pattern pa = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher ma = pa.matcher(content);
		if (ma.find()) {
			token=ma.group(0);
		}
		System.out.println("Token:"+token);
		return token;
	}
	
	static StringBuffer crawl(CloseableHttpClient httpclient, String url) {
		try{
			HttpGet httpGet = new HttpGet(url);
	
//			System.out.println("请求：" + httpGet.getRequestLine());
			CloseableHttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
//			System.out.println("响应：" + response.getStatusLine());
	
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					entity.getContent(), "UTF-8"));
			StringBuffer sb = Tools.openBufferedReader(reader);
			return sb;
		}
		catch (Exception e){
			System.out.println("Error in crawl.");
			e.printStackTrace();
			return null;
		}
	}

}
