/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.progressbar.tests;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.progressbar.demo.ProgressBarView;
import com.vaadin.tests.ComponentDemoTest;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

/**
 * Integration tests for the {@link ProgressBarView}.
 *
 * @author Vaadin Ltd.
 */
public class ProgressBarIT extends ComponentDemoTest {

    @Test
    public void clickOnProgressButtonIncrementsProgressBarValue() {
        WebElement progressBar = layout
                .findElement(By.id("custom-progress-bar"));
        WebElement button = layout.findElement(By.id("progress-button"));
        assertThat(valueOf(progressBar), is("20"));

        scrollIntoViewAndClick(button);

        waitUntil(driver -> valueOf(progressBar).equals("30"));
    }

    @Test
    public void assertVariants() {
        verifyThemeVariantsBeingToggled();
    }

    private String valueOf(WebElement progressBar) {
        return progressBar.getAttribute("value");
    }

    @Override
    protected String getTestPath() {
        return "/vaadin-progress-bar";
    }
}
