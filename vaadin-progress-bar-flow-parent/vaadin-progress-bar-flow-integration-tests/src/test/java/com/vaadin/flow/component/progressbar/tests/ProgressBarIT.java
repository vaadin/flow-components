/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.progressbar.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link ProgressBarView}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-progress-bar")
public class ProgressBarIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void clickOnProgressButtonIncrementsProgressBarValue() {
        WebElement progressBar = findElement(By.id("custom-progress-bar"));
        WebElement button = findElement(By.id("progress-button"));
        assertThat(valueOf(progressBar), is("20"));

        scrollIntoViewAndClick(button);

        waitUntil(driver -> valueOf(progressBar).equals("30"));
    }

    @Test
    public void assertVariants() {
        WebElement progressBar = findElement(
                By.id("progress-bar-theme-variant"));
        scrollToElement(progressBar);

        Assert.assertEquals(ProgressBarVariant.LUMO_ERROR.getVariantName(),
                progressBar.getAttribute("theme"));

        findElement(By.id("remove-theme-variant-button")).click();
        Assert.assertNull(progressBar.getAttribute("theme"));
    }

    private String valueOf(WebElement progressBar) {
        return progressBar.getAttribute("value");
    }
}
