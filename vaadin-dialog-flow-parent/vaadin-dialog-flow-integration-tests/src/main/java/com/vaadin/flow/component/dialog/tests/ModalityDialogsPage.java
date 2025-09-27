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
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd.
 *
 */
@Route("vaadin-dialog/dialog-modality")
public class ModalityDialogsPage extends Div {

    private Log log = new Log();

    public ModalityDialogsPage() {
        Dialog strictModalDialog = createStrictModalDialog();

        Dialog visualModalDialog = createVisualModalDialog();

        Dialog nonModalDialog = setupNonModalDialog();

        NativeButton openModal = new NativeButton("Open STRICT modal dialog",
                event -> strictModalDialog.open());
        openModal.setId("open-strict-modal-dialog");

        NativeButton openVisualModal = new NativeButton(
                "Open VISUAL modal dialog", event -> visualModalDialog.open());
        openVisualModal.setId("open-visual-modal-dialog");

        NativeButton addModal = new NativeButton("Add modal dialog to UI",
                event -> add(strictModalDialog));
        addModal.setId("add-modal-dialog");

        NativeButton enableCloseOnOutsideClick = new NativeButton(
                "Enable close on outside click",
                event -> strictModalDialog.setCloseOnOutsideClick(true));
        enableCloseOnOutsideClick.setId("enable-close-on-outside-click");

        NativeButton openNonModal = new NativeButton("Open non modal dialog",
                event -> nonModalDialog.open());
        openNonModal.setId("open-non-modal-dialog");

        NativeButton log = new NativeButton("Log",
                event -> this.log.log("Clicked"));
        log.setId("log");

        final NativeButton showStrictModal = new NativeButton(
                "Show Hidden Modal", e -> strictModalDialog.setVisible(true));
        showStrictModal.setId("show-strict-modal");

        final NativeButton showVisualModal = new NativeButton(
                "Show Visual Modal", e -> visualModalDialog.setVisible(true));
        showVisualModal.setId("show-visual-modal");

        add(openModal, addModal, enableCloseOnOutsideClick, showStrictModal,
                openVisualModal, showVisualModal, openNonModal, log, new Hr(),
                this.log);
    }

    private Dialog setupNonModalDialog() {
        final Dialog nonModalDialog = createNonModalDialog();
        nonModalDialog
                .add(new NativeButton("close", e -> nonModalDialog.close()));
        return nonModalDialog;
    }

    private Dialog createVisualModalDialog() {
        Dialog visualModalDialog = new Dialog();
        visualModalDialog.setModality(ModalityMode.VISUAL);
        visualModalDialog.setCloseOnOutsideClick(false);
        final NativeButton modalClose = new NativeButton("close",
                e -> visualModalDialog.close());
        modalClose.setId("close");
        final NativeButton hide = new NativeButton("hide",
                e -> visualModalDialog.setVisible(false));
        hide.setId("hide");
        visualModalDialog.add(modalClose, hide);
        return visualModalDialog;
    }

    private Dialog createStrictModalDialog() {
        Dialog modalDialog = new Dialog();
        modalDialog.setModality(ModalityMode.STRICT);
        modalDialog.setCloseOnOutsideClick(false);
        final NativeButton modalClose = new NativeButton("close",
                e -> modalDialog.close());
        modalClose.setId("close");
        final NativeButton hide = new NativeButton("hide",
                e -> modalDialog.setVisible(false));
        hide.setId("hide");
        modalDialog.add(modalClose, hide);

        Dialog nonModalDialog = createNonModalDialog();

        final NativeButton closeSub = new NativeButton("close",
                e -> nonModalDialog.close());
        closeSub.setId("close-sub");
        final NativeButton logSub = new NativeButton("log",
                e -> log.log("sub-click"));
        logSub.setId("log-sub");
        nonModalDialog.add(closeSub, logSub);

        final NativeButton openSubDialog = new NativeButton("open dialog",
                e -> nonModalDialog.open());
        openSubDialog.setId("open-sub");
        modalDialog.add(openSubDialog);

        return modalDialog;
    }

    private Dialog createNonModalDialog() {
        Dialog nonModalDialog = new Dialog();

        nonModalDialog.setCloseOnOutsideClick(false);
        nonModalDialog.setModality(ModalityMode.MODELESS);

        return nonModalDialog;
    }

    public static class Log extends Div {

        public static final String LOG_ID = "log-output";

        private int logCount;

        public Log() {
            setId(LOG_ID);
        }

        public void log(String msg) {
            Div div = new Div();
            div.addClassName("log");
            logCount++;
            div.setText(logCount + ". " + msg);
            add(div);
        }
    }

}
