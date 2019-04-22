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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Pattern;

import immoscraping.scrapers.LbcScraper;
import immoscraping.scrapers.PapScraper;
import immoscraping.scrapers.ParuVenduScraper;
import immoscraping.scrapers.WebScraper;

public class ImmoScraping {

	private static final long LOOP_PERIOD = 1800000; // ms
	private static final int WAKING_UP_HOUR = 8;
	private static final int SLEEPING_HOUR = 23;

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
		case "notify":
			notify(args);
			break;
		case "refresh":
			refreshDb();
			break;
		default:
			printHelp();
		}

	}

	private void notify(String[] args)
			throws ClassNotFoundException, IOException, ParseException, InterruptedException {

		String mail = "";
		Date sinceDate = new Date(0);

		int iArg = 1;
		while (iArg < args.length) {

			switch (args[iArg]) {

			// The date since last scraping is specified
			case "--since":
				if (iArg + 1 >= args.length) {
					printHelp();
					return;
				}
				String since = args[iArg + 1];
				if (!Pattern.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}_[0-9]{2}:[0-9]{2}", since)) {
					printHelp();
					return;
				}
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH:mm");
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

		loadDatabase();
		notifier.notify(sinceDate, mail, database);
	}

	private void refreshDb() throws ClassNotFoundException, IOException {

		loadDatabase();
		database.refresh();
		saveDatabase();
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
		boolean isLoop = false;

		int iArg = 1;
		while (iArg < args.length) {

			switch (args[iArg]) {

			// The date since last scraping is specified
			case "--since":
				if (iArg + 1 >= args.length) {
					printHelp();
					return;
				}
				String since = args[iArg + 1];
				if (!Pattern.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}_[0-9]{2}:[0-9]{2}", since)) {
					printHelp();
					return;
				}
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH:mm");
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

			// Used to keep on scraping
			case "--loop":
				isLoop = true;
				iArg += 1;
				break;

			default:
				printHelp();
				return;
			}

		}

		runScraping(mail, sinceDate, isLoop);

	}

	Date nextScrapeDate;

	private void runScraping(String mail, Date sinceDate, boolean isLoop)
			throws InterruptedException, ClassNotFoundException, IOException {

		nextScrapeDate = new Date();

		do {
			if (isTimeToScrape()) {
				// Scrap start date
				Date scrapeStartDate = new Date();

				// Load database from the autosave
				loadDatabase();

				// Init scrapers
				webScrapers.clear();
				if (isLoop) {

					webScrapers.add(new LbcScraper(database, aBitBefore(database.lastLbcAdDate)));
					webScrapers.add(new PapScraper(database, aBitBefore(database.lastPapAdDate)));
					webScrapers.add(new ParuVenduScraper(database, aBitBefore(database.lastParuVenduAdDate)));
				} else {
					webScrapers.add(new LbcScraper(database, sinceDate));
					webScrapers.add(new PapScraper(database, sinceDate));
					webScrapers.add(new ParuVenduScraper(database, sinceDate));
				}
				for (WebScraper webScraper : webScrapers) {
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
				}

				// Update database information
				database.process();

				// Save database
				saveDatabase();

				// Notify me by email
				if (!mail.equals("")) {
					notifier.notify(scrapeStartDate, mail, database);
				}
			}
			Thread.sleep(1000);
		} while (isLoop);
	}

	private static final long A_BIT_BEFORE = 1000l * 60l * 60l * 24l; // 24h

	private Date aBitBefore(Date lastLbcAdDate) {
		long time = lastLbcAdDate.getTime() - A_BIT_BEFORE;
		Date newDate = new Date(time);
		return newDate;
	}

	Random rand = new Random(0);
	Calendar calendar = new GregorianCalendar();
	boolean scanEnd = false;

	/**
	 * @return True when it is time to scrape
	 */
	private boolean isTimeToScrape() {
		Date currentDate = new Date();
		if (!currentDate.before(nextScrapeDate)) {

			// Find the next scraping date by incrementing
			// No scrape at night
			boolean isDaylight = false;
			while (!isDaylight) {
				long nextScrapeTime = nextScrapeDate.getTime();
				nextScrapeTime += LOOP_PERIOD * (1 + rand.nextGaussian() / 10); // Random to make it look humanly
				nextScrapeDate.setTime(nextScrapeTime);
				calendar.setTime(nextScrapeDate);
				int dayHour = calendar.get(Calendar.HOUR_OF_DAY);
				if (dayHour >= WAKING_UP_HOUR && dayHour < SLEEPING_HOUR) {
					isDaylight = true;
				}
			}
			scanEnd = true;
			return true;
		} else {
			if (scanEnd) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
				System.out.printf("Next scan: %s\n", sdf.format(nextScrapeDate));
				scanEnd = false;
			}
			return false;
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
		Vector<String> regexList = new Vector<>();

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

			// keyword
			case "--regex":
				iArg++;
				String regex = args[iArg];
				if (Pattern.matches("--.*", regex)) {
					printHelp();
					return;
				}
				regexList.add(regex);

				iArg++;
				while (iArg < args.length) {
					regex = args[iArg];
					if (Pattern.matches("--.*", regex)) {
						break;
					}
					regexList.add(regex);
					iArg++;
				}
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
		exportDatabase(filename, regexList);
	}

	private void exportDatabase(String filename, Vector<String> regexList) {
		database.export(filename, regexList);
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
	private Vector<WebScraper> webScrapers = new Vector<WebScraper>();
	private Notifier notifier = new Notifier();

}
