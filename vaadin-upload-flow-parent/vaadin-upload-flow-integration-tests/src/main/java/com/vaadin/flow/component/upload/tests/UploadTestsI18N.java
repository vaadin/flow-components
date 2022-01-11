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
