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
package com.vaadin.flow.component.confirmdialog.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/modality")
public class ModalityPage extends Div {
    public ModalityPage() {
        ConfirmDialog dialog = new ConfirmDialog();

        Button addDialog = new Button("Add dialog to UI", e -> add(dialog));
        addDialog.setId("add-dialog");

        Button openDialog = new Button("Open dialog", e -> dialog.open());
        openDialog.setId("open-dialog");

        Span testClickResult = new Span();
        testClickResult.setId("test-click-result");
        Button testClick = new Button("Test click",
                event -> testClickResult.setText("Click event received"));
        testClick.setId("test-click");

        add(new Div(addDialog, openDialog));
        add(new Div(testClick, testClickResult));
    }
}
