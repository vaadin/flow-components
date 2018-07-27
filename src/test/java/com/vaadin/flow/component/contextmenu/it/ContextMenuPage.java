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
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * Test view for {@link ContextMenu}.
 */
@Theme(Lumo.class)
@Route("context-menu-test")
public class ContextMenuPage extends Div {

    public ContextMenuPage() {
        addContextMenuWithOpenedChangeListener();
        addSeparator();
        addContextMenuWithControls();
        addSeparator();
        createContextMenuAndAddComponentAtIndex();
    }

    private void createContextMenuAndAddComponentAtIndex() {
        Label target = new Label(
                "Target for context menu with opened change listener");
        target.setId("context-menu-add-component-target");
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(target);

        contextMenu.setId("menu-add-component-at-index");
        contextMenu.add(new NativeButton(), new NativeButton(),
                new NativeButton());

        NativeButton addedButton = new NativeButton("Added Button");
        addedButton.setId("added-button");
        NativeButton addFirst = new NativeButton("Add to the first", event -> {
            contextMenu.addComponentAsFirst(addedButton);
        });
        addFirst.setId("button-to-first");

        NativeButton addAtSecond = createTestButton(contextMenu, addedButton,
                "button-to-second", 1);

        NativeButton addOverIndex = createTestButton(contextMenu, addedButton,
                "button-over-index", 10);

        NativeButton addNegativeIndex = createTestButton(contextMenu,
                addedButton,
                "button-negative-index", -10);

        add(target, addFirst, addAtSecond, addOverIndex, addNegativeIndex);
    }

    private NativeButton createTestButton(ContextMenu menu,
            NativeButton addedButton, String buttonId, int index) {
        NativeButton button = new NativeButton(buttonId, event -> {
            menu.addComponentAtIndex(index, addedButton);
        });
        button.setId(buttonId);
        return button;
    }

    private void addSeparator() {
        getElement().appendChild(new Element("hr"));
    }

    private void addContextMenuWithOpenedChangeListener() {
        Label target = new Label(
                "Target for context menu with opened change listener");
        target.setId("context-menu-test");
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(target);
        Paragraph content = new Paragraph("Context menu test.");
        contextMenu.add(content);

        Div message = new Div();
        message.setId("message");

        contextMenu.addOpenedChangeListener(event -> {
            message.setText("The open state of the context menu is "
                    + contextMenu.isOpened());
        });

        add(target, contextMenu, message);
    }

    private void addContextMenuWithControls() {
        ContextMenu contextMenu = new ContextMenu();
        Label target = new Label(
                "Target for context menu with setOpenOnClick() and setTarget()");
        target.setId("context-menu-with-controls");
        contextMenu.setTarget(target);

        Paragraph content = new Paragraph("Context menu With SetOpenOnClick.");
        contextMenu.add(content);

        Div message = new Div();
        message.setId("message-on-click");
        message.setText("Current state is " + contextMenu.isOpenOnClick());

        NativeButton on = new NativeButton("setOpenOnClick");
        on.setId("on");
        on.addClickListener(event -> {
            contextMenu.setOpenOnClick(true);
            message.setText("Current state is " + contextMenu.isOpenOnClick());
        });

        NativeButton off = new NativeButton("setOpenOnClick-off");
        off.setId("off");
        off.addClickListener(event -> {
            contextMenu.setOpenOnClick(false);
            message.setText("Current state is " + contextMenu.isOpenOnClick());
        });

        Label altTarget = new Label("Alternative target");
        altTarget.setId("alt-target");

        NativeButton changeTarget = new NativeButton("Change target",
                event -> contextMenu.setTarget(altTarget));
        changeTarget.setId("change-target");

        NativeButton removeTarget = new NativeButton("Remove target",
                event -> contextMenu.setTarget(null));
        removeTarget.setId("remove-target");

        add(contextMenu, target, altTarget, message, on, off, changeTarget,
                removeTarget);
    }

}
