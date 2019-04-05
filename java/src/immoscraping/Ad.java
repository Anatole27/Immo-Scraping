package immoscraping;

import java.io.Serializable;
import java.util.Date;

public class Ad implements Serializable {

	private static final long serialVersionUID = -4475206259882056436L;
	public char energyGrade;
	public char gesGrade;
	public double surface;
	public double price;
	public boolean isPro;
	public double[] latLon = new double[2];
	public double travelTime;
	public Date firstPubDate = new Date();
	public Date lastPubDate = new Date();
	public String url = "";
	public boolean isPostprocessed;
	public String description = "";
	public String type = "";
	public int latLonFreq;
	public String zipcode;
	public String rooms;
	public Date discoverDate = new Date();

	@Override
	public String toString() {
		String desc = String.format(
				"Ad url=%s\n 1st pub date : %s\n Surface: %f\n" + " Price: %f\n Lat/lon: %f/%f\n Energy: %s\n GES: %s\n"
						+ "Description:\n %s",
				url, firstPubDate.toString(), surface, price, latLon[0], latLon[1], energyGrade, gesGrade, description);
		return desc;
	}
}
