

package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;

@Route("vaadin-upload/non-immediate-upload")
public class NonImmediateUploadView extends Div {

    private Span result = new Span("No errors");

    public NonImmediateUploadView() {
        result.setId("error-handler-message");

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAutoUpload(false);
        add(upload, result);
    }

    @Override
    public void onAttach(AttachEvent attachEvent) {
        attachEvent.getUI().getSession().setErrorHandler(event -> {
            result.setText("There was an error");
            event.getThrowable().printStackTrace();
        });
    }
}
