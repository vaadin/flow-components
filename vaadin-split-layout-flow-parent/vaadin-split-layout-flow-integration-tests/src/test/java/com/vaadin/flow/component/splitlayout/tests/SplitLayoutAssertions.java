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

package com.vaadin.flow.component.splitlayout.tests;

import com.vaadin.flow.component.splitlayout.testbench.SplitLayoutElement;
import com.vaadin.testbench.TestBenchElement;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

class SplitLayoutAssertions {

    // Margin of error to account for rounding errors
    private static final int MARGIN = 2;

    public static void assertChildWidthInPercentage(
            SplitLayoutElement layoutElement, TestBenchElement child,
            double percentage) {
        int layoutWidth = layoutElement.getSize().getWidth();
        int splitterWidth = layoutElement.getSplitter().getSize().getWidth();
        int childWidth = child.getSize().getWidth();
        int expectedWidth = (int) Math
                .round((layoutWidth - splitterWidth) * percentage / 100);

        assertThat(childWidth,
                allOf(greaterThanOrEqualTo(expectedWidth - MARGIN),
                        lessThanOrEqualTo(expectedWidth + MARGIN)));
    }

    public static void assertChildHeightInPercentage(
            SplitLayoutElement layoutElement, TestBenchElement child,
            double percentage) {
        int layoutHeight = layoutElement.getSize().getHeight();
        int splitterHeight = layoutElement.getSplitter().getWrappedElement()
                .getSize().getHeight();
        int childHeight = child.getSize().getHeight();
        int expectedHeight = (int) Math
                .round((layoutHeight - splitterHeight) * percentage / 100);

        assertThat(childHeight,
                allOf(greaterThanOrEqualTo(expectedHeight - MARGIN),
                        lessThanOrEqualTo(expectedHeight + MARGIN)));
    }
}
