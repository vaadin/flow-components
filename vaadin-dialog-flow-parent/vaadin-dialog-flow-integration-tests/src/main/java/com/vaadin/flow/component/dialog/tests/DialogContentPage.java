/*
 * Copyright 2023 Vaadin Ltd.
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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-dialog/dialog-content")
public class DialogContentPage extends Div {

    public DialogContentPage() {
        Span logs = new Span();
        logs.setId("logs");

        Text defaultContent = new Text("Default content");
        Text extraContent = new Text("Extra content");

        Dialog dialog = new Dialog();
        dialog.add(defaultContent);

        NativeButton headerButton = new NativeButton("Header button",
                event -> logs.setText("Header button clicked"));
        headerButton.setId("header-button");
        dialog.getHeader().add(headerButton);

        NativeButton footerButton = new NativeButton("Footer button",
                event -> logs.setText("Footer button clicked"));
        footerButton.setId("footer-button");
        dialog.getFooter().add(footerButton);

        NativeButton addExtraContent = new NativeButton("Add extra content",
                event -> dialog.add(extraContent));
        addExtraContent.setId("add-extra-content");

        NativeButton removeExtraContent = new NativeButton(
                "Remove extra content", event -> dialog.remove(extraContent));
        removeExtraContent.setId("remove-extra-content");

        NativeButton removeDefaultContent = new NativeButton(
                "Remove default content",
                event -> dialog.remove(defaultContent));
        removeDefaultContent.setId("remove-default-content");

        NativeButton removeAllContent = new NativeButton("Remove all content",
                event -> dialog.removeAll());
        removeAllContent.setId("remove-all-content");

        dialog.getFooter().add(addExtraContent, removeExtraContent,
                removeDefaultContent, removeAllContent);

        NativeButton openDialog = new NativeButton("Open dialog",
                event -> dialog.open());
        openDialog.setId("open-dialog");

        add(openDialog, logs);
    }
}
