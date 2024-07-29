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
package com.vaadin.flow.component.splitlayout.tests;

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.component.splitlayout.test.SplitLayoutView;
import com.vaadin.flow.component.splitlayout.testbench.SplitLayoutElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for {@link SplitLayoutView}.
 */
@TestPath("vaadin-split-layout/splitter-position")
public class SplitterPositionIT extends AbstractComponentIT {

    public static final String JAVA_API = "JavaApi";
    public static final String ELEMENT_API = "ElementApi";

    @Before
    public void init() {
        open();
    }

    @Test
    public void testSplitterPositionJava() {
        testSplitterPosition(JAVA_API);
    }

    @Test
    public void testSplitterPositionElement() {
        testSplitterPosition(ELEMENT_API);
    }

    @Test
    public void testSplitterPositionAfterDragJava() {
        testSplitterPositionAfterDrag(JAVA_API);
    }

    @Test
    public void testSplitterPositionAfterDragElement() {
        testSplitterPositionAfterDrag(ELEMENT_API);
    }

    // Issue https://github.com/vaadin/vaadin-split-layout-flow/issues/75
    @Test
    public void testSplitterContentCanHaveWidthChanged() {
        $(NativeButtonElement.class).id("createLayoutComponent").click();
        final TestBenchElement toggleButton = $(TestBenchElement.class)
                .id("toggleButtonInLayoutComponent");
        toggleButton.click();
        toggleButton.click();
        final String width = $(DivElement.class)
                .id("mainContentInLayoutComponent")
                .getPropertyString("style", "width");
        Assert.assertEquals("100%", width);
    }

    @Test
    public void nestedSplitLayouts_moveParentSplitter_getSplitterPosition() {
        $(NativeButtonElement.class).id("nested-split-layout-button").click();
        var split = $(SplitLayoutElement.class).id("parent-layout");
        WebElement splitter = (WebElement) executeScript(
                "return arguments[0].$.splitter", split);

        // Move splitter by 150px
        Actions resizeAction = new Actions(getDriver());
        resizeAction.dragAndDropBy(splitter, 0, 150);
        resizeAction.perform();

        $(NativeButtonElement.class).id("splitter-position-button").click();
        var splitPosition = Double.parseDouble(
                $(TestBenchElement.class).id("splitter-position").getText());

        // The split layout has 500px height, so the splitter is about at 250px
        // Moving it 150px down, it would be at 400px (around 80% of the layout)
        Assert.assertTrue(splitPosition > 80);
        Assert.assertTrue(splitPosition < 81);
    }

    @Test
    public void setSplitterPositionFromServer_moveOnClient_resetToOriginal() {
        // Add split layout with 30% splitter position
        $(NativeButtonElement.class).id("createLayoutJavaApi").click();
        $(NativeButtonElement.class).id("setSplitPositionJavaApi").click();

        var split = $(SplitLayoutElement.class).id("splitLayoutJavaApi");
        var primaryComponent = split.getPrimaryComponent();

        var flexBasisInitial = primaryComponent.getCssValue("flex-basis");

        // Move splitter by 150px
        var splitter = split.getSplitter();
        Actions resizeAction = new Actions(getDriver());
        resizeAction.dragAndDropBy(splitter, 150, 0);
        resizeAction.perform();

        // Check that the splitter position is not 30% anymore
        var flexBasisAfterDrag = primaryComponent.getCssValue("flex-basis");
        Assert.assertNotEquals(flexBasisInitial, flexBasisAfterDrag);
        // Check that splitter position still is in px
        Assert.assertTrue(flexBasisAfterDrag.endsWith("px"));

        // Reset splitter position to 30%
        $(NativeButtonElement.class).id("setSplitPositionJavaApi").click();

        // Check that the splitter is at 30%
        var flexBasisFinal = primaryComponent.getCssValue("flex-basis");
        Assert.assertEquals(flexBasisInitial, flexBasisFinal);
    }

    private void testSplitterPosition(String testId) {
        testSplitterPosition(testId, splitLayoutElement -> {
        });
    }

    private void testSplitterPositionAfterDrag(String testId) {
        testSplitterPosition(testId, layout -> {
            new Actions(getDriver()).dragAndDropBy(layout.getSplitter(), 20, 0)
                    .perform();
            Assert.assertNotEquals("",
                    getShowSplitterPositionElement(testId).getText());
        });
    }

    private void testSplitterPosition(String testId,
            Consumer<SplitLayoutElement> modifyState) {
        $(NativeButtonElement.class).id("createLayout" + testId).click();
        SplitLayoutElement layout = $(SplitLayoutElement.class)
                .id("splitLayout" + testId);

        Assert.assertEquals("",
                getShowSplitterPositionElement(testId).getText());

        modifyState.accept(layout);
        $(NativeButtonElement.class).id("setSplitPosition" + testId).click();
    }

    private SpanElement getShowSplitterPositionElement(String testId) {
        return $(SpanElement.class).id("showSplitterPosition" + testId);
    }
}
