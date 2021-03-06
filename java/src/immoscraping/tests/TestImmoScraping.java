package immoscraping.tests;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;

import immoscraping.ImmoScraping;

class TestImmoScraping {
	public static void main(String[] args)
			throws ClassNotFoundException, ParseException, InterruptedException, IOException {
		testScrape();
	}

	@Test
	void testHelp() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(new String[] {});
		// fail("Not yet implemented");
	}

	@Test
	static void testScrape() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(new String[] { "scrape", "--email", "anatole.verhaegen@gmail.com", "--since",
				"06-04-2019_00:00", "--loop" });
		// fail("Not yet implemented");
	}

	@Test
	void testCompleteScrape() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(
				new String[] { "scrape", "--email", "anatole.verhaegen@gmail.com", "--since", "30-11-2018_12:00" });
		// fail("Not yet implemented");
	}

	void testExport() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(new String[] { "export", "--filename", "data.csv" });
		// fail("Not yet implemented");
	}

	// @Test
	// void testRefreshDatabase() throws ParseException, InterruptedException,
	// ClassNotFoundException, IOException {
	// ImmoScraping.main(new String[] { "refresh" });
	//// fail("Not yet implemented");
	// }
	@Test
	void testExportRegex() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(
				new String[] { "export", "--filename", "regexData.csv", "--regex", "garage", "atelier", "bricol" });
		// fail("Not yet implemented");
	}

	@Test
	void testNotify() throws ParseException, InterruptedException, ClassNotFoundException, IOException {
		ImmoScraping.main(
				new String[] { "notify", "--email", "anatole.verhaegen@gmail.com", "--since", "06-04-2019_10:50" });
		// fail("Not yet implemented");
	}

}
