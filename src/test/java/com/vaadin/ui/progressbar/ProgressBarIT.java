/*
 * Copyright 2000-2017 Vaadin Ltd.
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

package com.vaadin.ui.progressbar;

import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.demo.ComponentDemoTest;
import com.vaadin.testbench.By;

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
        WebElement progressBar = layout.findElement(By.id("custom-progress-bar"));
        WebElement button = layout.findElement(By.id("progress-button"));
        assertThat(valueOf(progressBar), is("20"));

        scrollIntoViewAndClick(button);

        waitUntil(driver -> valueOf(progressBar).equals("30"));
    }

    private String valueOf(WebElement progressBar) {
        return progressBar.getAttribute("value");
    }

    @Override
    protected String getTestPath() {
        return "/vaadin-progress-bar";
    }
}
