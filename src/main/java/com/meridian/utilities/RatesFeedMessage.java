package main.java.com.meridian.utilities;

/*
 * Represents one RSS message
 */

public class RatesFeedMessage {
    String symbol;
    String name;
    String quoteDate;
    String value;
    String dataToBeRead;

    public String getName () { return name; }

    public void setName (String name) {
        this.name = name;
    }

    public String getQuoteDate () {
        return quoteDate;
    }

    public void setQuoteDate (String quoteDate) {
        this.quoteDate = quoteDate;
    }

    public String getValue () {
        return value;
    }

    public void setValue (String value) {
        this.value = value;
    }

    public String getSymbol () {
        return symbol;
    }

    public void setSymbol (String symbol) {
        this.symbol = symbol;
    }

    public String getDataToBeRead () {
        return dataToBeRead;
    }

    public void setDataToBeRead (String dataToBeRead) {
        this.dataToBeRead = dataToBeRead;
    }
}
