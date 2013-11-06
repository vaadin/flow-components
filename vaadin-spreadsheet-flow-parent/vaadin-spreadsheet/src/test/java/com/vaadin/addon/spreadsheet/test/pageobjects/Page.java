package com.vaadin.addon.spreadsheet.test.pageobjects;

import com.vaadin.testbench.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Page {
    protected WebDriver driver;

    public Page(WebDriver driver) {
        this.driver = driver;
    }

    protected WebElement buttonWithCaption(String caption) {
        return driver.findElement(By.xpath(String.format("//span[@class='v-button-caption' and contains(text(), '%s')]", caption)));
    }
}
