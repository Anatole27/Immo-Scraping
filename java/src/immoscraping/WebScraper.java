package immoscraping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Vector;

public abstract class WebScraper extends Thread {

	Vector<String> regexpList;
	Database database;

	public WebScraper(Database database) {
		this.database = database;
	}

	public static String getSource(String urlString) throws Exception {

		URLConnection connection = new URL(urlString).openConnection();
		connection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		connection.connect();

		BufferedReader r = new BufferedReader(
				new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	abstract public void run();
}
