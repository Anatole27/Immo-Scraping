package immoscraping.tests;

import java.io.IOException;
import java.util.Date;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import immoscraping.Ad;
import immoscraping.Database;

class TestDatabase extends Database {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4716854097985039475L;

	public TestDatabase() {
		super();
	}

	@Test
	void test() throws IOException {
		Ad ad1 = new Ad();
		ad1.url = "url 1";
		ad1.latLon = new double[] { 43.553459, 1.496918 };
		Ad ad2 = new Ad();
		ad2.url = "url 1";
		ad2.latLon = new double[] { 43.553459, 1.493918 };
		Ad ad3 = new Ad();
		ad3.url = "url 2";
		ad3.latLon = new double[] { 43.553459, 1.496918 };

		add(ad1);
		process(new Date());
		Assert.assertTrue(this.ads.size() == 1);
		Assert.assertTrue(this.latLongList.size() == 1);
		Assert.assertTrue(this.travelDistanceList.size() == 1);

		add(ad2);
		process(new Date());
		Assert.assertTrue(this.ads.size() == 1);
		Assert.assertTrue(this.latLongList.size() == 1);
		Assert.assertTrue(this.travelDistanceList.size() == 1);

		add(ad3);
		process(new Date());
		Assert.assertTrue(this.ads.size() == 2);
		Assert.assertTrue(this.latLongList.size() == 1);
		Assert.assertTrue(this.travelDistanceList.size() == 1);

	}

	public static void main(String[] args) {
		TestDatabase database = new TestDatabase();
		try {
			database.test();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
