package pl.parser.nbp;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {

    private Currency currency;

    private boolean currencyFound = false;
    private boolean buyFound = false;
    private boolean sellFound = false;
    private boolean collect = false;

    private double buySum = 0;
    private int buyRecords = 0;
    private List<Double> sellRates = new ArrayList<>();

    public XmlHandler(Currency currency) {
        super();
        this.currency = currency;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("kod_waluty")) {
            currencyFound = true;
        }
        if (collect && qName.equalsIgnoreCase("kurs_kupna")) {
            buyFound = true;
        }
        if (collect && qName.equalsIgnoreCase("kurs_sprzedazy")) {
            sellFound = true;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (collect && qName.equalsIgnoreCase("pozycja")) {
            collect = false;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (currencyFound) {
            if (currency.name().equals(new String(ch, start, length))) {
                collect = true;
            }
            currencyFound = false;
        }
        if (buyFound) {
            buyRecords++;
            buySum += new Double(new String(ch, start, length).replace(",", "."));
            buyFound = false;
        }
        if (sellFound) {
            sellRates.add(new Double(new String(ch, start, length).replace(",", ".")));
            sellFound = false;
        }
    }

    public double getBuyMean() {
        if (buyRecords == 0) {
            return 0;
        }
        return (buySum / buyRecords);
    }

    public double getSellStandardDeviation() {
        if (sellRates.size() == 0) {
            return 0;
        }
        // mean
        double sellSum = 0;
        for (Double sellRate : sellRates) {
            sellSum += sellRate;
        }
        double sellMean = sellSum / sellRates.size();
        // variance
        double varianceTemp = 0;
        for (Double sellRate : sellRates) {
            varianceTemp += (sellMean - sellRate) * (sellMean - sellRate);
        }
        double variance = varianceTemp / sellRates.size();
        // standard deviation
        return Math.sqrt(variance);
    }
}
