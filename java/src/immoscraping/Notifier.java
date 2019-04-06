package immoscraping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Notifier {

	private static final String MAIL_ADDRESS_FILE = "/home/anatole/Documents/Code/Immo-Scraping/mailapi/mail_address";
	private static final String PASSWD_FILE = "/home/anatole/Documents/Code/Immo-Scraping/mailapi/passwd";

	public void notify(Date sinceDate, String mail, Database database) throws IOException, InterruptedException {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à HH:mm");
		SimpleDateFormat sdfEng = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		String body = "Bonjour Anatole,\n\n";

		int[] iAd = new int[1];
		for (Ad ad : database.ads) {
			if (!ad.discoverDate.before(sinceDate)) {
				iAd[0]++;
			}
		}

		System.out.format("\n%d new ads discovered between %s and %s.\n", iAd[0], sdfEng.format(sinceDate),
				sdfEng.format(new Date()));

		body += String.format("Ces annonces pourraient vous intéresser :\n");
		iAd[0] = 0;

		// Geolocated ads
		String desc = "Les adresses de ces maisons sont probablement connues";
		double price = 500000;
		double surface = 90;
		int freq = 1;
		String type = "Maison";
		double travelTime = 30 * 60;
		char energy = 'Z';
		char ges = 'Z';
		body += selectAds(sinceDate, database, desc, price, surface, freq, type, travelTime, energy, ges, iAd);

		// Very close ads
		desc = "Ces maisons sont très proche du boulot";
		freq = Integer.MAX_VALUE;
		travelTime = 10 * 60;
		body += selectAds(sinceDate, database, desc, price, surface, freq, type, travelTime, energy, ges, iAd);

		// Houses
		desc = "Les maisons";
		type = "Maison";
		travelTime = 30 * 60;
		body += selectAds(sinceDate, database, desc, price, surface, freq, type, travelTime, energy, ges, iAd);
//
//		// Flats
//		desc = "Les apparts";
//		type = "Appartement";
//		travelTime = 30 * 60;
//		body += selectAds(sinceDate, database, desc, price, surface, freq, type, travelTime, energy, ges);
//
//		// All others
//		body += selectAllAds(sinceDate, database);

		if (iAd[0] > 0) {
			System.out.println("____________________________________________________________________\n");
			System.out.print(body);
			System.out.println("____________________________________________________________________\n");
			sendMail("Immo-scraping du " + sdf.format(new Date()), body, mail);
		}
	}

	private String selectAds(Date lastUpdateDate, Database database, String desc, double price, double surface,
			int freq, String type, double travelTime, char energy, char ges, int[] iAd) {
		String submsg = "";
		submsg += "\n\n";
		submsg += String.format("%s:\n", desc);
		submsg += String.format(
				"(Prix < %de, surface > %dm2, type : %s, taf a moins de %d minutes, NRJ >= %s, GES >= %s, freq = %d\n)",
				(int) price, (int) surface, type, (int) (travelTime / 60), energy, ges, freq);
		for (Ad ad : database.ads) {
			if (!ad.discoverDate.before(lastUpdateDate)) {
				if (ad.price <= price && ad.surface >= surface && ad.type.compareTo(type) == 0
						&& ad.travelTime <= travelTime && ad.energyGrade <= energy && ad.gesGrade <= ges
						&& ad.latLonFreq <= freq) {
					submsg += String.format("%s (lat,long)=(%f,%f)\n", ad.url, ad.latLon[0], ad.latLon[1]);
					iAd[0]++;
				}

			}
		}
		if (iAd[0] == 0) {
			submsg = "";
		}
		return submsg;
	}

	public void sendMail(String subject, String body, String destination) throws IOException, InterruptedException {

		BufferedReader br = new BufferedReader(new FileReader(MAIL_ADDRESS_FILE));
		final String username = br.readLine();
		br.close();
		br = new BufferedReader(new FileReader(PASSWD_FILE));
		final String password = br.readLine();
		br.close();
		System.out.printf("ID: %s, passwd : %s\n", username, password);

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.laposte.net");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		boolean isSendFail = true;
		while (isSendFail) {
			try {

				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(username));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destination));
				message.setSubject(subject);
				message.setText(body);

				Transport.send(message);

				System.out.println("Notification sent");
				isSendFail = false;
			} catch (MessagingException e) {
				isSendFail = true;
				System.out.println("Send failed : ");
				System.out.println(e.getMessage());
				Thread.sleep(10000);
			}
		}
	}
}
