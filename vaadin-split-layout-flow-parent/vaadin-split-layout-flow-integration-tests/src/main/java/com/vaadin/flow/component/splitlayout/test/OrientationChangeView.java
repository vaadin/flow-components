/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
