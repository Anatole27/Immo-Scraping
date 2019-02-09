package immoscraping.tests;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

import immoscraping.Ad;
import immoscraping.Database;
import immoscraping.Notifier;

class TestNotifier {

	@Test
	void test() throws IOException, ParseException, InterruptedException {
		Notifier notifier = new Notifier();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date sinceDate = sdf.parse("01/01/2000");
		String mail = "anatole.verhaegen@gmail.com";
		Database db = new Database();
		Ad ad1 = new Ad();
		ad1.energyGrade = 'A';
		ad1.gesGrade = 'B';
		ad1.firstPubDate = sdf.parse("02/01/2000");
		ad1.latLonFreq = 1;
		ad1.price = 100000;
		ad1.type = "Maison";
		ad1.travelTime = 0;
		ad1.surface = 100;
		ad1.url = "ad1";
		db.add(ad1);

		Ad ad2 = new Ad();
		ad2.energyGrade = 'A';
		ad2.gesGrade = 'B';
		ad2.firstPubDate = sdf.parse("02/01/2000");
		ad2.latLonFreq = 2;
		ad2.price = 100000;
		ad2.travelTime = 15 * 60;
		ad2.type = "Maison";
		ad2.surface = 100;
		ad2.url = "ad2";
		db.add(ad2);

		notifier.notify(sinceDate, mail, db);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Notifier notifier = new Notifier();
		notifier.sendMail("test", "test", "anatole.verhaegen@gmail.com");
	}

}
