/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.contextmenu.demo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.Route;

/**
 * View for {@link ContextMenu} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-context-menu")
public class ContextMenuView extends DemoView {

    @Override
    public void initView() {
        addBasicContextMenu();
        addContextMenuWithComponents();
        addCard("Target component used in the demo");
    }

    private void addBasicContextMenu() {
        // begin-source-example
        // source-example-heading: Basic ContextMenu
        ContextMenu contextMenu = new ContextMenu();

        Component target = createTargetComponent();
        contextMenu.setTarget(target);

        Label message = new Label("-");

        contextMenu.addItem("First menu item",
                e -> message.setText("Clicked on the first item"));

        contextMenu.addItem("Second menu item",
                e -> message.setText("Clicked on the second item"));

        // The created MenuItem component can be saved for later use
        MenuItem item = contextMenu.addItem("Disabled menu item",
                e -> message.setText("This cannot happen"));
        item.setEnabled(false);

        // end-source-example

        addCard("Basic ContextMenu", target, message);
        target.setId("basic-context-menu-target");
        contextMenu.setId("basic-context-menu");
    }

    private void addContextMenuWithComponents() {
        // begin-source-example
        // source-example-heading: ContextMenu With Components
        Component target = createTargetComponent();
        ContextMenu contextMenu = new ContextMenu(target);

        Label message = new Label("-");

        // Components can be used also inside menu items
        contextMenu.addItem(new H5("First menu item"),
                e -> message.setText("Clicked on the first item"));

        Checkbox checkbox = new Checkbox("Checkbox");
        contextMenu.addItem(checkbox, e -> message.setText(
                "Clicked on checkbox with value: " + checkbox.getValue()));

        // Components can also be added to the overlay
        // without creating menu items with add()
        Component horizontalLine = createHorizontalLine();
        contextMenu.add(horizontalLine, new Label("This is not a menu item"));

        // end-source-example

        addCard("ContextMenu With Components", target, message);
        target.setId("context-menu-with-components-target");
        contextMenu.setId("context-menu-with-components");
        message.setId("context-menu-with-components-message");
    }

    // begin-source-example
    // source-example-heading: Target component used in the demo
    private Component createTargetComponent() {
        H2 header = new H2("Right click this component");
        Paragraph paragraph = new Paragraph("(or long touch on mobile)");
        Div div = new Div(header, paragraph);
        div.getStyle().set("border", "1px solid black").set("textAlign",
                "center");
        return div;
    }
    // end-source-example

    private Component createHorizontalLine() {
        Element hr = ElementFactory.createHr();
        return new Component(hr) {
        };
    }
}
