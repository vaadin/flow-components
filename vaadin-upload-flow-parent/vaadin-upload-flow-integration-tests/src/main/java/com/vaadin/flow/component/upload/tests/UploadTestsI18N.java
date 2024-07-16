/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.upload.UploadI18N;

import java.util.stream.Collectors;
import java.util.stream.Stream;

class UploadTestsI18N {
    static final UploadI18N RUSSIAN_FULL = new UploadI18N()
            .setDropFiles(
                    new UploadI18N.DropFiles().setOne("Перетащите файл сюда...")
                            .setMany("Перетащите файлы сюда..."))
            .setAddFiles(new UploadI18N.AddFiles()
                    .setOne("Выбрать файл").setMany("Добавить файлы"))
            .setCancel("Отменить")
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
                    .collect(Collectors.toList()));

    static final UploadI18N RUSSIAN_PARTIAL = new UploadI18N()
            // Only translate a single property from dropFiles
            .setDropFiles(new UploadI18N.DropFiles()
                    .setOne("Перетащите файл сюда..."))
            // Set an empty object into addFiles, but don't translate anything
            .setAddFiles(new UploadI18N.AddFiles());
}
