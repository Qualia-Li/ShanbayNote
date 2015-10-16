import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler {

	ArrayList<String> allUrlList = new ArrayList<String>();// 所有的网页url，需要更高效的去重可以考虑HashSet
	ArrayList<String> uncrawledUrlList = new ArrayList<String>();// 未爬过的网页url
	HashMap<String, Integer> depth = new HashMap<String, Integer>();// 所有网页的url深度
	ArrayList<StringBuffer> responseList = new ArrayList<StringBuffer>();
	int crawDepth = 1; // 爬虫深度
	int threadNumber = 10; // 线程数量
	int waitingThreadCount = 0; // 表示有多少个线程处于wait状态
	public static final Object signal = new Object(); // 线程间通信变量

	public static void main(String[] args) throws IOException {
		final WebCrawler wc = new WebCrawler();
		Word.readWordList();
		for(String word : Word.wordList){
			wc.addUrl(Word.wordUrl+word,1);
		}
		
		long start = System.currentTimeMillis();
		System.out.println("开始爬虫……");
		wc.begin();

		while (true) {
			StringBuffer response=null;
			synchronized (wc.responseList){
				if(!wc.responseList.isEmpty()){
					 response=wc.responseList.get(0);
					wc.responseList.remove(0);
				}
			}
			if(response!=null){//got a response
				new Word(response);
			}
			
			// 所有爬虫线程都处于等待状态，而且url池已经空了
			if (wc.uncrawledUrlList.isEmpty() && Thread.activeCount() == 1
					|| wc.waitingThreadCount == wc.threadNumber) {
				long end = System.currentTimeMillis();
				System.out.println("总共抓取了" + wc.allUrlList.size() + "个网页。");
				System.out.println("总共耗时" + (end - start) / 1000 + "秒。");
				System.exit(1);
			}
		}
	}

	// Thread manager
	private void begin() {
		for (int i = 0; i < threadNumber; i++) {
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						String tmp = getAUrl();
						if (tmp != null) {
							crawler(tmp);
						} else {
							synchronized (signal) { // ------------------（2）
								try {
									waitingThreadCount++;
									System.out.println("当前有"
											+ waitingThreadCount + "个线程在等待。");
									signal.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}, "thread-" + i).start();
		}
	}

	// Get an URL from URL Set, a sync method
	public synchronized String getAUrl() {
		if (uncrawledUrlList.isEmpty())
			return null;
		String tmpAUrl;
		tmpAUrl = uncrawledUrlList.get(0);
		uncrawledUrlList.remove(0);
		return tmpAUrl;
	}

	// Add an URL to URL pool
	public synchronized void addUrl(String url, int d) {
		uncrawledUrlList.add(url);
		allUrlList.add(url);
		depth.put(url, d);
	}

	// 爬网页sUrl, put the result into responseList
	public void crawler(String sUrl) {
		URL url;
		try {
			url = new URL(sUrl);
			URLConnection urlconnection = url.openConnection();
			urlconnection.addRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			InputStream is = url.openStream();
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					is));
			StringBuffer sb = new StringBuffer();// sb为爬到的网页内容
			String rLine = null;
			while ((rLine = bReader.readLine()) != null) {
				sb.append(rLine);
				sb.append("/n");
			}
			int d = depth.get(sUrl);
			System.out.println(sUrl + "抓取成功，由线程"+ Thread.currentThread().getName()+"。");
			synchronized (responseList) {
				responseList.add(sb);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 从context提取url地址，需要深度搜索时使用
	public void parseContext(String context, int dep) {
		String regex = "<a href.*?/a>";
		// String regex = "<title>.*?</title>";
		String s = "fdfd<title>我 是</title><a href=\"http://www.iteye.com/blogs/tag/Google\">Google</a>fdfd<>";
		// String regex ="http://.*?>";
		Pattern pt = Pattern.compile(regex);
		Matcher mt = pt.matcher(context);
		while (mt.find()) {
			// System.out.println(mt.group());
			Matcher myurl = Pattern.compile("href=\".*?\"").matcher(mt.group());
			while (myurl.find()) {
				String str = myurl.group().replaceAll("href=\"|\"", "");
				// System.out.println("网址是:"+ str);
				if (str.contains("http:")) { // 取出一些不是url的地址
					if (!allUrlList.contains(str)) {
						addUrl(str, dep);// 加入一个新的url
						if (waitingThreadCount > 0) { // 如果有等待的线程，则唤醒
							synchronized (signal) { // ---------------------（2）
								waitingThreadCount--;
								signal.notify();
							}
						}

					}
				}
			}
		}
	}
}