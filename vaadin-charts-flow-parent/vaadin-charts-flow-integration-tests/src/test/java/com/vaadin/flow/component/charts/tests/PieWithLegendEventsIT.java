/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.charts.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.pie.PieWithLegendEvents;
import com.vaadin.flow.component.charts.events.PointLegendItemClickEvent;
import com.vaadin.tests.elements.SpanElement;

public class PieWithLegendEventsIT extends AbstractTBTest {

    private SpanElement lastEvent;
    private SpanElement eventDetails;

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return PieWithLegendEvents.class;
    }

    @Override
    public void setup() throws Exception {
        super.setup();
        lastEvent = $(SpanElement.class).id("lastEvent");
        eventDetails = $(SpanElement.class).id("eventDetails");
    }

    @Test
    public void pointLegendItemClick_withoutModifier_eventIsFired() {
        getLegendElement().click();

        Assert.assertEquals(PointLegendItemClickEvent.class.getSimpleName(),
                lastEvent.getText());
        Assert.assertTrue(
                eventDetails.getText().contains("\"shiftKey\" : false"));
    }

    @Test
    public void pointLegendItemClick_withModifier_eventIsFired() {
        WebElement legend = getLegendElement();
        new Actions(driver).keyDown(Keys.SHIFT).click(legend).keyUp(Keys.SHIFT)
                .perform();
        Assert.assertEquals(PointLegendItemClickEvent.class.getSimpleName(),
                lastEvent.getText());
        Assert.assertTrue(
                eventDetails.getText().contains("\"shiftKey\" : true"));
    }

    private WebElement getLegendElement() {
        return getChartElement().$("*")
                .attributeContains("class", "highcharts-legend-item").first();
    }
}
