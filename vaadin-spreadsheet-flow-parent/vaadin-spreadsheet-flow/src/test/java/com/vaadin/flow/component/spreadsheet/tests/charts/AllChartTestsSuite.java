/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.charts;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import com.vaadin.flow.component.spreadsheet.tests.charts.typetests.ColumnAndBarTest;
import com.vaadin.flow.component.spreadsheet.tests.charts.typetests.LineAreaScatterTest;
import com.vaadin.flow.component.spreadsheet.tests.charts.typetests.PieAndDonutTest;

@Suite
@SelectClasses({ LineAreaScatterTest.class, ChartFeatureTest.class,
        ColumnAndBarTest.class, PieAndDonutTest.class, StyleTest.class })
class AllChartTestsSuite {
}
