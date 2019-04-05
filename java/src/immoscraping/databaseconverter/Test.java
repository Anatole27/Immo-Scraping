package immoscraping.databaseconverter;

import java.util.Vector;

import immoscraping.Ad;

public class Test {

	public static void main(String[] args) {
		Ad ad1 = new Ad();
		Ad ad2 = new Ad();
		Vector<Ad> adList = new Vector<>();
		adList.add(ad1);
		ad1.description = "ad1";
		System.out.println(adList.get(0).description);
		ad2.description = "ad2";
		Ad ad1Bis = adList.get(0);
		ad1Bis = ad2;
		System.out.println(adList.get(0).description);
	}

}
