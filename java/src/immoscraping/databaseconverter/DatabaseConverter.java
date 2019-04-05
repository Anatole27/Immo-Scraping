package immoscraping.databaseconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;

import immoscraping.Database;

public class DatabaseConverter {

	private final static String DATA_PATH = "/home/anatole/Documents/Code/Immo-Scraping/java/data";
	private final static String DATABASE_SAVE_FILEPATH = Paths.get(DATA_PATH, "database.db").toString();
	protected static Database database = new Database();

	static protected void loadDatabase() throws IOException, ClassNotFoundException {
		File f = new File(DATABASE_SAVE_FILEPATH);
		if (f.exists()) {
			FileInputStream fin = new FileInputStream(DATABASE_SAVE_FILEPATH);
			ObjectInputStream oos = new ObjectInputStream(fin);
			database = (Database) oos.readObject();
			oos.close();
			fin.close();
		}
	}

	protected static void saveDatabase() throws IOException {
		File f = new File(DATABASE_SAVE_FILEPATH);
		f.getParentFile().mkdirs();
		FileOutputStream fout = new FileOutputStream(DATABASE_SAVE_FILEPATH);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(database);
		oos.close();
		fout.close();
	}
}
