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
		ImmoScraping.main(
				new String[] { "scrape", "--email", "anatole.verhaegen@gmail.com", "--since", "31-11-2018_22:15" });
//		fail("Not yet implemented");
	}

	@Test
	void testCompleteScrape() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(
				new String[] { "scrape", "--email", "anatole.verhaegen@gmail.com", "--since", "30-11-2018_12:00" });
//		fail("Not yet implemented");
	}

	@Test
	void testExport() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(new String[] { "export", "--filename", "data.csv" });
//		fail("Not yet implemented");
	}

//	@Test
//	void testRefreshDatabase() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
//		ImmoScraping.main(new String[] { "refresh" });
////		fail("Not yet implemented");
//	}

	@Test
	void testExportRegex() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(new String[] { "export", "--filename", "regexData.csv", "--regex", "atelier" });
//		fail("Not yet implemented");
	}

	@Test
	void testNotify() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(
				new String[] { "notify", "--email", "anatole.verhaegen@gmail.com", "--since", "30-01-2019_12:00" });
//		fail("Not yet implemented");
	}

}
