/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.splitlayout.tests;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.splitlayout.test.OrientationChangeView;
import com.vaadin.flow.component.splitlayout.testbench.SplitLayoutElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-split-layout/orientation-change")
public class OrientationChangeIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void keepSplitterPositionAfterOrientationChange() {
        SplitLayoutElement layout = $(SplitLayoutElement.class)
                .id("splitLayout");
        TestBenchElement primaryComponent = layout.getPrimaryComponent();
        TestBenchElement secondaryComponent = layout.getSecondaryComponent();

        SplitLayoutAssertions.assertChildWidthInPercentage(layout,
                primaryComponent, OrientationChangeView.SPLITTER_POSITION);
        SplitLayoutAssertions.assertChildWidthInPercentage(layout,
                secondaryComponent,
                100 - OrientationChangeView.SPLITTER_POSITION);

        TestBenchElement toggleOrientationButton = $("button")
                .id("toggleOrientationButton");
        toggleOrientationButton.click();

        SplitLayoutAssertions.assertChildHeightInPercentage(layout,
                primaryComponent, OrientationChangeView.SPLITTER_POSITION);
        SplitLayoutAssertions.assertChildHeightInPercentage(layout,
                secondaryComponent,
                100 - OrientationChangeView.SPLITTER_POSITION);
    }
}
