package com.vaadin.flow.component.charts.tests;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.column.ColumnWithLazyMultiLevelDrilldownCallbackTests;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ColumnWithLazyMultiLevelDrilldownCallbackTestsIT
        extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return ColumnWithLazyMultiLevelDrilldownCallbackTests.class;
    }

    @Test
    public void test() throws AssertionError {
        ChartElement chart = $(ChartElement.class).first();
        waitUntil(e -> getElementFromShadowRoot(chart,
                ".highcharts-drilldown-point") != null);
        clickDrilldownPoint(chart, 0);
        // Can't drilldown with null callback
        assertEquals(0, getLogMessages().size());

        // Set new callback
        findElement(By.id("setNew")).click();
        // Showing nested drilldowns
        clickDrilldownPoint(chart, 0);
        assertLastLogText("DrilldownEvent: Item1");
        clickDrilldownPoint(chart, 1);
        assertLastLogText("DrilldownEvent: Item1_2");
        clickDrilldownPoint(chart, 1);
        assertLastLogText("DrilldownEvent: Item1_2_2");
        // Set same callback
        findElement(By.id("setSame")).click();
        getDrillUpButtonByTopItemName(chart, "Item1_2").click();
        assertLastLogText("ChartDrillupEvent");
        clickDrilldownPoint(chart, 0);
        assertLastLogText("DrilldownEvent: Item1_2_1");
        getDrillUpButtonByTopItemName(chart, "Item1_2").click();
        assertLastLogText("ChartDrillupEvent");

        // Set callback to null
        findElement(By.id("setNull")).click();
        clickDrilldownPoint(chart, 0);
        // Can't drilldown with null callback
        assertLastLogText("ChartDrillupEvent");
        getDrillUpButtonByTopItemName(chart, "Item1").click();
        // At Item1
        getDrillUpButton(chart, "Back to Top").click();
        // At top level, no more drilldown buttons
        assertLastLogText("ChartDrillupEvent");

        // Can't drilldown with null callback
        clickDrilldownPoint(chart, 0);
        assertLastLogText("ChartDrillupEvent");
    }

    private void clickDrilldownPoint(ChartElement chart, int index) {
        getElementFromShadowRoot(chart, ".highcharts-drilldown-point", index)
                .click();
    }

    private WebElement getDrillUpButtonByTopItemName(ChartElement chart,
            String item) {
        return getDrillUpButton(chart, "Back to " + item + "_drill");
    }

    private WebElement getDrillUpButton(ChartElement chart, String label) {
        final String selector = String.format("button[aria-label=\"◁ %s\"",
                label);
        return getElementFromShadowRoot(chart, selector);
    }

    private void assertLastLogText(String text) {
        final List<String> messages = getLogMessages();
        assertEquals(text, messages.get(messages.size() - 1));
    }

    private void assertLogText(String text) {
        final StringBuilder sb = new StringBuilder();
        getLogMessages().forEach(sb::append);
        final String messages = sb.toString();
        assertTrue(String.format("Couldn't find text '%s' from the log.", text),
                messages.contains(text));
    }

    private List<String> getLogMessages() {
        return findElements(By.tagName("li")).stream().map(e -> e.getText())
                .collect(Collectors.toList());
    }
}
