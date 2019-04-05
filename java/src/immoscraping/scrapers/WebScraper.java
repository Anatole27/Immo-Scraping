package immoscraping.scrapers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import immoscraping.Ad;
import immoscraping.Database;

public abstract class WebScraper extends Thread {

	Vector<String> regexpList;
	Database database;
	protected WebDriver driver;
	private Pattern patternAd;
	private Date sinceDate;
	private String domain;
	private String baseSearchUrl;

	private static final long SLEEP_DURATION = 10;

	public WebScraper(Database database, Date sinceDate, String domain, String baseSearchUrl, String adHrefRegex) {
		this.database = database;
		patternAd = Pattern.compile(adHrefRegex);
		this.sinceDate = sinceDate;
		this.domain = domain;
		this.baseSearchUrl = baseSearchUrl;
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

	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YY HH:mm");

	public void run() {
		System.out.format("\nRunning scrape on %s back to %s\n", getSiteName(), sdf.format(sinceDate));
		ChromeOptions chromeOptions = new ChromeOptions();
//		chromeOptions.addArguments("--headless");
		System.setProperty("webdriver.chrome.driver", "/usr/lib/chromium-browser/chromedriver");
		driver = new ChromeDriver(chromeOptions);

		int iPage = 1;

		Ad ad = new Ad();
		boolean atLeastOneAd = false;
		do {
			String url = "";
			try {
				// Load page
				url = baseSearchUrl + iPage;
				driver.get(url);
				String sourceCode = driver.getPageSource();

				// Load each ad
				Matcher matcherAd = patternAd.matcher(sourceCode);
				atLeastOneAd = false;
				while (matcherAd.find()) {
					atLeastOneAd = true;
					String adUrl = domain + matcherAd.group();
					try {
						ad = getAd(adUrl);
						addToDatabase(ad);
					} catch (Exception e) {
						System.err.println("Error on ad URL :");
						System.err.println(adUrl);
						e.printStackTrace();
					}

					// Give back focus
					try {
						Thread.sleep(SLEEP_DURATION);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// End looking for ads if the since date is reached
					if (sinceDate.after(ad.lastPubDate)) {
						break;
					}
				}
				iPage++;

			} catch (Exception e) {
				System.err.println("Error on page URL :");
				System.err.println(url);
				System.err.println(e.toString());
			}
		} while (sinceDate.before(ad.lastPubDate) && atLeastOneAd);

		close();

	}

	protected abstract String getSiteName();

	protected abstract void addToDatabase(Ad ad);

	protected abstract Ad getAd(String adUrl);

	public void close() {
		driver.quit();
	}

	protected String getElement(String regex, String sourceCode) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sourceCode);
		if (m.find()) {
			return m.group();
		} else {
			return "";
		}
	}
}
