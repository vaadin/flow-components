/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-dialog/content")
public class DialogContentIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void addContentAndOpenTwice_contentPresent() {
        // Add dialog content (the close button) and open the dialog
        clickElementWithJs("add-content-button");
        clickElementWithJs("open-button");
        waitForElementPresent(
                By.cssSelector("vaadin-dialog-overlay #close-button"));

        // Close the dialog
        clickElementWithJs("open-button");

        // Add the same content again and open the dialog
        clickElementWithJs("add-content-button");
        clickElementWithJs("open-button");
        // Expect the content to be present inside the dialog overlay
        waitForElementPresent(
                By.cssSelector("vaadin-dialog-overlay #close-button"));
    }
}
