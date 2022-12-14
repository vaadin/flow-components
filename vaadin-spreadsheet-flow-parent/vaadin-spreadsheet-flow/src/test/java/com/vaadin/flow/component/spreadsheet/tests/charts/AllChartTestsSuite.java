package com.vaadin.flow.component.spreadsheet.tests.charts;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.vaadin.flow.component.spreadsheet.tests.charts.typetests.ColumnAndBarTest;
import com.vaadin.flow.component.spreadsheet.tests.charts.typetests.LineAreaScatterTest;
import com.vaadin.flow.component.spreadsheet.tests.charts.typetests.PieAndDonutTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ LineAreaScatterTest.class, ChartFeatureTest.class,
        ColumnAndBarTest.class, PieAndDonutTest.class, StyleTest.class })
public class AllChartTestsSuite {
}
