package immoscraping.tests;

import java.util.Date;

//import static org.junit.jupiter.api.Assertions.*;

import immoscraping.Ad;
import immoscraping.Database;
import immoscraping.scrapers.LbcScraper;

class TestAdLbcScraping extends LbcScraper {

	public TestAdLbcScraping(Database database, Date sinceDate) {
		super(database, sinceDate);
	}

	private static final String AD_URL = "https://www.leboncoin.fr/ventes_immobilieres/1536174671.htm/";
	
//	@Test
	void test() {
		Ad ad = getAd(AD_URL);
		System.out.println(ad.toString());
		close();
	}
	
	public static void main(String[] args) {
		TestAdLbcScraping test = new TestAdLbcScraping(null, null);
		test.test();
	}

}
