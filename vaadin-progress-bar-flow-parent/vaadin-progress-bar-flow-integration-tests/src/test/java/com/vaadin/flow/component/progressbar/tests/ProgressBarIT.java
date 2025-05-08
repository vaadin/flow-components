/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.progressbar.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.progressbar.testbench.ProgressBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link ProgressBarView}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-progress-bar")
public class ProgressBarIT extends AbstractComponentIT {

    private ProgressBarElement progressBar;

    @Before
    public void init() {
        open();
        progressBar = $(ProgressBarElement.class).waitForFirst();
    }

    @Test
    public void clickOnProgressButtonIncrementsProgressBarValue() {
        Assert.assertEquals(20, progressBar.getValue(), 0);

        findElement(By.id("progress-button")).click();

        Assert.assertEquals(30, progressBar.getValue(), 0);
    }
}
