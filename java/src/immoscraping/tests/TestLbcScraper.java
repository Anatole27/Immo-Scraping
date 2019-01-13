package immoscraping.tests;

import java.util.Date;

import immoscraping.Database;
import immoscraping.LbcScraper;

class TestLbcScraper {

//	@Test
	public static void main(String[] args) {
		LbcScraper lbScraper = new LbcScraper(new Database(), new Date());
		lbScraper.run();
	}
	
}
