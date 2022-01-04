/*
 * Copyright 2000-2022 Vaadin Ltd.
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
 *
 */
package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import com.vaadin.flow.router.Route;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * View for {@link Upload} tests using {@link FileBuffer} and
 * {@link MultiFileBuffer}.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-upload/filebuffer")
public class FileBufferView extends Div {
    public FileBufferView() {
        add(createSingleFileUpload());
        add(createMultiFileUpload());
    }

    private static Div createSingleFileUpload() {
        final FileBuffer buffer = new FileBuffer();
        final Upload singleFileUpload = new Upload(buffer);
        singleFileUpload.setId("single-upload");
        singleFileUpload.setMaxFiles(1);
        return setupUploadSection(singleFileUpload,
                e -> buffer.getInputStream());

    }

    private static Div createMultiFileUpload() {
        final MultiFileBuffer buffer = new MultiFileBuffer();
        final Upload multiFileUpload = new Upload(buffer);
        multiFileUpload.setId("multi-upload");
        return setupUploadSection(multiFileUpload,
                e -> buffer.getInputStream(e.getFileName()));
    }

    private static Div setupUploadSection(Upload upload,
            Function<SucceededEvent, InputStream> streamProvider) {
        final String uploadId = upload.getId().orElse("");
        final Div output = new Div();
        output.setId(uploadId + "-output");
        final Div eventsOutput = new Div();
        eventsOutput.setId(uploadId + "-event-output");

        upload.addSucceededListener(event -> {
            try {
                output.add(event.getFileName());
                output.add(
                        IOUtils.toString(streamProvider.apply(event), "UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            eventsOutput.add("-succeeded");
        });

        upload.addAllFinishedListener(event -> eventsOutput.add("-finished"));

        return new Div(output, eventsOutput, upload);
    }

}
