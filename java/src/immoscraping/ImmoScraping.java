package immoscraping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class ImmoScraping {

	public static void main(String[] args)
			throws ParseException, InterruptedException, ClassNotFoundException, IOException {

		// If no argument, print help
		if (args.length == 0) {
			printHelp();
			return;
		}

		ImmoScraping immoScraping = new ImmoScraping();
		immoScraping.run(args);
	}

	private void run(String[] args) throws ParseException, InterruptedException, ClassNotFoundException, IOException {

		switch (args[0]) {
		case "reset":
			resetDatabase(args);
			break;
		case "export":
			verifyAndExportDataBase(args);
			break;
		case "scrape":
			verifyAndScrape(args);
			break;
		default:
			printHelp();
		}

	}

	/**
	 * Runs a scraping on the internet.
	 * 
	 * @param args
	 * @throws ParseException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void verifyAndScrape(String[] args)
			throws ParseException, InterruptedException, ClassNotFoundException, IOException {

		String mail = "";
		Date sinceDate = new Date(0);

		int iArg = 1;
		while (iArg < args.length) {

			switch (args[iArg]) {

			// Website to scrape is specified
			case "--website":
				System.out.println("Leboncoin is the only site yet. Option invalid. Returning.");
				return;

			// The date since last scraping is specified
			case "--since":
				if (iArg + 1 >= args.length) {
					printHelp();
					return;
				}
				String since = args[iArg + 1];
				if (!Pattern.matches("[0-9]{2}-[0-9]{2}-[0-9]{4} [0-9]{2}:[0-9]{2}", since)) {
					printHelp();
					return;
				}
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
				sinceDate = formatter.parse(since);
				iArg += 2;
				break;

			// The mail to send updates is specified
			case "--email":
				if (iArg + 1 >= args.length) {
					printHelp();
					return;
				}
				mail = args[iArg + 1];
				if (!Pattern.matches(".*@.*\\..*", mail)) {
					printHelp();
					return;
				}
				iArg += 2;
				break;

			default:
				printHelp();
				return;
			}

		}

		runScraping(mail, sinceDate);

	}

	private void runScraping(String mail, Date sinceDate)
			throws InterruptedException, ClassNotFoundException, IOException {

		// Load database from the autosave
		loadDatabase();
		webScraper = new LbcScraper(database, sinceDate);
		webScraper.start();

		// Save database periodically in case the program crashes
		long time = System.currentTimeMillis();
		while (webScraper.isAlive()) {
			if (System.currentTimeMillis() - time > AUTO_SAVE_PERIOD) {
				time = System.currentTimeMillis();
				System.out.println("Saving database");
				saveDatabase();
			}
			Thread.sleep(ALIVE_CHECK_PERIOD);
		}

		// Update database information
		database.process();

		// Save database
		saveDatabase();

		// Notify me by email
		if (!mail.equals("")) {
			notifier.notify(mail, database);
		}

	}

	private void loadDatabase() throws IOException, ClassNotFoundException {
		File f = new File(DATABASE_SAVE_FILEPATH);
		if (f.exists()) {
			FileInputStream fin = new FileInputStream(DATABASE_SAVE_FILEPATH);
			ObjectInputStream oos = new ObjectInputStream(fin);
			database = (Database) oos.readObject();
			oos.close();
			fin.close();
		}
	}

	private void saveDatabase() throws IOException {
		File f = new File(DATABASE_SAVE_FILEPATH);
		f.getParentFile().mkdirs();
		FileOutputStream fout = new FileOutputStream(DATABASE_SAVE_FILEPATH);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(database);
		oos.close();
		fout.close();
	}

	private void verifyAndExportDataBase(String[] args) {

		String filename = DEFAULT_FILENAME;

		int iArg = 1;
		while (iArg < args.length) {

			switch (args[iArg]) {

			// filename
			case "--filename":
				String reqFilename = args[iArg + 1];
				if (!Pattern.matches(".*\\.csv", reqFilename)) {
					printHelp();
					return;
				}
				filename = Paths.get(OUT_PATH, reqFilename).toString();
				iArg += 2;
				break;

			default:
				printHelp();
				return;
			}

		}

		try {
			loadDatabase();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exportDatabase(filename);
	}

	private void exportDatabase(String filename) {
		database.export(filename);
	}

	/**
	 * Removes the saved database
	 * 
	 * @param args
	 */
	private static void resetDatabase(String[] args) {
		// TODO Auto-generated method stub

	}

	private static void printHelp() {
		System.out.println("HELP MSG TODO");
	}

	private final static String DATA_PATH = "/home/anatole/Documents/Code/Immo-Scraping/java/data";
	private final static String OUT_PATH = "/home/anatole/Documents/Code/Immo-Scraping/java/out";
	private final static String DATABASE_SAVE_FILEPATH = Paths.get(DATA_PATH, "database.db").toString();
	private static final String DEFAULT_FILENAME = Paths.get(OUT_PATH, "database.xls").toString();
	private final static long MINUTES = 60 * 1000;
	private final static long AUTO_SAVE_PERIOD = 5 * MINUTES;
	private final static long ALIVE_CHECK_PERIOD = 1000;
	private Database database = new Database();
	private WebScraper webScraper;
	private Notifier notifier;

}