package immoscraping.scrapers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import immoscraping.Ad;
import immoscraping.Database;

public class ParuVenduScraper extends WebScraper {

	private static final String DOMAIN = "https://www.paruvendu.fr";
	private static final String BASE_SEARCH_URL = "https://www.paruvendu.fr/immobilier/annonceimmofo/liste/listeAnnonces?nbp=0&tt=1&tbApp=1&tbDup=1&tbChb=1&tbLof=1&tbAtl=1&tbPla=1&tbMai=1&tbVil=1&tbCha=1&tbPro=1&tbHot=1&tbMou=1&tbFer=1&at=1&nbp0=99&sur0=90&sur1=200&px0=300000&px1=500000&pa=FR&ddlFiltres=nofilter&codeINSEE=PA055,PA056,,&p=";
	private static final String AD_HREF_REGEX = "(?<=<a class=\"voirann\" href=\")\\/immobilier\\/.*(?=\" title)";
	private static final String ENERGY_GRADE_REGEX = "(?<=communfo/img/DPE/ce_)[a-z](?=\\.png)";
	private static final String GES_REGEX = "(?<=communfo\\/img\\/DPE\\/ges_)[a-z](?=\\.png)";
	private static final String SURFACE_REGEX = "(?<='surfmax', \\[')[0-9]*";
	private static final String PRICE_REGEX = "(?<=prixmax', \\[')[0-9]*";
	private static final String FIRST_DATE_REGEX = "le [0-9]{1,2}\\/[0-9]{1,2}\\/[0-9]{4} à [0-9]{1,2}:[0-9]{2}";
	private static final String HOUSE_REGEX = "(villa|maison)";
	private static final String IS_PRO_REGEX = "Annonce de particulier";
	private static final String POSTAL_CODE_REGEX = "(?<=\\()[0-9]{5}(?=\\))";
	private static final String ROOMS_REGEX = "(?<=<strong>Nombre de pièces :<\\/strong>\\n)[0-9]*";

	public ParuVenduScraper(Database database, Date sinceDate) {
		super(database, sinceDate, DOMAIN, BASE_SEARCH_URL, AD_HREF_REGEX);
	}

	@Override
	protected Ad getAd(String adUrl) {

		driver.get(adUrl);
		String sourceCode = driver.getPageSource();

		Ad ad = new Ad();

		// Set url
		ad.url = adUrl;

		// Get type
		if (getElement(HOUSE_REGEX, ad.url).length() > 0) {
			ad.type = "Maison";
		} else {
			ad.type = "Appartement";
		}

		// Get Energy grade
		String elmt = getElement(ENERGY_GRADE_REGEX, sourceCode);
		if (!elmt.equals("")) {
			ad.energyGrade = Character.toUpperCase(elmt.charAt(0));
		} else {
			ad.energyGrade = 'N';
		}

		// Get GES
		elmt = getElement(GES_REGEX, sourceCode);
		if (!elmt.equals("")) {
			ad.gesGrade = Character.toUpperCase(elmt.charAt(0));
		} else {
			ad.gesGrade = 'N';
		}

		// Get latitude / longitude
		// Information not available on the site
		ad.latLon[0] = 43.604401;
		ad.latLon[1] = 1.44295;

		// Get price
		ad.price = Double.parseDouble(getElement(PRICE_REGEX, sourceCode));

		// Get surface
		try {
			ad.surface = Double.parseDouble(getElement(SURFACE_REGEX, sourceCode));
		} catch (Exception e) {
			ad.surface = 0;
		}

		// Get 1st pub date
		SimpleDateFormat dateFormat = new SimpleDateFormat("'le' dd/MM/yyyy à HH:mm");
		try {
			ad.firstPubDate = dateFormat.parse(getElement(FIRST_DATE_REGEX, sourceCode));
		} catch (ParseException e) {
			ad.firstPubDate.setTime(System.currentTimeMillis());
			System.err.println("Date not found in ad " + adUrl);
		}

		// Get last pub date
		ad.lastPubDate = ad.firstPubDate;

		// Get description
		// Too boring to get
		ad.description = "";

		// Ad is not postprocessed yet
		ad.isPostprocessed = false;

		// Get is pro
		String proString = getElement(IS_PRO_REGEX, sourceCode);
		if (proString.length() > 0) {
			ad.isPro = false;
		} else {
			ad.isPro = true;
		}

		// Get postal code
		ad.zipcode = getElement(POSTAL_CODE_REGEX, sourceCode);

		// Get number of rooms
		ad.rooms = getElement(ROOMS_REGEX, sourceCode);

		return ad;
	}

	@Override
	protected void addToDatabase(Ad ad) {

		if (ad.lastPubDate.after(database.lastParuVenduAdDate)) {
			database.lastParuVenduAdDate = ad.lastPubDate;
		}
		database.add(ad);
	}

	@Override
	protected String getSiteName() {
		return "Paru Vendu";
	}
}
