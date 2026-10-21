/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.popover.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/flow-components/issues/10472
 * <p>
 * Variant "inside": popover is a child of the hidden container (reporter's
 * setup). Variant "outside": only the target is inside the hidden container,
 * the popover itself stays visible on the server.
 */
@Route("repro-10472")
public class Repro10472View extends Div {

    public Repro10472View() {
        add(createVariant("inside", true), createVariant("outside", false));
    }

    private Div createVariant(String id, boolean popoverInsideContainer) {
        Div wrapper = new Div();
        wrapper.getStyle().set("border", "1px solid #ccc")
                .set("padding", "10px").set("margin", "10px");

        Div container = new Div();
        container.setId(id + "-container");

        NativeButton target = new NativeButton("target " + id);
        target.setId(id + "-target");

        Span log = new Span();
        log.setId(id + "-log");

        Popover popover = new Popover();
        popover.setId(id + "-popover");
        popover.setTarget(target);
        popover.setOpenOnClick(true);
        popover.setOpenOnHover(true);
        NativeButton hideFromPopover = new NativeButton("hide (server)",
                e -> container.setVisible(false));
        hideFromPopover.setId(id + "-hide-in-popover");
        popover.add(new Span("Popover content " + id), hideFromPopover);
        popover.addOpenedChangeListener(e -> log.setText(log.getText()
                + " | opened=" + e.isOpened() + " fromClient="
                + e.isFromClient()));

        container.add(target);
        if (popoverInsideContainer) {
            container.add(popover);
        } else {
            wrapper.add(popover);
        }

        NativeButton show = new NativeButton("show container (server)",
                e -> container.setVisible(true));
        show.setId(id + "-show");
        NativeButton hide = new NativeButton("hide container (server)",
                e -> container.setVisible(false));
        hide.setId(id + "-hide");
        NativeButton isOpened = new NativeButton("server isOpened()",
                e -> log.setText(log.getText() + " | isOpened()="
                        + popover.isOpened()));
        isOpened.setId(id + "-is-opened");

        wrapper.add(new Div(new Span("Variant: " + id)), container,
                new Div(show, hide, isOpened), log);
        return wrapper;
    }
}
