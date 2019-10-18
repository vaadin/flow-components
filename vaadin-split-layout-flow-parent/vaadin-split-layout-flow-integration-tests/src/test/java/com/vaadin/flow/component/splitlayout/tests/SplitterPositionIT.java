/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.component.splitlayout.demo.SplitLayoutView;
import com.vaadin.flow.component.splitlayout.test.SplitterPositionView;
import com.vaadin.flow.component.splitlayout.testbench.SplitLayoutElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

/**
 * Integration tests for {@link SplitLayoutView}.
 */
@TestPath("splitter-position")
public class SplitterPositionIT extends AbstractComponentIT {

    @Before
    public void setUp() {
        open();
    }

    @Test
    public void testSplitterPositionJava() {
        testSplitterPosition("JavaApi");
    }

    @Test
    public void testSplitterPositionElement() {
        testSplitterPosition("ElementApi");
    }

    private void testSplitterPosition(String testId) {
        $(NativeButtonElement.class).id("createLayout" + testId).click();
        SplitLayoutElement layout = $(SplitLayoutElement.class)
                .id("splitLayout" + testId);
        TestBenchElement primaryElement = layout.$(SpanElement.class)
                .id("primary" + testId);
        TestBenchElement secondaryElement = layout.$(SpanElement.class)
                .id("secondary" + testId);
        assertElementWidth(primaryElement,
                (int) SplitterPositionView.INITIAL_POSITION + "%");
        assertElementWidth(secondaryElement,
                (int) SplitterPositionView.FINAL_POSITION + "%");

        $(NativeButtonElement.class).id("setSplitPosition" + testId).click();
        assertElementWidth(primaryElement,
                (int) SplitterPositionView.FINAL_POSITION + "%");
        assertElementWidth(secondaryElement,
                (int) SplitterPositionView.INITIAL_POSITION + "%");
    }

    private void assertElementWidth(TestBenchElement element, String expected) {
        executeScript("console.log(arguments[0])", element);
        executeScript("console.log(arguments[0].style)", element);
        executeScript("console.log(arguments[0].style.width)", element);
        Assert.assertEquals(expected,
                element.getPropertyString("style", "width"));
    }

}
