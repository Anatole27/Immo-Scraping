package immoscraping.tests;

import java.io.IOException;
import java.text.ParseException;

import org.junit.jupiter.api.Test;

import immoscraping.ImmoScraping;

class TestImmoScraping {

	@Test
	void testHelp() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(new String[] {});
//		fail("Not yet implemented");
	}

	@Test
	void testScrape() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(new String[] { "scrape", "--email", "allo@test.fd", "--since", "17-01-2019 19:15" });
//		fail("Not yet implemented");
	}

	@Test
	void testCompleteScrape() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(new String[] { "scrape", "--email", "allo@test.fd", "--since", "16-11-2018 12:00" });
//		fail("Not yet implemented");
	}

	@Test
	void testExport() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(new String[] { "export", "--filename", "data.csv" });
//		fail("Not yet implemented");
	}

	@Test
	void testRefreshDatabase() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(new String[] { "refresh" });
//		fail("Not yet implemented");
	}

}
