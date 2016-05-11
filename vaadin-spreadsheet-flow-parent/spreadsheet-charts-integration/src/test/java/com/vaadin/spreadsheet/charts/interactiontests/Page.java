package com.vaadin.spreadsheet.charts.interactiontests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;

/**
 * FIXME copy-pasted from the spreadsheet, please find a way to reuse the same code.
 */
public class Page extends TestBenchTestCase {
    public Page(WebDriver driver) {
        setDriver(driver);
    }

    protected WebElement buttonWithCaption(String caption) {
        return $(ButtonElement.class).caption(caption).first();
    }
}
