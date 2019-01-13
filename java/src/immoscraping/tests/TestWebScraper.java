package immoscraping.tests;

import org.junit.jupiter.api.Test;

import immoscraping.WebScraper;

class TestWebScraper {

	@Test
	void test() throws Exception {
		WebScraper.getSource("https://www.leboncoin.fr/recherche/?category=9&regions=16&location=Toulouse_31400,Toulouse_31500&real_estate_type=2,1&square=min-140&page=1");
	}

}
