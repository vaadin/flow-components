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

import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.splitlayout.test.SplitLayoutView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.component.splitlayout.test.SplitterPositionView;
import com.vaadin.flow.component.splitlayout.testbench.SplitLayoutElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.openqa.selenium.interactions.Actions;

import java.util.function.Consumer;

/**
 * Integration tests for {@link SplitLayoutView}.
 */
@TestPath("vaadin-split-layout/splitter-position")
public class SplitterPositionIT extends AbstractComponentIT {

    public static final String JAVA_API = "JavaApi";
    public static final String ELEMENT_API = "ElementApi";

    @Before
    public void setUp() {
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

    private void testSplitterPosition(String testId) {
        testSplitterPosition(testId, splitLayoutElement -> {
        });
    }

    private void testSplitterPositionAfterDrag(String testId) {
        testSplitterPosition(testId, layout -> {
            new Actions(getDriver()).dragAndDropBy(layout.getSplitter(), 20, 0)
                    .perform();
            Assert.assertNotEquals("", getPrimaryElement(layout, testId)
                    .getPropertyString("style", "flex"));
            Assert.assertNotEquals("", getSecondaryElement(layout, testId)
                    .getPropertyString("style", "flex"));
        });
    }

    private void testSplitterPosition(String testId,
            Consumer<SplitLayoutElement> modifyState) {
        $(NativeButtonElement.class).id("createLayout" + testId).click();
        SplitLayoutElement layout = $(SplitLayoutElement.class)
                .id("splitLayout" + testId);
        TestBenchElement primaryElement = getPrimaryElement(layout, testId);
        TestBenchElement secondaryElement = getSecondaryElement(layout, testId);
        SplitLayoutAssertions.assertChildWidthInPercentage(layout,
                primaryElement, SplitterPositionView.INITIAL_POSITION);
        SplitLayoutAssertions.assertChildWidthInPercentage(layout,
                secondaryElement, SplitterPositionView.FINAL_POSITION);

        modifyState.accept(layout);
        $(NativeButtonElement.class).id("setSplitPosition" + testId).click();

        SplitLayoutAssertions.assertChildWidthInPercentage(layout,
                primaryElement, SplitterPositionView.FINAL_POSITION);
        SplitLayoutAssertions.assertChildWidthInPercentage(layout,
                secondaryElement, SplitterPositionView.INITIAL_POSITION);
    }

    private TestBenchElement getPrimaryElement(SplitLayoutElement layout,
            String testId) {
        return layout.$(SpanElement.class).id("primary" + testId);
    }

    private TestBenchElement getSecondaryElement(SplitLayoutElement layout,
            String testId) {
        return layout.$(SpanElement.class).id("secondary" + testId);
    }
}
