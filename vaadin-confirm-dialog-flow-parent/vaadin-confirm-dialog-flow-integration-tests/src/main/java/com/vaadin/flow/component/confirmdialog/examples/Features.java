package com.vaadin.flow.component.confirmdialog.examples;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;

@Route("Features")
@BodySize
public class Features extends Div {

    static final String CONFIRM_DIALOG = "SampleConfirmDialog";
    static final String CONFIRM_DELETE_DIALOG = "SampleConfirmDeleteDialog";
    static final String CONFIRM_PUBLISH_DIALOG = "SampleConfirmPublishDialog";
    static final String UNSAVED_CHANGES_DIALOG = "SampleUnsavedChangesDialog";

    private Div status = new Div(new Text(""));

    public Features() {
        add(status);
        Map<String, Function<String, ConfirmDialog>> samples = new HashMap<>();
        samples.put(CONFIRM_DIALOG, this::createConfirmDialog);
        samples.put(CONFIRM_DELETE_DIALOG, this::createConfirmDeleteDialog);
        samples.put(CONFIRM_PUBLISH_DIALOG, this::createConfirmPublishDialog);
        samples.put(UNSAVED_CHANGES_DIALOG, this::createUnsavedChangesDialog);
        samples.entrySet().forEach(
                entry -> createSample(entry.getKey(), entry.getValue()));
    }

    private void createSample(String sample,
            Function<String, ConfirmDialog> creator) {
        Button button = new Button(sample);
        button.setId(sample);
        button.addClickListener(event -> openSample(creator.apply(sample)));
        add(button);
    }

    private ConfirmDialog createConfirmDialog(String sample) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setId(sample);
        return dialog;
    }

    private ConfirmDialog createConfirmDeleteDialog(String sample) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setId(sample);
        return dialog;
    }

    private ConfirmDialog createConfirmPublishDialog(String sample) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setId(sample);
        return dialog;
    }

    private ConfirmDialog createUnsavedChangesDialog(String sample) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setId(sample);
        return dialog;
    }

    private void openSample(ConfirmDialog dialog) {
        status.setText("");
        dialog.open();
        new ConfirmDialog("Meeting starting",
                "Your next meeting starts in 5 minutes", "OK", this::onOK);
        new ConfirmDialog("Ready to publish?",
                "Do you want to publish this post?", "Publish", this::onPublish,
                "Cancel", this::onCancel);
        new ConfirmDialog("Usaved changes",
                "Do you want to save or discard your changes before navigating away?",
                "Save", this::onSave, "Discard", this::onDiscard, "Cancel",
                (Object) -> {
                });
        dialog.setConfirmButtonTheme("primary");
        dialog.setRejectButtonTheme("error");

        new ConfirmDialog();
        dialog.setHeader("Question");
        dialog.setText("Yes or no or cancel?");
        dialog.setConfirmButton("Yes", this::onConfirm, "primary");
        dialog.setRejectButton("No", this::onReject);
        dialog.setCancelButtonEnabled(true);
    }

    private void onOK(Object event) {

    }

    private void onPublish(Object event) {

    }

    private void onSave(Object event) {

    }

    private void onConfirm(Object event) {

    }

    private void onReject(Object event) {

    }

    private void onDiscard(Object event) {

    }

    private void onCancel(Object event) {

    }

    public static class ConfirmDialog extends Dialog {

        public ConfirmDialog(String header, String text, String confirmText,
                Consumer<Object> confirmListener) {
            // TODO Auto-generated constructor stub
        }

        public void setCancelButtonEnabled(boolean enabled) {
            // TODO Auto-generated method stub

        }

        public void setRejectButton(String buttonText,
                Consumer<Object> rejectListener) {
            // TODO Auto-generated method stub

        }

        public void setConfirmButton(String buttonText,
                Consumer<Object> confirmListener, String theme) {
            // TODO Auto-generated method stub

        }

        public void setText(String text) {
            // TODO Auto-generated method stub

        }

        public void setHeader(String header) {
            // TODO Auto-generated method stub

        }

        public void setRejectButtonTheme(String theme) {
            // TODO Auto-generated method stub

        }

        public void setConfirmButtonTheme(String theme) {
            // TODO Auto-generated method stub

        }

        public ConfirmDialog(String header, String text, String confirmText,
                Consumer<Object> confirmListener, String cancelText,
                Consumer<Object> cancelListener) {
            // TODO Auto-generated constructor stub
        }

        public ConfirmDialog(String header, String text, String confirmText,
                Consumer<Object> confirmListener, String rejectText,
                Consumer<Object> rejectListener, String cancelText,
                Consumer<Object> cancelListener) {
            // TODO Auto-generated constructor stub
        }

        public ConfirmDialog() {
            // TODO Auto-generated constructor stub
        }

    }
}
