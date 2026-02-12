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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadFormat;
import com.vaadin.flow.router.Route;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

@Route("vaadin-upload/format")
public class UploadFormatView extends Div {

    public UploadFormatView() {
        Upload upload = new Upload();
        upload.setUploadFormat(UploadFormat.MULTIPART);

        upload.setUploadHandler(uploadEvent -> {
            HttpServletRequest request = (HttpServletRequest) uploadEvent
                    .getRequest();
            try {
                // Try to get parts to check if the request is parsed as
                // multipart
                request.getParts();
                uploadEvent.getUI().access(() -> {
                    Span output = new Span("Parsed multipart request");
                    output.setId("output");
                    add(output);
                });
            } catch (ServletException e) {
                uploadEvent.getUI().access(() -> {
                    Span output = new Span("Failed to parse multipart request");
                    output.setId("output");
                    add(output);
                });
            }
        });

        add(upload);
    }
}
