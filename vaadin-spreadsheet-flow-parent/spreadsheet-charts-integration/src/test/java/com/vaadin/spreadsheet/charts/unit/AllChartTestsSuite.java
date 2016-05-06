package com.vaadin.spreadsheet.charts.unit;

import com.vaadin.spreadsheet.charts.integration.InteractionTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({ AreaAndLineTest.class, ChartFeatureTestsTest.class,
        ColumnAndBarTest.class, InteractionTests.class,
        PieAndDonutTest.class, StyleTests.class })
public class AllChartTestsSuite {

}
