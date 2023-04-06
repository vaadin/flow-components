

package com.vaadin.flow.component.splitlayout.tests;

import com.vaadin.flow.component.splitlayout.test.OrientationChangeView;
import com.vaadin.flow.component.splitlayout.testbench.SplitLayoutElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-split-layout/orientation-change")
public class OrientationChangeIT extends AbstractComponentIT {

    @Before
    public void setUp() {
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
