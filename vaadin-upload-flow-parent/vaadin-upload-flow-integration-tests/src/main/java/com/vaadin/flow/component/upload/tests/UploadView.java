/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.upload.tests;

import java.io.IOException;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.internal.StringUtil;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Upload} tests.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-upload")
public class UploadView extends Div {

    private Upload upload;
    private final Div logArea;

    public UploadView() {
        logArea = new Div();
        logArea.setId("log-area");

        createSimpleUpload();
        addControlButtons();
        add(logArea);
    }

    private void createSimpleUpload() {
        Div output = new Div();
        Div eventsOutput = new Div();
        Div fileCount = new Div();

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            try {
                output.add(event.getFileName());
                output.add(event.getMIMEType());
                output.add(StringUtil.toUTF8String(
                        buffer.getInputStream(event.getFileName())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            eventsOutput.add("-succeeded");
        });
        upload.addAllFinishedListener(event -> eventsOutput.add("-finished"));
        upload.addFileRejectedListener(event -> {
            eventsOutput.add("-rejected");
            output.add("REJECTED:" + event.getFileName());
        });
        upload.addFileRemovedListener(event -> {
            eventsOutput.add("-removed");
            output.add("REMOVED:" + event.getFileName());
        });
        upload.addProgressListener(
                event -> output.add("PROGRESS:" + event.getFileName()));

        NativeButton clearFileListBtn = new NativeButton("Clear file list",
                e -> upload.clearFileList());

        NativeButton printFileCountBtn = new NativeButton("Print file count",
                e -> {
                    upload.getElement().executeJs("return this.files")
                            .then(jsonValue -> {
                                fileCount.setText(
                                        String.valueOf(jsonValue.size()));
                            });
                });

        upload.setMaxFileSize(500 * 1024);
        upload.setId("test-upload");
        clearFileListBtn.setId("clear-file-list");
        printFileCountBtn.setId("print-file-count");
        fileCount.setId("file-count");
        output.setId("test-output");
        eventsOutput.setId("test-events-output");

        addCard("Simple in memory receiver", upload, output, eventsOutput,
                fileCount, clearFileListBtn, printFileCountBtn);
    }

    private void addControlButtons() {
        // --- Accepted MIME Types ---
        var mimeGroup = createButtonGroup("MIME Types:");
        var setAcceptText = new NativeButton("text/*",
                event -> upload.setAcceptedMimeTypes("text/*"));
        setAcceptText.setId("set-accept-text");
        var setAcceptImage = new NativeButton("image/*",
                event -> upload.setAcceptedMimeTypes("image/*"));
        setAcceptImage.setId("set-accept-image");
        var setAcceptMimePdf = new NativeButton("application/pdf",
                event -> upload.setAcceptedMimeTypes("application/pdf"));
        setAcceptMimePdf.setId("set-accept-mime-pdf");
        var clearMime = new NativeButton("Clear",
                event -> upload.setAcceptedMimeTypes((String[]) null));
        clearMime.setId("clear-accept-mime");
        mimeGroup.add(setAcceptText, setAcceptImage, setAcceptMimePdf,
                clearMime);
        add(mimeGroup);

        // --- Accepted File Extensions ---
        var extGroup = createButtonGroup("Extensions:");
        var setAcceptTxt = new NativeButton(".txt",
                event -> upload.setAcceptedFileExtensions(".txt"));
        setAcceptTxt.setId("set-accept-ext-txt");
        var setAcceptPdf = new NativeButton(".pdf",
                event -> upload.setAcceptedFileExtensions(".pdf"));
        setAcceptPdf.setId("set-accept-ext-pdf");
        var clearExt = new NativeButton("Clear",
                event -> upload.setAcceptedFileExtensions((String[]) null));
        clearExt.setId("clear-accept-ext");
        extGroup.add(setAcceptTxt, setAcceptPdf, clearExt);
        add(extGroup);

        // --- Accepted File Types (old API) ---
        var fileTypeGroup = createButtonGroup("File type (old API):");
        var setAcceptFileTypeTxt = new NativeButton(".txt",
                event -> upload.setAcceptedFileTypes(".txt"));
        setAcceptFileTypeTxt.setId("set-accept-file-type-txt");
        fileTypeGroup.add(setAcceptFileTypeTxt);
        add(fileTypeGroup);

        // --- Handler ---
        var handlerGroup = createButtonGroup("Handler:");
        var useUploadHandler = new NativeButton("Use UploadHandler", e -> {
            upload.setUploadHandler(uploadEvent -> {
                byte[] data = uploadEvent.getInputStream().readAllBytes();
                uploadEvent.getUI().access(
                        () -> log("Uploaded: " + uploadEvent.getFileName()
                                + " (" + data.length + " bytes)"));
            });
            log("Handler set: UploadHandler");
        });
        useUploadHandler.setId("use-upload-handler");
        handlerGroup.add(useUploadHandler);
        add(handlerGroup);
    }

    private Div createButtonGroup(String label) {
        var group = new Div();
        group.getStyle().set("display", "flex");
        group.getStyle().set("gap", "5px");
        group.getStyle().set("align-items", "center");
        group.getStyle().set("margin", "5px 0");

        var labelSpan = new Div();
        labelSpan.setText(label);
        labelSpan.getStyle().set("font-weight", "bold");
        labelSpan.getStyle().set("min-width", "120px");
        group.add(labelSpan);

        return group;
    }

    private void log(String message) {
        var entry = new Div();
        entry.setText(message);
        logArea.addComponentAsFirst(entry);
    }

    private void addCard(String title, Component... components) {
        Div container = new Div(components);
        container.addComponentAsFirst(new H2(title));
        add(container);
    }

}
