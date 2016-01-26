package pl.parser.nbp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MainClass {

    static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    static String nbpKursyPrefixUrl = "http://www.nbp.pl/kursy/xml/";

    public static void main(String[] args) {
        try {
            if (args.length != 3) {
                throw new Exception("Invalid args count");
            }
            Currency currency = Currency.valueOf(args[0]);
            DateTime startDate = formatter.parseDateTime(args[1]);
            DateTime endDate = formatter.parseDateTime(args[2]);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            XmlHandler xmlHandler = new XmlHandler(currency);

            DateTime now = DateTime.now();

            while (!startDate.isAfter(endDate)) {
                String dirFileUri = "dir" + (startDate.getYear() != now.getYear() ? startDate.getYear() : "") + ".txt";

                try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(nbpKursyPrefixUrl + dirFileUri).openStream()))) {
                    for (String line; (line = br.readLine()) != null;) {
                        if (line.matches("^c\\d{3}z" + startDate.toString("yy") + startDate.toString("MM") + startDate.toString("dd") + "$")) {
                            // System.out.println("found : " + line);
                            saxParser.parse(new URI(nbpKursyPrefixUrl + line + ".xml").toString(), xmlHandler);
                            break;
                        }
                    }
                }
                startDate = startDate.plusDays(1);
            }

            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.CEILING);
            System.out.println(df.format(xmlHandler.getBuyMean()));
            System.out.println(df.format(xmlHandler.getSellStandardDeviation()));

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
