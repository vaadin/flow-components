package com.vaadin.addon.spreadsheet.test.pageobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;

public class Page extends TestBenchTestCase {
    public Page(WebDriver driver) {
        setDriver(driver);
    }

    protected WebElement buttonWithCaption(String caption) {
        return $(ButtonElement.class).caption(caption).first();
    }
}
