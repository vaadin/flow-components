/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.splitlayout.tests;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import com.vaadin.flow.component.splitlayout.testbench.SplitLayoutElement;
import com.vaadin.testbench.TestBenchElement;

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
