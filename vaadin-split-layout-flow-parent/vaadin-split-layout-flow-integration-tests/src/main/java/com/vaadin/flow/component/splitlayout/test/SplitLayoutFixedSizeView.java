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
package com.vaadin.flow.component.splitlayout.test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-split-layout/fixed-size")
public class SplitLayoutFixedSizeView extends Div {

    public SplitLayoutFixedSizeView() {
        Span primary = new Span("Primary");
        primary.setId("primary");

        Span secondary = new Span("Secondary");
        secondary.setId("secondary");

        SplitLayout layout = new SplitLayout(primary, secondary);

        NativeButton setPrimaryWidth = new NativeButton(
                "Set primary width 250px",
                event -> layout.getPrimaryComponent().setWidth("250px"));

        NativeButton setSecondaryWidth = new NativeButton(
                "Set secondary width 250px",
                event -> layout.getSecondaryComponent().setWidth("250px"));

        add(layout, setPrimaryWidth, setSecondaryWidth);
    }
}
