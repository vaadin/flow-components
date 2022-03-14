/*
 * Copyright 2000-2022 Vaadin Ltd.
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
 *
 */

package com.vaadin.flow.component.progressbar.tests;

import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
