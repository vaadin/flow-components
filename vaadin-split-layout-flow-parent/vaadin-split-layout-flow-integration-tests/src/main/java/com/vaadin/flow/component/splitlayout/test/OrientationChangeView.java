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

package com.vaadin.flow.component.splitlayout.test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-split-layout/orientation-change")
public class OrientationChangeView extends Div {
    public static final double SPLITTER_POSITION = 70;

    public OrientationChangeView() {
        Span primary = new Span("Primary");
        Span secondary = new Span("Secondary");
        SplitLayout layout = new SplitLayout(primary, secondary);
        layout.setWidth("400px");
        layout.setHeight("200px");
        layout.setSplitterPosition(SPLITTER_POSITION);
        layout.setId("splitLayout");

        NativeButton toggleOrientationButton = new NativeButton(
                "Toggle orientation",
                e -> layout.setOrientation(layout
                        .getOrientation() == SplitLayout.Orientation.HORIZONTAL
                                ? SplitLayout.Orientation.VERTICAL
                                : SplitLayout.Orientation.HORIZONTAL));
        toggleOrientationButton.setId("toggleOrientationButton");

        add(new H1("Orientation change"), layout, toggleOrientationButton);
    }
}
