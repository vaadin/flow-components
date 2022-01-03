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
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;

@Route("vaadin-upload/i18n")
public class UploadI18nView extends Div {
    public UploadI18nView() {
        createFullUploadI18N();
        createPartialUploadI18N();
    }

    private void createFullUploadI18N() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setId("upload-full-i18n");

        upload.setI18n(UploadTestsI18N.RUSSIAN_FULL);

        add(new H1("Full I18N"), upload, new Hr());
    }

    private void createPartialUploadI18N() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setId("upload-partial-i18n");

        upload.setI18n(UploadTestsI18N.RUSSIAN_PARTIAL);

        add(new H1("Partial I18N"), upload, new Hr());
    }

}
