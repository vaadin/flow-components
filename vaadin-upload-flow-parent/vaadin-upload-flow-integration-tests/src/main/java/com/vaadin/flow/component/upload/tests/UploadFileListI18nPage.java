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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadFileListI18N;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Test page for UploadFileList i18n.
 */
@Route("vaadin-upload/file-list-i18n")
public class UploadFileListI18nPage extends Div {

    // Full i18n configuration (Finnish)
    static final UploadFileListI18N FULL_I18N = new UploadFileListI18N()
            .setFile(new UploadFileListI18N.File().setRetry("Yritä uudelleen")
                    .setStart("Aloita").setRemove("Poista"))
            .setError(new UploadFileListI18N.Error()
                    .setTooManyFiles("Liian monta tiedostoa")
                    .setFileIsTooBig("Tiedosto on liian suuri")
                    .setIncorrectFileType("Väärä tiedostotyyppi"))
            .setUploading(new UploadFileListI18N.Uploading()
                    .setStatus(new UploadFileListI18N.Uploading.Status()
                            .setConnecting("Yhdistetään...")
                            .setStalled("Pysähtynyt")
                            .setProcessing("Käsitellään...").setHeld("Odottaa"))
                    .setRemainingTime(
                            new UploadFileListI18N.Uploading.RemainingTime()
                                    .setPrefix("jäljellä: ")
                                    .setUnknown("tuntematon"))
                    .setError(new UploadFileListI18N.Uploading.UploadError()
                            .setServerUnavailable("Palvelin ei saatavilla")
                            .setUnexpectedServerError("Palvelinvirhe")
                            .setForbidden("Kielletty")));

    public UploadFileListI18nPage() {
        var owner = new Div();
        var manager = new UploadManager(owner,
                UploadHandler.inMemory((metadata, data) -> {
                }));
        manager.setAutoUpload(false);

        var uploadButton = new UploadButton(manager);
        uploadButton.setText("Select Files");

        var fileList = new UploadFileList(manager);

        NativeButton setFullI18n = new NativeButton("Set full I18N", e -> {
            fileList.setI18n(FULL_I18N);
        });
        setFullI18n.setId("set-full-i18n");

        NativeButton setEmptyI18n = new NativeButton("Set empty I18N", e -> {
            fileList.setI18n(new UploadFileListI18N());
        });
        setEmptyI18n.setId("set-empty-i18n");

        add(owner, uploadButton, fileList, setFullI18n, setEmptyI18n);
    }
}
