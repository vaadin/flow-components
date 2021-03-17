package com.vaadin.flow.component.dialog.tests;

import java.util.EventObject;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/shortcuts")
public class DialogWithShortcutPage extends VerticalLayout {

    public static final Key SHORTCUT_KEY = Key.KEY_X;
String.join("", SHORTCUT_KEY.getKeys());
    public static final String EVENT_LOG = "event-log";
    public static final String UI_BUTTON = "ui-button";
    public static final String MODELESS_SHORTCUT_ON_UI = "modeless-shortcut-on-ui";
    public static final String MODELESS_SHORTCUT_LISTEN_ON_DIALOG = "modeless-shortcur-listen-on-dialog";
    public static final String LISTEN_ON_DIALOG = "listen-on-dialog";
    public static final String SHORTCUT_ON_UI = "shortcut-on-ui";
    public static final String DIALOG_ID = "dialog";
    public static final String REUSABLE_DIALOG = "reusable-dialog";
    public static final String UI_ID = "ui-id";
    public static final String DIALOG_BUTTON_MESSAGE_ID = "dialog-button-message";
    private int eventCounter;
    private int dialogCounter;

    private final Div eventLog;
    private Dialog reusableDialog;

    public DialogWithShortcutPage() {
        eventLog = new Div();
        eventLog.setId(EVENT_LOG);
        final NativeButton modelessWithShortcutOnUi = new NativeButton(
                "Modeless with shortcut on UI",
                e -> createAndOpenDialog(false).setModal(false));
        modelessWithShortcutOnUi.setId(MODELESS_SHORTCUT_ON_UI);
        final NativeButton modelessWithShortcutListenOnDialog = new NativeButton(
                "Modeless with shortcut listenOn(dialog)",
                e -> createAndOpenDialog(true).setModal(false));
        modelessWithShortcutListenOnDialog
                .setId(MODELESS_SHORTCUT_LISTEN_ON_DIALOG);
        final NativeButton dialogWithShortcutListenOnDialog = new NativeButton(
                "Dialog with shortcut listenOn(dialog)",
                e -> createAndOpenDialog(true));
        dialogWithShortcutListenOnDialog.setId(LISTEN_ON_DIALOG);
        final NativeButton dialogWithShortcutOnUi = new NativeButton(
                "Dialog with shortcut on UI", e -> createAndOpenDialog(false));
        dialogWithShortcutOnUi.setId(SHORTCUT_ON_UI);
        final NativeButton reusableDialogButton = new NativeButton("Reusable dialog",
                event -> {
                    if (reusableDialog == null) {
                        reusableDialog = createAndOpenDialog(true);
                    } else {
                        reusableDialog.open();
                    }
                });
        reusableDialogButton.setId(REUSABLE_DIALOG);
        add(modelessWithShortcutOnUi, modelessWithShortcutListenOnDialog,
                dialogWithShortcutOnUi, dialogWithShortcutListenOnDialog,
                reusableDialogButton);

        NativeButton nonDialogButton = new NativeButton("Button on UI with shortcut on UI",
                this::onEvent);
        nonDialogButton.addClickShortcut(SHORTCUT_KEY);
        nonDialogButton.setId(UI_BUTTON);

        add(nonDialogButton, eventLog);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        attachEvent.getUI().setId(UI_ID);
    }

    private Dialog createAndOpenDialog(boolean listenOnDialog) {
        int index = dialogCounter++;
        final String dialogId = DIALOG_ID + index;
        NativeButton myDialogButton = createDialogButton();
        myDialogButton.setId(dialogId + "-button");
        Dialog dialog = new Dialog(
                new Div(new Div(new Text("" + index)), myDialogButton,
                        new Input()));
        NativeButton closeButton = new NativeButton("Close", buttonClickEvent -> dialog.close());
        dialog.add(closeButton);
        dialog.setDraggable(true);
        dialog.open();
        dialog.setId(dialogId);
        final ShortcutRegistration registration = myDialogButton
                .addClickShortcut(SHORTCUT_KEY);
        if (listenOnDialog) {
            registration.listenOn(dialog);
        }
        return dialog;
    }

    private void onEvent(EventObject event) {
        final Div div = new Div();
        final int index = eventCounter++;
        div.setText(index + "-"
                + ((Component) event.getSource()).getId().orElse("NO-ID!"));
        div.setId(DIALOG_BUTTON_MESSAGE_ID + "-" + index);
        eventLog.addComponentAsFirst(div);
    }

    private NativeButton createDialogButton() {
        return new NativeButton("Hit " + SHORTCUT, this::onEvent);
    }
}
