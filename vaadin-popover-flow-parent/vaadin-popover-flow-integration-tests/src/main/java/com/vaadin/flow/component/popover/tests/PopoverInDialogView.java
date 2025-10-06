/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.popover.tests;




import static com.vaadin.flow.dom.Style.WhiteSpace.PRE;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-popover/dialog")
public class PopoverInDialogView extends Div {

    public PopoverInDialogView() {
        var messageLog = new Div();
        messageLog.setId("message-log");
        messageLog.getStyle().setWhiteSpace(PRE);

        var popoverDialog = new Popover(new Div("This is a popover"));
        popoverDialog.setId("popover-dialog");
        popoverDialog.addOpenedChangeListener(e -> {// Not working when inside a
                                                    // modal dialog
                messageLog.setText("Popover in dialog opened: " + e.isOpened());
        });

        var button = new NativeButton("Open popover");
        button.setId("popover-target");
        popoverDialog.setTarget(button);

        final Dialog dialog = new Dialog();
        dialog.setModality(ModalityMode.STRICT);

        var grid = new Grid<String>();
        grid.setItems("Item 1", "Item 2", "Item 3");
        grid.addColumn(new ComponentRenderer<>(item -> {
                var span = new Span(item);
                var name = item.toLowerCase().replace(" ", "-");
                span.setId("target-"+ name);
                var gridPopover = new Popover(new Div("Details for " + item));
                gridPopover.addOpenedChangeListener(listener -> {
                        messageLog.setText("Popover for " + item + " opened: "
                                + listener.isOpened());
                });
                gridPopover.setId("grid-popover-" + name);
                gridPopover.setTarget(span);
                return span;
        })).setHeader("Items");

        var closeDialogButton = new NativeButton("Close dialog",
                e -> dialog.close());
        closeDialogButton.setId("close-dialog");
        dialog.add(button, grid, messageLog, closeDialogButton);

        NativeButton openDialogButton = new NativeButton("Open dialog", e -> dialog.open());
        openDialogButton.setId("open-dialog");
        add(openDialogButton);
    }
}
