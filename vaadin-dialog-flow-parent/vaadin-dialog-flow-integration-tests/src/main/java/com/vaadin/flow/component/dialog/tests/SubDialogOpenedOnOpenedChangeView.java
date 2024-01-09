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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-dialog/sub-dialog-opened-on-opened-change")
public class SubDialogOpenedOnOpenedChangeView extends Div {

    public SubDialogOpenedOnOpenedChangeView() {
        Span output = new Span();
        output.setId("output");

        Dialog mainDialog = new Dialog();
        mainDialog.setHeaderTitle("Main Dialog");
        mainDialog.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                Dialog subDialog = new Dialog();
                subDialog.setHeaderTitle("Sub-Dialog");
                subDialog.open();
            }
        });
        mainDialog.addDetachListener(ev -> output.setText("Detached"));

        Button closeMainDialogAndOpenSubDialogButton = new Button(
                "Close main dialog and open sub-dialog",
                ev -> mainDialog.close());
        closeMainDialogAndOpenSubDialogButton
                .setId("close-main-dialog-and-open-sub-dialog");
        mainDialog.add(closeMainDialogAndOpenSubDialogButton);

        Button openMainDialogButton = new Button("Open main dialog");
        openMainDialogButton.setId("open-main-dialog");
        openMainDialogButton.addClickListener(e -> mainDialog.open());

        add(openMainDialogButton, output);
    }
}
