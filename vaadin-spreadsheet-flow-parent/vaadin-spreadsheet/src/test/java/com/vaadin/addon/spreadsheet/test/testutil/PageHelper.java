package com.vaadin.addon.spreadsheet.test.testutil;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.By;

public class PageHelper extends SeleniumHelper {

    public PageHelper(WebDriver driver) {
        super(driver);
    }

    public void assertTextPresent(String text) {
        List<WebElement> list = driver.findElements(By
                .xpath("//*[contains(text(),'" + text + "')]"));
        Assert.assertTrue("'" + text + "' not found in page!", list.size() > 0);
    }

    public void assertTextPresent(String[] textOptions) {
        boolean found = false;
        for (String text : textOptions) {
            List<WebElement> list = driver.findElements(By
                    .xpath("//*[contains(text(),'" + text + "')]"));
            if (list.size() > 0) {
                found = true;
                break;
            }
        }
        Assert.assertTrue("None of texts '" + Arrays.toString(textOptions)
                + "' found in page!", found);
    }

    public void assertUrlContains(String expectedUrl) {
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue("Current URL expectation failed: expected '"
                + expectedUrl + "' is not contained in '" + currentUrl + "'",
                currentUrl.contains(expectedUrl));
    }

}
