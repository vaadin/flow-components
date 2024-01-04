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
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-dialog/content")
public class DialogContentView extends HorizontalLayout {

    private Dialog dialog = new Dialog();

    public DialogContentView() {
        var closeButton = new Button("Close dialog", e -> {
            dialog.close();
        });
        closeButton.setId("close-button");

        var addContentButton = new Button("Add content", e -> {
            dialog.add(closeButton);
        });
        addContentButton.setId("add-content-button");

        var openButton = new Button("Open dialog", e -> {
            dialog.open();
        });
        openButton.setId("open-button");

        // It's crucial that the dialog itself is added to the UI for this test
        add(dialog, addContentButton, openButton);
    }
}
