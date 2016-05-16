package com.vaadin.spreadsheet.charts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.vaadin.spreadsheet.charts.typetests.LineAreaScatterTest;
import com.vaadin.spreadsheet.charts.typetests.ColumnAndBarTest;
import com.vaadin.spreadsheet.charts.typetests.PieAndDonutTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ LineAreaScatterTest.class, ChartFeatureTest.class,
        ColumnAndBarTest.class, PieAndDonutTest.class, StyleTest.class })
public class AllChartTestsSuite {

}
