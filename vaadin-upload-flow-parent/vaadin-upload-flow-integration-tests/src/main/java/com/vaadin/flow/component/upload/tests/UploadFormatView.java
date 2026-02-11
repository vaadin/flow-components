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
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadFormat;
import com.vaadin.flow.router.Route;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * View for {@link Upload} tests.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-upload/format")
public class UploadFormatView extends Div {

    public UploadFormatView() {
        Upload upload = new Upload();

        upload.setUploadFormat(UploadFormat.MULTIPART);

        upload.setUploadHandler(uploadEvent -> {
            String fileName = uploadEvent.getFileName();

            HttpServletRequest r = (HttpServletRequest) uploadEvent
                    .getRequest();
            try {
                r.getParts();
                uploadEvent.getUI().access(() -> {
                    add("multipart=true");
                });
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }

            uploadEvent.getInputStream().readAllBytes();
            uploadEvent.getUI().access(() -> {
                add(fileName + " uploaded");
            });
        });

        add(upload);
    }

}
