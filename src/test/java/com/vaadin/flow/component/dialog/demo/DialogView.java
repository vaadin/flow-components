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
package com.vaadin.flow.component.dialog.demo;

import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Dialog} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-dialog")
@HtmlImport("bower_components/vaadin-valo-theme/vaadin-button.html")
public class DialogView extends DemoView {

    private static final String BUTTON_CAPTION = "Open dialog";

    @Override
    public void initView() {
        addBasicDialog();
        addDialogWithOpenedChangedListener();
        addDialogWithHTML();
    }

    private void addBasicDialog() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        // begin-source-example
        // source-example-heading: Basic dialog
        Dialog dialog = new Dialog("Hello World!");
        button.addClickListener(event -> dialog.open());
        // end-source-example

        button.setId("basic-dialog-button");
        addCard("Basic dialog", button, dialog);
    }

    private void addDialogWithOpenedChangedListener() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        Label message = new Label();
        Dialog dialog = new Dialog("Hello World!");

        // begin-source-example
        // source-example-heading: Dialog with an OpenedChangedListener
        dialog.addOpenedChangeListener(event -> {
            if (dialog.isOpened()) {
                message.setText("Dialog opened!");
            } else {
                message.setText("Dialog closed!");
            }
        });
        // end-source-example

        button.addClickListener(event -> dialog.open());
        button.setId("dialog-with-listener-button");
        message.setId("dialog-message-label");
        addCard("Dialog with an OpenedChangedListener",
                new HorizontalLayout(button, message), dialog);
    }

    private void addDialogWithHTML() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        // begin-source-example
        // source-example-heading: Dialog with HTML content
        Dialog dialog = new Dialog("<b>Dialog can be closed by:</b><ul>"
                + "<li>Hitting the esc key</li>"
                + "<li>Clicking outside of it</li></ul>");
        // end-source-example

        button.addClickListener(event -> dialog.open());
        button.setId("dialog-with-html-button");
        addCard("Dialog with HTML content", button, dialog);
    }
}
