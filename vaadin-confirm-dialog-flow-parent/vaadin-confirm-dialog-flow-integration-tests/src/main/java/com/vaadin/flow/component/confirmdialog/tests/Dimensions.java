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
package com.vaadin.flow.component.confirmdialog.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Dimensions
 */
@Route("vaadin-confirm-dialog/Dimensions")
public class Dimensions extends Div {

    public static String VIEW_ID = "dimensions";
    public static String CONFIRM_DIALOG_ID = "confirm-dialog-dimensions";
    public static String OPEN_DIALOG_ID = "open-confirm-dialog-id";
    public static String ATTACH_DIALOG_ID = "attach-confirm-dialog";
    public static String CHANGE_DIALOG_WIDTH_ID = "change-confirm-dialog-width";
    public static String CHANGE_DIALOG_HEIGHT_ID = "change-confirm-dialog-height";
    public static String CHANGE_DIALOG_ATTACHED_WIDTH_ID = "change-confirm-attached-dialog-width";
    public static String CHANGE_DIALOG_ATTACHED_HEIGHT_ID = "change-confirm-attached-dialog-height";
    public static String RESET_DIALOG_DIMENSIONS_ID = "reset-confirm-dialog-dimensions";

    public static String DIMENSION_BIGGER = "600px";
    public static String DIMENSION_SMALLER = "300px";

    public Dimensions() {
        setId(VIEW_ID);
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setId(CONFIRM_DIALOG_ID);
        confirmDialog.setHeader("Confirm dialog header");

        Button changeWidthOpenedButton = new Button("ChangeWidth",
                e -> confirmDialog.setWidth(DIMENSION_SMALLER));
        changeWidthOpenedButton.setId(CHANGE_DIALOG_ATTACHED_WIDTH_ID);

        Button changeHeightOpenedButton = new Button("ChangeHeight",
                e -> confirmDialog.setHeight(DIMENSION_SMALLER));
        changeHeightOpenedButton.setId(CHANGE_DIALOG_ATTACHED_HEIGHT_ID);

        confirmDialog.add(changeWidthOpenedButton, changeHeightOpenedButton);

        Button attachConfirmDialogButton = new Button("AttachConfirmDialog",
                e -> {
                    add(confirmDialog);
                });
        attachConfirmDialogButton.setId(ATTACH_DIALOG_ID);

        Button openConfirmDialogButton = new Button("OpenConfirmDialog", e -> {
            confirmDialog.open();
        });
        openConfirmDialogButton.setId(OPEN_DIALOG_ID);

        Button changeDialogWidthButton = new Button("ChangeDialogWidth", e -> {
            confirmDialog.setWidth(DIMENSION_BIGGER);
        });
        changeDialogWidthButton.setId(CHANGE_DIALOG_WIDTH_ID);

        Button changeDialogHeightButton = new Button("ChangeDialogHeight",
                e -> {
                    confirmDialog.setHeight(DIMENSION_BIGGER);
                });
        changeDialogHeightButton.setId(CHANGE_DIALOG_HEIGHT_ID);

        Button resetDialogDimenssion = new Button("ResetDialogDimensions",
                e -> {
                    confirmDialog.setWidth(null);
                    confirmDialog.setHeight(null);
                });
        resetDialogDimenssion.setId(RESET_DIALOG_DIMENSIONS_ID);

        add(attachConfirmDialogButton, openConfirmDialogButton,
                changeDialogWidthButton, changeDialogHeightButton,
                resetDialogDimenssion);
    }
}
