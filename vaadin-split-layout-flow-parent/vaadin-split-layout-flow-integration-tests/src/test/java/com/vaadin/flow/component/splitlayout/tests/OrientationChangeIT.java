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
