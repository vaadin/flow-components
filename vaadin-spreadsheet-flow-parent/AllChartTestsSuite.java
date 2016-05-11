package com.vaadin.spreadsheet.charts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.vaadin.spreadsheet.charts.interactiontests.InteractionTests;
import com.vaadin.spreadsheet.charts.typetests.LineAreaScatterTests;
import com.vaadin.spreadsheet.charts.typetests.ColumnAndBarTests;
import com.vaadin.spreadsheet.charts.typetests.PieAndDonutTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({ LineAreaScatterTests.class, ChartFeatureTests.class,
        ColumnAndBarTests.class, //InteractionTests.class,
        PieAndDonutTests.class, StyleTests.class })
public class AllChartTestsSuite {

}
