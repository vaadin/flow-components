/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.card.tests;

import java.time.Duration;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.flow.component.card.testbench.CardElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the CardView.
 */
@TestPath("vaadin-card")
public class CardIT extends AbstractComponentIT {

    private TestBenchTestCase layout;

    @Before
    public void init() {
        open();
        layout = this;
        waitForElementPresent(By.tagName("vaadin-card"));
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-card");
    }
}
