package immoscraping.databaseconverter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InitWebscraperLastUpdate extends DatabaseConverter {
	public static void main(String[] args) throws ClassNotFoundException, IOException, ParseException {
		loadDatabase();

		Date date;
		String since = "03-04-2019_20:00";
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH:mm");
		date = formatter.parse(since);
		database.lastLbcAdDate = date;
		database.lastPapAdDate = date;
		database.lastParuVenduAdDate = date;

		saveDatabase();
	}

}
