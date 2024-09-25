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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/sub-dialog")
public class SubDialogPage extends VerticalLayout {

    public SubDialogPage() {
        // Create a dialog and a button to open it
        var dialog = new Dialog();
        var dialogButton = new Button("Open dialog", e -> {
            dialog.open();
        });
        dialogButton.setId("open-dialog");

        // Create a button to open a sub-dialog
        var subDialogButton = new Button("Open sub-dialog", e -> {
            var subDialog = new Dialog();
            subDialog.add(new Span("Sub-dialog"));
            subDialog.open();
        });
        subDialogButton.setId("open-sub-dialog");

        // Create a scroller with a long text
        var longText = new Span();
        longText.setText(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, "
                        + "nunc id aliquam ultricies, diam magna mollis nisl, ut aliquet elit "
                        + "nisl in diam. Nulla facilisi. Donec euismod, nunc id aliquam ultricies, "
                        + "diam magna mollis nisl, ut aliquet elit nisl in diam. Nulla facilisi. "
                        + "Donec euismod, nunc id aliquam ultricies, diam magna mollis nisl, ut "
                        + "aliquet elit nisl in diam. Nulla facilisi. Donec euismod, nunc id aliquam "
                        + "ultricies, diam magna mollis nisl, ut aliquet elit nisl in diam. Nulla "
                        + "facilisi. Donec euismod, nunc id aliquam ultricies, diam magna mollis "
                        + "nisl, ut aliquet elit nisl in diam. Nulla facilisi.");

        var scroller = new Scroller(longText);
        scroller.setId("scroller");
        scroller.setHeight("120px");
        scroller.setWidth("200px");

        dialog.add(subDialogButton, scroller);

        add(dialogButton);
    }

}
