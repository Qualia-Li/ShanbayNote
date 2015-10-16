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

	ArrayList<String> allUrlList = new ArrayList<String>();// ���е���ҳurl����Ҫ����Ч��ȥ�ؿ��Կ���HashSet
	ArrayList<String> uncrawledUrlList = new ArrayList<String>();// δ��������ҳurl
	HashMap<String, Integer> depth = new HashMap<String, Integer>();// ������ҳ��url���
	ArrayList<StringBuffer> responseList = new ArrayList<StringBuffer>();
	int crawDepth = 1; // �������
	int threadNumber = 10; // �߳�����
	int waitingThreadCount = 0; // ��ʾ�ж��ٸ��̴߳���wait״̬
	public static final Object signal = new Object(); // �̼߳�ͨ�ű���

	public static void main(String[] args) throws IOException {
		final WebCrawler wc = new WebCrawler();
		Word.readWordList();
		for(String word : Word.wordList){
			wc.addUrl(Word.wordUrl+word,1);
		}
		
		long start = System.currentTimeMillis();
		System.out.println("��ʼ���桭��");
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
			
			// ���������̶߳����ڵȴ�״̬������url���Ѿ�����
			if (wc.uncrawledUrlList.isEmpty() && Thread.activeCount() == 1
					|| wc.waitingThreadCount == wc.threadNumber) {
				long end = System.currentTimeMillis();
				System.out.println("�ܹ�ץȡ��" + wc.allUrlList.size() + "����ҳ��");
				System.out.println("�ܹ���ʱ" + (end - start) / 1000 + "�롣");
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
							synchronized (signal) { // ------------------��2��
								try {
									waitingThreadCount++;
									System.out.println("��ǰ��"
											+ waitingThreadCount + "���߳��ڵȴ���");
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

	// ����ҳsUrl, put the result into responseList
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
			StringBuffer sb = new StringBuffer();// sbΪ��������ҳ����
			String rLine = null;
			while ((rLine = bReader.readLine()) != null) {
				sb.append(rLine);
				sb.append("/n");
			}
			int d = depth.get(sUrl);
			System.out.println(sUrl + "ץȡ�ɹ������߳�"+ Thread.currentThread().getName()+"��");
			synchronized (responseList) {
				responseList.add(sb);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ��context��ȡurl��ַ����Ҫ�������ʱʹ��
	public void parseContext(String context, int dep) {
		String regex = "<a href.*?/a>";
		// String regex = "<title>.*?</title>";
		String s = "fdfd<title>�� ��</title><a href=\"http://www.iteye.com/blogs/tag/Google\">Google</a>fdfd<>";
		// String regex ="http://.*?>";
		Pattern pt = Pattern.compile(regex);
		Matcher mt = pt.matcher(context);
		while (mt.find()) {
			// System.out.println(mt.group());
			Matcher myurl = Pattern.compile("href=\".*?\"").matcher(mt.group());
			while (myurl.find()) {
				String str = myurl.group().replaceAll("href=\"|\"", "");
				// System.out.println("��ַ��:"+ str);
				if (str.contains("http:")) { // ȡ��һЩ����url�ĵ�ַ
					if (!allUrlList.contains(str)) {
						addUrl(str, dep);// ����һ���µ�url
						if (waitingThreadCount > 0) { // ����еȴ����̣߳�����
							synchronized (signal) { // ---------------------��2��
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