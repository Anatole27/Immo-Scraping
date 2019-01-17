package immoscraping;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class LbcScraper extends WebScraper {

	private static final String DOMAIN = "https://www.leboncoin.fr";
	private static final String BASE_SEARCH_URL = "https://www.leboncoin.fr/recherche/?category=9&regions=16&location=Toulouse_31400,31320,31650,Toulouse_31500&real_estate_type=2,1&page=";
	private static final String AD_HREF_REGEX = "(?<=clearfix trackable\" rel=\"nofollow\" href=\")/ventes_immobilieres/[0-9]*.htm/";
	private static final String ENERGY_GRADE_REGEX = "(?<=\"key_label\":\"Classe énergie\",\"value_label\":\")[A-Z]";
	private static final String GES_REGEX = "(?<=\"key_label\":\"GES\",\"value_label\":\")[A-Z]";
	private static final String SURFACE_REGEX = "(?<=\"Surface\",\"value_label\":\")[0-9]*";
	private static final String PRICE_REGEX = "(?<=\"price\":\\[)[0-9]*";
	private static final String LAT_REGEX = "(?<=\"lat\":)[0-9]*\\.[0-9]*";
	private static final String LON_REGEX = "(?<=\"lng\":)[0-9]*\\.[0-9]*";
	private static final String FIRST_DATE_REGEX = "(?<=first_publication_date\":\")[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}";
	private static final String LAST_DATE_REGEX = "(?<=index_date\":\")[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}";
	private static final String DESC_REGEX = "(?<=<span class=\"_2wB1z\" data-reactid=\"[0-9]{1,3}\">).*(?=</span>)";
	private static final String TYPE_REGEX = "(?<=Type de bien</div><div class=\"_3Jxf3\" data-reactid=\"[0-9]{3}\">)[A-z]*";
	private static final String IS_PRO_REGEX = "N° SIREN";

	private static final long SLEEP_DURATION = 10;
	private Pattern patternAd;
	WebDriver driver;
	private Date sinceDate;

	public LbcScraper(Database database, Date sinceDate) {
		super(database);
		patternAd = Pattern.compile(AD_HREF_REGEX);
		this.sinceDate = sinceDate;
	}

	@Override
	public void run() {
		ChromeOptions chromeOptions = new ChromeOptions();
		System.setProperty("webdriver.chrome.driver", "/usr/lib/chromium-browser/chromedriver");
//		chromeOptions.addArguments("--headless");
		driver = new ChromeDriver(chromeOptions);

		int iPage = 1;

		Ad ad = new Ad();
		do {
			// Load page
			String url = BASE_SEARCH_URL + iPage;
			System.out.println(url);
			driver.get(url);
			String sourceCode = driver.getPageSource();

			// Load each ad
			Matcher matcherAd = patternAd.matcher(sourceCode);
			while (matcherAd.find()) {
				long time = System.currentTimeMillis();
				String adUrl = DOMAIN + matcherAd.group();
//				System.out.println(adUrl);
				ad = getAd(adUrl);
				database.add(ad);

				// Give back focus
				try {
					Thread.sleep(SLEEP_DURATION);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// End looking for ads if the since date is reached
				System.out.println("Ad date : " + ad.lastPubDate);
//				System.out.println("Took " + (System.currentTimeMillis() - time) / 1000 + "sec");
				if (sinceDate.after(ad.lastPubDate)) {
					break;
				}
			}
			iPage++;
		} while (sinceDate.before(ad.lastPubDate));

		close();

	}

	public void close() {
		driver.quit();
	}

	protected Ad getAd(String adUrl) {

		driver.get(adUrl);
		String sourceCode = driver.getPageSource();

		Ad ad = new Ad();

		// Set url
		ad.url = adUrl;

		// Get type
		ad.type = getElement(TYPE_REGEX, sourceCode);

		// Get Energy grade
		String elmt = getElement(ENERGY_GRADE_REGEX, sourceCode);
		if (!elmt.equals("")) {
			ad.energyGrade = getElement(ENERGY_GRADE_REGEX, sourceCode).charAt(0);
		} else {
			ad.energyGrade = 'N';
		}

		// Get GES
		elmt = getElement(ENERGY_GRADE_REGEX, sourceCode);
		if (!elmt.equals("")) {
			ad.gesGrade = getElement(GES_REGEX, sourceCode).charAt(0);
		} else {
			ad.gesGrade = 'N';
		}

		// Get latitude / longitude
		ad.latLon[0] = Double.parseDouble(getElement(LAT_REGEX, sourceCode));
		ad.latLon[1] = Double.parseDouble(getElement(LON_REGEX, sourceCode));

		// Get price
		ad.price = Double.parseDouble(getElement(PRICE_REGEX, sourceCode));

		// Get surface
		try {
			ad.surface = Double.parseDouble(getElement(SURFACE_REGEX, sourceCode));
		} catch (Exception e) {
			ad.surface = 0;
		}

		// Get 1st pub date
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			ad.firstPubDate = dateFormat.parse(getElement(FIRST_DATE_REGEX, sourceCode));
		} catch (ParseException e) {
			ad.firstPubDate.setTime(System.currentTimeMillis());
			System.err.println("Date not found in ad " + adUrl);
		}

		// Get last pub date
		try {
			ad.lastPubDate = dateFormat.parse(getElement(LAST_DATE_REGEX, sourceCode));
		} catch (ParseException e) {
			ad.lastPubDate.setTime(System.currentTimeMillis());
			System.err.println("Date not found in ad " + adUrl);
		}

		// Get description
		ad.description = getElement(DESC_REGEX, sourceCode);
		ad.description = ad.description.replace("<br />", "\n");

		// Ad is not postprocessed yet
		ad.isPostprocessed = false;

		// Get is pro
		String proString = getElement(IS_PRO_REGEX, sourceCode);
		if (proString.length() > 0) {
			ad.isPro = true;
		}

		return ad;
	}

	private String getElement(String regex, String sourceCode) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sourceCode);
		if (m.find()) {
			return m.group();
		} else {
			return "";
		}
	}

}
