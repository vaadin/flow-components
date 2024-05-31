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
 *
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
