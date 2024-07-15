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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-popover/modality")
public class ModalityPage extends Div {
    public ModalityPage() {
        Popover popover = new Popover();
        Div content = new Div("Popover content");
        popover.add(content);

        NativeButton target = new NativeButton("Toggle popover");
        target.setId("popover-target");
        popover.setTarget(target);

        NativeButton setModal = new NativeButton("Set modal",
                e -> popover.setModal(true));
        setModal.setId("set-modal");

        NativeButton setNonModal = new NativeButton("Set non modal",
                e -> popover.setModal(false));
        setNonModal.setId("set-non-modal");

        Span testClickResult = new Span();
        testClickResult.setId("test-click-result");

        NativeButton testClick = new NativeButton("Test click",
                event -> testClickResult.setText("Click event received"));
        testClick.setId("test-click");

        add(new Div(popover, setModal, setNonModal, target));
        add(new Div(testClick, testClickResult));
    }
}
