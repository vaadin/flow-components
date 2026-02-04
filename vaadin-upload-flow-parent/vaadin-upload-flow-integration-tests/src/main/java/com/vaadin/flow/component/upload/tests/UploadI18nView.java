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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;

@Route("vaadin-upload/i18n")
public class UploadI18nView extends Div {
    static final UploadI18N RUSSIAN_FULL = new UploadI18N()
            .setDropFiles(
                    new UploadI18N.DropFiles().setOne("Перетащите файл сюда...")
                            .setMany("Перетащите файлы сюда..."))
            .setAddFiles(new UploadI18N.AddFiles().setOne("Выбрать файл")
                    .setMany("Добавить файлы"))
            .setFile(new UploadI18N.File().setStart("Загрузить")
                    .setRetry("Повторить").setRemove("Удалить"))
            .setError(new UploadI18N.Error()
                    .setTooManyFiles("Слишком много файлов.")
                    .setFileIsTooBig("Слишком большой файл.")
                    .setIncorrectFileType("Некорректный тип файла."))
            .setUploading(
                    new UploadI18N.Uploading()
                            .setStatus(new UploadI18N.Uploading.Status()
                                    .setConnecting("Соединение...")
                                    .setStalled("Загрузка застопорилась.")
                                    .setProcessing(
                                            "Обработка файла...")
                                    .setHeld("прош"))
                            .setRemainingTime(
                                    new UploadI18N.Uploading.RemainingTime()
                                            .setPrefix("оставшееся время: ")
                                            .setUnknown(
                                                    "оставшееся время неизвестно"))
                            .setError(new UploadI18N.Uploading.Error()
                                    .setServerUnavailable("Сервер недоступен")
                                    .setUnexpectedServerError(
                                            "Неожиданная ошибка сервера")
                                    .setForbidden("Загрузка запрещена")))
            .setUnits(Stream
                    .of("Б", "Кбайт", "Мбайт", "Гбайт", "Тбайт", "Пбайт",
                            "Эбайт", "Збайт", "Ибайт")
                    .collect(Collectors.toList()), 1024);

    public UploadI18nView() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        NativeButton setI18n = new NativeButton("Set I18N", e -> {
            upload.setI18n(RUSSIAN_FULL);
        });
        setI18n.setId("set-i18n");

        NativeButton setEmptyI18n = new NativeButton("Set empty I18N", e -> {
            upload.setI18n(new UploadI18N());
        });
        setEmptyI18n.setId("set-empty-i18n");

        add(upload, setI18n, setEmptyI18n);
    }
}
