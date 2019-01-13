package immoscraping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// TODO Thread safety
public class Database implements Serializable {

	private static final long serialVersionUID = -7749365650227300460L;
	protected Vector<Ad> ads = new Vector<>();
	protected Vector<double[]> latLongList = new Vector<>();
	protected Vector<Double> travelDistanceList = new Vector<>();

	public Database() {
	}

	/**
	 * Add the advertisement if it is unique
	 * @param ad
	 */
	public void add(Ad ad) {
		for(int i = 0; i < ads.size(); i++) {
			if(ad.url.equals(ads.get(i).url)){
				return;
			}
		}
		ads.add(ad);
	}

	public void process() throws IOException {
		for(Ad ad : ads) {
			// Get travel distance
			ad.travelTime = getDistance(ad.latLon);

			// Is pro ad ?
			ad.isPro = getIsPro(ad.description);
		}
	}

	private static final String MAPS_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%f,%f&destinations=%f,%f&mode=bicycling&language=en-EN&sensor=false&key=%s";
	private static final double[] workLatLon = {43.563459, 1.496918};
	private static final String apiKeyFilepath = "/home/anatole/Desktop/Immo-Scraping/googlemapsapi/key";
	private static final String LAT_REGEX = "(?<=\"value\" : )[0-9]*";

	private double getDistance(double[] latLon) throws IOException {

		// Look if lat/long are already known
		int i = 0;
		for(double[] latLonDatabase : latLongList) {
			if(latLonDatabase[0] == latLon[0] && latLonDatabase[1] == latLon[1]) {
				return travelDistanceList.get(i);
			}
			i++;
		}

		// Lat long not know: ask google maps
		BufferedReader br = new BufferedReader(new FileReader(apiKeyFilepath)); 
		String apiKey = br.readLine();
		br.close();
		URL googleApiUrl = new URL(String.format(MAPS_URL, latLon[0], latLon[1], 
				workLatLon[0], workLatLon[1], apiKey));
		BufferedReader in = new BufferedReader(
				new InputStreamReader(googleApiUrl.openStream()));

		String inputLine;
		String sourceCode = "";
		while ((inputLine = in.readLine()) != null) {
			sourceCode += inputLine + "\n";
		}
		in.close();

		Pattern pattern = Pattern.compile(LAT_REGEX);
		Matcher matcher = pattern.matcher(sourceCode);
		double travelTime;
		if(matcher.find() && matcher.find()) {
			travelTime = Double.parseDouble(matcher.group());
		}
		else {
			System.err.printf("Distance to travel not found for lat/lon %f/%f\n",latLon[0],latLon[1]);
			travelTime = 0;
		}

		// Store new lat/long
		latLongList.add(latLon);
		travelDistanceList.add(travelTime);

		return travelTime;
	}

	private static final String[] PRO_STRING = {"référence annonce",
			"honoraires",
	"à la charge"};

	/**
	 * Return true of some words lead to think it is a pro ad
	 * @param description
	 * @return
	 */
	private boolean getIsPro(String description) {
		for(String proString : PRO_STRING) {
			if(description.toLowerCase().contains(proString)) {
				return true;
			}
		}
		return false;

	}

	public void export(String filename) {
		 File f = new File(filename);
		 f.getParentFile().mkdirs();
		 PrintWriter writer = null;
		try {
			writer = new PrintWriter(f.getAbsolutePath(), "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		// Print header
		writer.print("url");
		writer.print(";");
		writer.print("surface");
		writer.print(";");
		writer.print("price");
		writer.print(";");
		writer.print("firstPubDate");
		writer.print(";");
		writer.print("isPro");
		writer.print(";");
		writer.print("travelTime");
		writer.print(";");
		writer.print("energyGrade");
		writer.print(";");
		writer.print("gesGrade");
//		writer.print(";");
//		writer.print("description");
		writer.println("");
		
		// Print ads
		for(Ad ad : ads) {
			writer.print(ad.url);
			writer.print(";");
			writer.print(ad.surface);
			writer.print(";");
			writer.print(ad.price);
			writer.print(";");
			writer.print(ad.firstPubDate);
			writer.print(";");
			writer.print(ad.isPro);
			writer.print(";");
			writer.print(ad.travelTime);
			writer.print(";");
			writer.print(ad.energyGrade);
			writer.print(";");
			writer.print(ad.gesGrade);
//			writer.print(";");
//			writer.print("\"" + ad.description + "\"");
			writer.println("");
		}
		 writer.close();
	}
}