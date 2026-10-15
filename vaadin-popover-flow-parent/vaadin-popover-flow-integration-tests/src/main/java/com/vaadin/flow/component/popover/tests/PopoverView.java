/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.popover.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.router.Route;

@Route("vaadin-popover")
public class PopoverView extends Div {

    public PopoverView() {
        Popover popover = new Popover();
        Div content = new Div("Popover content");
        content.setId("popover-content");
        popover.add(content);

        NativeButton target = new NativeButton("Toggle popover");
        target.setId("popover-target");
        popover.setTarget(target);

        NativeButton clearTarget = new NativeButton("Clear target",
                event -> popover.setTarget(null));
        clearTarget.setId("clear-target");

        NativeButton detachTarget = new NativeButton("Detach target",
                event -> remove(target));
        detachTarget.setId("detach-target");

        NativeButton attachTarget = new NativeButton("Attach target",
                event -> add(target));
        attachTarget.setId("attach-target");

        NativeButton disableCloseOnEsc = new NativeButton(
                "Disable close on Esc", event -> popover.setCloseOnEsc(false));
        disableCloseOnEsc.setId("disable-close-on-esc");

        NativeButton disableCloseOnOutsideClick = new NativeButton(
                "Disable close on outside click",
                event -> popover.setCloseOnOutsideClick(false));
        disableCloseOnOutsideClick.setId("disable-close-on-outside-click");

        add(popover, clearTarget, detachTarget, attachTarget, disableCloseOnEsc,
                disableCloseOnOutsideClick, target);
    }
}
