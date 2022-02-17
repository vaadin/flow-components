package com.vaadin.flow.component.charts.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@Element("vaadin-chart")
public class ChartElement extends TestBenchElement {

    public List<TestBenchElement> getPoints() {
        return $(".highcharts-point").all();
    }

    public List<TestBenchElement> getVisiblePoints() {
        return $(":not([visibility=hidden]).highcharts-point").all();
    }

    private List<WebElement> getElementsFromShadowRootBySelector(
            String selector) {
        return getElementsFromShadowRoot(By.cssSelector(selector));
    }

    private List<WebElement> getElementsFromShadowRoot(By by) {
        WebElement shadowRoot = (WebElement) executeScript(
                "return arguments[0].shadowRoot", this);
        assertNotNull("Could not locate shadowRoot in the element", shadowRoot);
        return shadowRoot.findElements(by);
    }
}