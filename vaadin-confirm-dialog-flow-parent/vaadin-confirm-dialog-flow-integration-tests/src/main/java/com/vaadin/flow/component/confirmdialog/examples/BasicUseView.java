package com.vaadin.flow.component.confirmdialog.examples;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/basicuse")
public class BasicUseView extends Div {

    private Log log = new Log();

    boolean expanded = true;

    public BasicUseView() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("My header");
        confirmDialog.setText("Here is my text");
        Button showDialogButton = new Button("Show dialog",
                e -> confirmDialog.open());
        showDialogButton.setId("open-dialog");

        NativeButton logButton = new NativeButton("Log",
                event -> log.log("Clicked"));
        logButton.setId("log");

        add(confirmDialog, showDialogButton, logButton, new Hr(), log);
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
