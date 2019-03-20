package immoscraping.scrapers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import immoscraping.Ad;
import immoscraping.Database;

public class PapScraper extends WebScraper {

	private static final String DOMAIN = "https://www.pap.fr";
	private static final String BASE_SEARCH_URL = "https://www.pap.fr/annonce/vente-appartement-maison-toulouse-31-g43612-";
	private static final String AD_HREF_REGEX = "(?<=<a class=\"item-title\" href=\")\\/annonces\\/.*(?=\"\\s*name)";
	private static final String ENERGY_GRADE_REGEX = "(?<=kvclasse_energie=)[A-Z]";
	private static final String SURFACE_REGEX = "(?<=kvsurface_max=)[0-9]*";
	private static final String PRICE_REGEX = "(?<=kvprix_max=)[0-9]*";
	private static final String LAT_REGEX = "(?<=data-mappy=\"\\{&quot;center&quot;:\\[&quot;)[0-9]*\\.[0-9]*";
	private static final String LON_REGEX = "(?<=quot;,&quot;)[0-9]*\\.[0-9]*(?=&quot;])";
	private static final String LAST_DATE_REGEX = "[0-9]* [a-zéû]* [0-9]{4}";
	private static final String DESC_REGEX = "(?<=<!-- Description \\+ métro -->\\n\\t\\t\\t\\t\\n<div class=\"margin-bottom-30\">\\n\\t<p>)(.*\\n)*.*(?=<\\/p>\\n\\t<p><\\/p>\\n<\\/div>)";
	private static final String TYPE_REGEX = "(?<=kvtypebien=)[A-z]*";
	private static final String POSTAL_CODE_REGEX = "(?<=\\()[0-9]{5}(?=\\))";
	private static final String ROOMS_REGEX = "(?<=kvnb_pieces=)[0-9]*";

	private static final long SLEEP_DURATION = 10;
	private Pattern patternAd;
	WebDriver driver;
	private Date sinceDate;

	public PapScraper(Database database, Date sinceDate) {
		super(database);
		patternAd = Pattern.compile(AD_HREF_REGEX);
		this.sinceDate = sinceDate;
	}

	@Override
	public void run() {
		ChromeOptions chromeOptions = new ChromeOptions();
		// chromeOptions.addArguments("--headless");
		System.setProperty("webdriver.chrome.driver", "/usr/lib/chromium-browser/chromedriver");
		driver = new ChromeDriver(chromeOptions);

		int iPage = 1;

		Ad ad = new Ad();
		boolean atLeastOneAd;
		do {
			// Load page
			String url = BASE_SEARCH_URL + iPage;
			System.out.println(url);
			driver.get(url);
			String sourceCode = driver.getPageSource();

			// Load each ad
			Matcher matcherAd = patternAd.matcher(sourceCode);
			atLeastOneAd = false;
			while (matcherAd.find()) {

				atLeastOneAd = true;

				String adUrl = DOMAIN + matcherAd.group();
				// System.out.println(adUrl);
				try {
					ad = getAd(adUrl);
				} catch (Exception e) {
					System.err.println("Error on ad URL :");
					System.err.println(adUrl);
					System.err.println(e.toString());
				}
				database.add(ad);

				// Give back focus
				try {
					Thread.sleep(SLEEP_DURATION);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// End looking for ads if the since date is reached
				System.out.printf("Ad update: %s, first date: %s\n", ad.lastPubDate, ad.firstPubDate);
				// System.out.println("Took " + (System.currentTimeMillis() - time) / 1000 +
				// "sec");
				if (sinceDate.after(ad.lastPubDate)) {
					break;
				}
			}
			iPage++;
		} while (sinceDate.before(ad.lastPubDate) && atLeastOneAd);

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
		ad.type = Character.toUpperCase(ad.type.charAt(0)) + ad.type.substring(1);

		// Get Energy grade
		String elmt = getElement(ENERGY_GRADE_REGEX, sourceCode);
		if (!elmt.equals("")) {
			ad.energyGrade = getElement(ENERGY_GRADE_REGEX, sourceCode).charAt(0);
		} else {
			ad.energyGrade = 'N';
		}

		// Get GES
		ad.gesGrade = 'N';

		// Get latitude / longitude
		try {
			ad.latLon[0] = Double.parseDouble(getElement(LAT_REGEX, sourceCode));
			ad.latLon[1] = Double.parseDouble(getElement(LON_REGEX, sourceCode));
		} catch (Exception e) {
			System.err.printf("No lat/lon found for %s\n", ad.url);
			ad.latLon[0] = 43.604401;
			ad.latLon[1] = 1.44295;
		}

		// Get price
		ad.price = Double.parseDouble(getElement(PRICE_REGEX, sourceCode));

		// Get surface
		try {
			ad.surface = Double.parseDouble(getElement(SURFACE_REGEX, sourceCode));
		} catch (Exception e) {
			ad.surface = 0;
		}

		// Get 1st pub date
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE);
		try {
			ad.firstPubDate = dateFormat.parse(getElement(LAST_DATE_REGEX, sourceCode));
		} catch (ParseException e) {
			ad.firstPubDate.setTime(System.currentTimeMillis());
			System.err.println("Date not found in ad " + adUrl);
		}

		// Get last pub date
		ad.lastPubDate = ad.firstPubDate;

		// Get description
		ad.description = getElement(DESC_REGEX, sourceCode);
		ad.description = ad.description.replace("<br />", "\n");

		// Ad is not postprocessed yet
		ad.isPostprocessed = false;

		// Get is pro
		ad.isPro = false;

		// Get postal code
		ad.zipcode = getElement(POSTAL_CODE_REGEX, sourceCode);

		// Get number of rooms
		ad.rooms = getElement(ROOMS_REGEX, sourceCode);

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
