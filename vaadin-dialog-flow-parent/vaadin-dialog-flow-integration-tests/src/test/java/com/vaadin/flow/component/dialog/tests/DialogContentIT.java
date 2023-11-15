/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.dialog.tests;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

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
