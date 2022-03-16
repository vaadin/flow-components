package com.vaadin.flow.component.charts.tests;

import com.google.gson.*;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.dynamic.ServerSideEvents;
import com.vaadin.flow.component.charts.events.*;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class ServerSideEventsIT extends AbstractTBTest {

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        resetHistory();
    }

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return ServerSideEvents.class;
    }

    @Test
    public void chartClick_occured_eventIsFired() {
        new Actions(driver).moveByOffset(200, 200).click().build().perform();

        assertLastEventIsType(ChartClickEvent.class);
    }

    @Test
    public void pointClick_occured_eventIsFired() {
        WebElement firstMarker = findLastDataPointOfTheFirstSeries();

        firstMarker.click();

        assertHasEventOfType(PointClickEvent.class);
    }

    @Test
    public void legendItemClick_occuredWhileVisibilityTogglingDisabled_eventIsFired() {
        WebElement disableVisibilityToggling = findDisableVisibityToggle();
        disableVisibilityToggling.click();
        WebElement legendItem = findLegendItem();

        legendItem.click();

        assertHasEventOfType(SeriesLegendItemClickEvent.class);
    }

    @Test
    public void seriesLegendItemClick_withoutModifier_eventIsFired() {
        WebElement disableVisibilityToggling = findDisableVisibityToggle();
        disableVisibilityToggling.click();
        WebElement legend = findLegendItem();

        legend.click();

        HistoryEvent event = findHistoryEventOfType(
                SeriesLegendItemClickEvent.class);
        Assert.assertNotNull(event);
        Assert.assertFalse(
                event.as(SeriesLegendItemClickEvent.class).isShiftKey());
    }

    @Test
    public void seriesLegendItemClick_withModifier_eventIsFired() {
        WebElement disableVisibilityToggling = findDisableVisibityToggle();
        disableVisibilityToggling.click();
        WebElement legend = findLegendItem();

        new Actions(driver).keyDown(Keys.SHIFT).click(legend).keyUp(Keys.SHIFT)
                .perform();

        HistoryEvent event = findHistoryEventOfType(
                SeriesLegendItemClickEvent.class);
        Assert.assertNotNull(event);
        Assert.assertTrue(
                event.as(SeriesLegendItemClickEvent.class).isShiftKey());
    }

    @Test
    public void legendItemClick_occuredWhileVisibilityTogglingEnabled_eventAndSeriesHideEventAreFired() {
        WebElement legendItem = findLegendItem();

        legendItem.click();

        assertHasEventOfType(SeriesHideEvent.class);
        assertHasEventOfType(SeriesLegendItemClickEvent.class);
    }

    @Test
    public void checkBoxClick_occured_eventIsFired() {
        WebElement checkBox = findCheckBox();

        checkBox.click();

        assertHasEventOfType(SeriesCheckboxClickEvent.class);
    }

    @Test
    public void checkBoxClick_secondCheckboxClicked_secondSeriesIsReturned() {
        WebElement secondCheckBox = findSecondCheckbox();

        secondCheckBox.click();

        HistoryEvent event = findHistoryEventOfType(
                SeriesCheckboxClickEvent.class);
        Assert.assertNotNull(event);
        Assert.assertEquals(1,
                event.as(SeriesCheckboxClickEvent.class).getSeriesItemIndex());
    }

    @Test
    public void checkBoxClick_seriesWasNotSelected_checkBoxIsChecked() {
        WebElement secondCheckBox = findSecondCheckbox();

        secondCheckBox.click();

        HistoryEvent event = findHistoryEventOfType(
                SeriesCheckboxClickEvent.class);
        Assert.assertNotNull(event);
        Assert.assertTrue(event.as(SeriesCheckboxClickEvent.class).isChecked());
    }

    @Test
    public void hideSeries_occuredFromLegendClick_eventIsFired() {
        WebElement legendItem = findLegendItem();

        legendItem.click();

        assertHasEventOfType(SeriesHideEvent.class);
    }

    @Test
    public void hideSeries_occuredFromServer_eventIsFired() {
        WebElement hideSeries = findHideFirstSeriesButton();

        hideSeries.click();

        assertHasEventOfType(SeriesHideEvent.class);
    }

    @Test
    public void showSeries_occuredFromLegendClick_eventIsFired() {
        WebElement legendItem = findLegendItem();
        legendItem.click();

        legendItem.click();

        assertHasEventOfType(SeriesShowEvent.class);
    }

    @Test
    public void showSeries_occuredFromServer_eventIsFired() {
        WebElement hideSeriesToggle = findHideFirstSeriesButton();
        hideSeriesToggle.click();

        hideSeriesToggle.click();

        assertHasEventOfType(SeriesShowEvent.class);
    }

    @Test
    public void unselect_occured_eventIsFired() {
        WebElement lastDataPointOfTheFirstSeries = findLastDataPointOfTheFirstSeries();

        lastDataPointOfTheFirstSeries.click();

        assertLastEventIsType(PointUnselectEvent.class);
    }

    @Test
    public void select_occured_eventIsFired() {
        ChartElement chart = getChartElement();
        List<TestBenchElement> points = chart.$(".highcharts-point").all();
        points.get(1).click();

        assertHasEventOfType(PointSelectEvent.class);
    }

    @Test
    public void toggle_extremes_eventIsFired() {
        WebElement toggleExtremesButton = findToggleButton();
        toggleExtremesButton.click();

        assertHasEventOfType(YAxesExtremesSetEvent.class);
    }

    private HistoryEvent findHistoryEventOfType(
            Class<? extends ComponentEvent<Chart>> expectedEvent) {
        List<HistoryEvent> historyEvents = getHistoryEvents();
        String expectedEventType = expectedEvent.getSimpleName();

        return historyEvents.stream()
                .filter(event -> expectedEventType.equals(event.eventType))
                .findFirst().orElse(null);
    }

    private void assertLastEventIsType(
            Class<? extends ComponentEvent<Chart>> expectedEvent) {
        getCommandExecutor().waitForVaadin();
        List<HistoryEvent> historyEvents = getHistoryEvents();

        Assert.assertTrue("History should have events",
                historyEvents.size() > 0);

        HistoryEvent firstEvent = historyEvents.get(historyEvents.size() - 1);

        Assert.assertEquals(expectedEvent.getSimpleName(),
                firstEvent.eventType);
    }

    private void assertHasEventOfType(
            Class<? extends ComponentEvent<Chart>> expectedEvent) {
        getCommandExecutor().waitForVaadin();
        List<HistoryEvent> historyEvents = getHistoryEvents();

        Assert.assertTrue("History should have events",
                historyEvents.size() > 0);

        String expectedEventType = expectedEvent.getSimpleName();
        HistoryEvent searchedEvent = findHistoryEventOfType(expectedEvent);

        Assert.assertNotNull(
                "History does not contain event of type: " + expectedEventType,
                searchedEvent);
    }

    private void resetHistory() {
        waitUntil(e -> $(ButtonElement.class).exists());
        WebElement resetHistoryButton = $(ButtonElement.class)
                .id("resetHistory");
        resetHistoryButton.click();
    }

    private WebElement findHideFirstSeriesButton() {
        return $(ButtonElement.class).waitForFirst();
    }

    private WebElement findLastDataPointOfTheFirstSeries() {
        return getElementFromShadowRoot(getChartElement(),
                ".highcharts-markers > path");
    }

    private WebElement findLegendItem() {
        return getChartElement().$("*")
                .attributeContains("class", "highcharts-legend-item").first();
    }

    private WebElement findCheckBox() {
        return findCheckBox(0);
    }

    private WebElement findSecondCheckbox() {
        return findCheckBox(1);
    }

    private WebElement findCheckBox(int index) {
        return getChartElement().$("input")
                .attributeContains("type", "checkbox").get(index);
    }

    private WebElement findDisableVisibityToggle() {
        return $(CheckboxElement.class).id("visibilityToggler");
    }

    private WebElement findToggleButton() {
        return $(ButtonElement.class).id("toggleExtremes");
    }

    private static class DataSeriesDeserializer
            implements JsonDeserializer<Series> {
        @Override
        public Series deserialize(JsonElement series, Type type,
                JsonDeserializationContext jdc) throws JsonParseException {
            return new Gson().fromJson(series, DataSeries.class);
        }
    }

    private List<HistoryEvent> getHistoryEvents() {
        TestBenchElement historyLayout = $(TestBenchElement.class)
                .id("history");
        List<TestBenchElement> historyItems = historyLayout.$("li").all();

        return historyItems.stream().map(item -> {
            TestBenchElement eventTypeSpan = item.$(TestBenchElement.class)
                    .id("event-type");
            TestBenchElement eventDetailsSpan = item.$(TestBenchElement.class)
                    .id("event-details");
            String eventType = eventTypeSpan.getText();
            String eventDetailsJson = eventDetailsSpan.getText();

            return new HistoryEvent(eventType, eventDetailsJson);
        }).collect(Collectors.toList());
    }

    private static class HistoryEvent {
        String eventType;
        String eventDetailsJson;

        public HistoryEvent(String eventType, String eventDetailsJson) {
            this.eventType = eventType;
            this.eventDetailsJson = eventDetailsJson;
        }

        public <T> T as(Class<T> clazz) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Series.class,
                    new DataSeriesDeserializer()).create();

            return gson.fromJson(this.eventDetailsJson, clazz);
        }
    }
}
