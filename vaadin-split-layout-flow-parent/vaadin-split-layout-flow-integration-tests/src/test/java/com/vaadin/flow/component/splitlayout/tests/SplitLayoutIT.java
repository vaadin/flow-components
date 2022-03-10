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
 */
package com.vaadin.flow.component.splitlayout.tests;

import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.component.splitlayout.test.SplitLayoutView;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.commands.TestBenchCommandExecutor;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Integration tests for {@link SplitLayoutView}.
 */
@TestPath("vaadin-split-layout/split-layout")
public class SplitLayoutIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void combined_layouts() {
        WebElement splitLayout = findElement(By.id("split-layout-combination"));
        WebElement firstComponent = splitLayout
                .findElement(By.id("first-component"));
        WebElement nestedLayout = splitLayout
                .findElement(By.id("nested-layout"));
        WebElement secondComponent = nestedLayout
                .findElement(By.id("second-component"));
        WebElement thirdComponent = nestedLayout
                .findElement(By.id("third-component"));

        Assert.assertTrue("First component on the left",
                firstComponent.getLocation().x < secondComponent.getLocation().x
                        && firstComponent.getLocation().x < thirdComponent
                                .getLocation().x);
        Assert.assertTrue("Second component above third component",
                secondComponent.getLocation().y < thirdComponent
                        .getLocation().y);
    }

    @Test
    @Ignore // Due to drag and drop issues with selenium.
    public void resize_events_fired() {
        WebElement splitLayout = findElement(By.id("split-layout-resize"));
        WebElement resizeMessage = findElement(By.id("resize-message"));

        WebElement splitter = new TestBenchWrapper(splitLayout,
                getCommandExecutor()).$("*").id("splitter")
                        .findElement(By.tagName("div"));

        new Actions(getDriver()).dragAndDropBy(splitter, 1, 1).clickAndHold()
                .moveByOffset(200, 0).release().build().perform();

        Assert.assertTrue("Resize events fired", resizeMessage.getText()
                .matches("SplitLayout Resized 1 times."));

        new Actions(getDriver()).dragAndDropBy(splitter, 1, 1).clickAndHold()
                .moveByOffset(200, 0).release().build().perform();

        Assert.assertTrue("Resize events fired", resizeMessage.getText()
                .matches("SplitLayout Resized 2 times."));
    }

    @Test
    public void initial_splitter_position() {
        WebElement primaryComponent = findElement(
                By.id("initial-sp-first-component"));
        WebElement secondaryComponent = findElement(
                By.id("initial-sp-second-component"));

        Assert.assertTrue("Primary component should take up ~80% space",
                Math.abs(((float) primaryComponent.getSize().width)
                        / ((float) secondaryComponent.getSize().width)
                        - 4) < 0.1);
    }

    @Test
    @Ignore // Due to drag and drop issues with selenium.
    public void min_and_max_width_splitter() {
        WebElement splitLayout = findElement(By.id("split-layout-min-max"));
        WebElement splitter = new TestBenchWrapper(splitLayout,
                getCommandExecutor()).$("*").id("splitter")
                        .findElement(By.tagName("div"));
        WebElement primaryComponent = findElement(
                By.id("min-max-first-component"));

        new Actions(getDriver()).moveToElement(splitter, 1, 1).clickAndHold()
                .moveByOffset(-1000, 0).release().build().perform();

        Assert.assertEquals("Primary component width should be 100", 100,
                primaryComponent.getSize().width);

        new Actions(getDriver()).moveToElement(splitter, 1, 1).clickAndHold()
                .moveByOffset(2000, 0).release().build().perform();

        Assert.assertEquals("Primary component width should be 150", 150,
                primaryComponent.getSize().width);
    }

    @Test
    public void assertVariants() {
        WebElement splitLayout = findElement(
                By.id("split-layout-theme-variant"));
        scrollToElement(splitLayout);
        Assert.assertEquals(SplitLayoutVariant.LUMO_SMALL.getVariantName(),
                splitLayout.getAttribute("theme"));

        findElement(By.id("remove-variant-button")).click();
        Assert.assertNull(splitLayout.getAttribute("theme"));
    }

    @Element("*")
    public static class TestBenchWrapper extends TestBenchElement {
        public TestBenchWrapper() {
            // needed for creating instances inside TB
        }

        // used to convert in streams
        TestBenchWrapper(WebElement item,
                TestBenchCommandExecutor commandExecutor) {
            super(item, commandExecutor);
        }
    }
}
