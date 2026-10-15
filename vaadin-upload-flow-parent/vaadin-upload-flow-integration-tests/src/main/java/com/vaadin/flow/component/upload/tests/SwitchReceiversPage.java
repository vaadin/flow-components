/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;

@Route("vaadin-upload/switch-receivers")
public class SwitchReceiversPage extends Div {

    public SwitchReceiversPage() {
        Upload upload = new Upload();

        NativeButton setSingleFileReceiver = new NativeButton(
                "Set single file receiver", event -> {
                    MemoryBuffer buffer = new MemoryBuffer();
                    upload.setReceiver(buffer);
                });
        setSingleFileReceiver.setId("set-single-file-receiver");

        NativeButton setMultiFileReceiver = new NativeButton(
                "Set multi file receiver", event -> {
                    MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
                    upload.setReceiver(buffer);
                });
        setMultiFileReceiver.setId("set-multi-file-receiver");

        NativeButton setMultiFileReceiverAndMaxFiles = new NativeButton(
                "Set multi file receiver and max files", event -> {
                    MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
                    upload.setReceiver(buffer);
                    upload.setMaxFiles(3);
                });
        setMultiFileReceiverAndMaxFiles
                .setId("set-multi-file-receiver-and-max-files");

        add(upload, setSingleFileReceiver, setMultiFileReceiver,
                setMultiFileReceiverAndMaxFiles);
    }

}
