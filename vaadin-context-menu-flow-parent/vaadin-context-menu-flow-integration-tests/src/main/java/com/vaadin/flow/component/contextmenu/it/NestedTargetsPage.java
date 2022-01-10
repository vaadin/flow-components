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
 */
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-context-menu/nested-targets")
public class NestedTargetsPage extends Div {

    public NestedTargetsPage() {
        Div messages = new Div();
        messages.setId("messages");

        Div parentTarget = new Div();
        Paragraph notInChildTarget = new Paragraph(
                "Element inside parent target");
        notInChildTarget.setId("not-in-child-target");
        Paragraph childTarget = new Paragraph(
                "Child target inside parent target");
        childTarget.setId("child-target");

        parentTarget.add(notInChildTarget, childTarget);

        ContextMenu menuOnParent = new ContextMenu(parentTarget);
        menuOnParent.addItem("menu on parent target",
                e -> messages.add("parent"));

        ContextMenu menuOnChild = new ContextMenu(childTarget);
        menuOnChild.addItem("menu on child target", e -> messages.add("child"));

        add(parentTarget, messages);
    }
}
