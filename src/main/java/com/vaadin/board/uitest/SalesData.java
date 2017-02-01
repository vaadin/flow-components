package com.vaadin.board.uitest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SalesData {

    private final int time;
    private final double value;
    private static Random random = new Random(123);

    public SalesData(int time, double value) {
        this.time = time;
        this.value = value;
    }

    public int getTime() {
        return time;
    }

    public double getValue() {
        return value;
    }

    public static List<SalesData> generateMonthData() {
        List<SalesData> data = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            data.add(new SalesData(i, random.nextDouble() * 1000 / 12));
        }
        return data;
    }

    public static List<SalesData> generateYearData() {
        List<SalesData> data = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            data.add(new SalesData(i, random.nextDouble() * 1000));
        }
        return data;
    }

}
