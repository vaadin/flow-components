/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Upload} tests.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-upload")
public class UploadView extends Div {

    public UploadView() {
        createSimpleUpload();
    }

    private void createSimpleUpload() {
        Div output = new Div();
        Div eventsOutput = new Div();
        Div fileList = new Div();

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            try {
                output.add(event.getFileName());
                output.add(IOUtils.toString(
                        buffer.getInputStream(event.getFileName()), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            eventsOutput.add("-succeeded");
        });
        upload.addAllFinishedListener(event -> eventsOutput.add("-finished"));

        NativeButton clearFileListBtn = new NativeButton("Clear file list",
                e -> upload.clearFileList());

        NativeButton printFileListBtn = new NativeButton("Print file list",
                e -> fileList
                        .setText(upload.getElement().getProperty("files")));

        upload.setMaxFileSize(500 * 1024);
        upload.setId("test-upload");
        clearFileListBtn.setId("clear-file-list");
        printFileListBtn.setId("print-file-list");
        fileList.setId("file-list");
        output.setId("test-output");
        eventsOutput.setId("test-events-output");

        addCard("Simple in memory receiver", upload, output, eventsOutput,
                fileList, clearFileListBtn, printFileListBtn);
    }

    private void addCard(String title, Component... components) {
        Div container = new Div(components);
        container.addComponentAsFirst(new H2(title));
        add(container);
    }

}
