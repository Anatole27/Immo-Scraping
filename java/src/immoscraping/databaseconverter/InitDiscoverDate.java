package immoscraping.databaseconverter;

import java.io.IOException;
import java.util.Date;

import immoscraping.Ad;

public class InitDiscoverDate extends DatabaseConverter {
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		loadDatabase();

		for (Ad ad : database.ads) {
			if (ad.discoverDate == null) {
				ad.discoverDate = (Date) ad.firstPubDate.clone();
			}
		}

		saveDatabase();
	}

}
