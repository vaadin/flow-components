package com.vaadin.flow.component.charts.examples.timeline.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockPrices {

    protected static class TimeData {

        private long date;

        private TimeData(long date) {
            this.date = date;
        }

        public long getDate() {
            return date;
        }
    }

    public static class PriceData extends TimeData {

        private double price;

        private PriceData(long date, double price) {
            super(date);
            this.price = price;
        }

        public double getPrice() {
            return price;
        }
    }

    public static class RangeData extends TimeData {

        private double min;
        private double max;

        private RangeData(long date, double min, double max) {
            super(date);
            this.min = min;
            this.max = max;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }
    }

    public static class OhlcData extends TimeData {

        private double open;
        private double high;
        private double low;
        private double close;

        private OhlcData(long date, double open, double high, double low,
                double close) {
            super(date);
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }

        public double getOpen() {
            return open;
        }

        public double getHigh() {
            return high;
        }

        public double getLow() {
            return low;
        }

        public double getClose() {
            return close;
        }
    }

    public static class JsonData {
        private Number[][] data;

        public Number[][] getData() {
            return data;
        }

        public void setData(Number[][] data) {
            this.data = data;
        }
    }

    public static List<PriceData> fetchAaplPrice() {
        List<PriceData> data = readValueData("aapl-price.json");
        return Collections.unmodifiableList(data);
    }

    public static List<PriceData> fetchAaplPriceWithTime() {
        List<PriceData> data = readValueData("aapl-price-withtime.json");
        return Collections.unmodifiableList(data);
    }

    public static List<PriceData> fetchGoogPrice() {
        List<PriceData> data = readValueData("goog-price.json");
        return Collections.unmodifiableList(data);
    }

    public static List<PriceData> fetchMsftPrice() {
        List<PriceData> data = readValueData("msft-price.json");
        return Collections.unmodifiableList(data);
    }

    public static List<OhlcData> fetchAaplOhlcPrice() {
        List<OhlcData> data = readOhlcData("aapl-ohlc.json");
        return Collections.unmodifiableList(data);
    }

    public static List<RangeData> fetchDailyTempRanges() {
        List<RangeData> data = readRangeData("daily-temp-ranges.json");
        return Collections.unmodifiableList(data);
    }

    private static List<PriceData> readValueData(String filename) {
        JsonData jsonData = readJsonDataFrom(filename);

        List<PriceData> data = new ArrayList<>();
        for (int i = 0; i < jsonData.data.length; ++i) {
            Number[] row = jsonData.data[i];
            data.add(new PriceData(row[0].longValue(), row[1].doubleValue()));
        }

        return data;
    }

    private static List<RangeData> readRangeData(String filename) {
        JsonData jsonData = readJsonDataFrom(filename);

        List<RangeData> data = new ArrayList<>();
        for (int i = 0; i < jsonData.data.length; ++i) {
            Number[] row = jsonData.data[i];
            data.add(new RangeData(row[0].longValue(), row[1].doubleValue(),
                    row[2].doubleValue()));
        }

        return data;
    }

    private static List<OhlcData> readOhlcData(String filename) {
        JsonData jsonData = readJsonDataFrom(filename);

        List<OhlcData> data = new ArrayList<>();
        for (int i = 0; i < jsonData.data.length; ++i) {
            Number[] row = jsonData.data[i];
            data.add(new OhlcData(row[0].longValue(), row[1].doubleValue(),
                    row[2].doubleValue(), row[3].doubleValue(),
                    row[4].doubleValue()));
        }

        return data;
    }

    private static JsonData readJsonDataFrom(String filename) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(
                    new InputStreamReader(
                            StockPrices.class.getResourceAsStream(filename)),
                    JsonData.class);

        } catch (IOException e) {
            throw new RuntimeException("Cannot read data from " + filename, e);
        }
    }

}
