/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.ui.upload;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.util.MessageDigestUtil;
import com.vaadin.router.Route;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tag;
import com.vaadin.ui.Text;
import com.vaadin.ui.common.HtmlComponent;
import com.vaadin.ui.html.Div;
import com.vaadin.ui.html.H2;
import com.vaadin.ui.html.Image;
import com.vaadin.ui.html.NativeButton;
import com.vaadin.ui.html.Span;
import com.vaadin.ui.paper.dialog.GeneratedPaperDialog;
import com.vaadin.ui.upload.receivers.MemoryBuffer;
import com.vaadin.ui.upload.receivers.MultiFileMemoryBuffer;

/**
 * View for {@link Upload} demo.
 *
 * @author Vaadin Ltd
 */
@Route("")
public class UploadView extends DemoView {

    @Override
    protected void initView() {
        createSimpleUpload();
        createSimpleMultiFileUpload();
        createFilteredMultiFileUpload();
        createNonImmediateUpload();
        changeDefaultComponents();
        i18nSampleUpload();
    }

    private void createSimpleUpload() {
        // begin-source-example
        // source-example-heading: Simple in memory receiver for single file upload
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            Component component = createComponent(event.getMIMEType(),
                    event.getFileName(), buffer.getInputStream());
            createAndOpenDialog("Simple upload", event.getFileName(),
                    component);
        });
        // end-source-example
        upload.setMaxFileSize(500 * 1024);
        upload.setId("test-upload");

        addCard("Simple in memory receiver for single file upload", upload);
    }

    private void createSimpleMultiFileUpload() {
        // begin-source-example
        // source-example-heading: Simple in memory receiver for multi file upload
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);

        // upload.addFinishedListener(
        // event -> System.out.println("Finished event"));
        upload.addSucceededListener(event -> {
            Component component = createComponent(event.getMIMEType(),
                    event.getFileName(),
                    buffer.getInputStream(event.getFileName()));
            createAndOpenDialog("Multi file upload", event.getFileName(),
                    component);
        });
        // end-source-example
        upload.setMaxFileSize(200 * 1024);

        addCard("Simple in memory receiver for multi file upload", upload);
    }

    private void createFilteredMultiFileUpload() {
        // begin-source-example
        // source-example-heading: Filtered multi file upload for images
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAccept("image/jpeg,image/png,image/gif");
        // upload.addFinishedListener(
        // event -> System.out.println("Finished event"));
        upload.addSucceededListener(event -> {
            Component component = createComponent(event.getMIMEType(),
                    event.getFileName(),
                    buffer.getInputStream(event.getFileName()));
            createAndOpenDialog("Image upload", event.getFileName(), component);
        });
        // end-source-example
        upload.setMaxFileSize(200 * 1024);

        addCard("Filtered multi file upload for images", upload);
    }

    private void createNonImmediateUpload() {
        // begin-source-example
        // source-example-heading: Non immediate upload
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setNoAuto(true);

        upload.addSucceededListener(event -> {
            Component component = createComponent(event.getMIMEType(),
                    event.getFileName(),
                    buffer.getInputStream(event.getFileName()));
            createAndOpenDialog("Uploaded file", event.getFileName(),
                    component);
        });
        // end-source-example
        upload.setMaxFileSize(200 * 1024);

        addCard("Non immediate upload", upload);
    }

    private void changeDefaultComponents() {
        // begin-source-example
        // source-example-heading: Custom components upload demo
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);

        NativeButton uploadButton = new NativeButton("Upload");
        uploadButton.getElement().setAttribute("slot", "add-button");
        upload.add(uploadButton);

        Span dropLabel = new Span("Drag and drop things here!");
        dropLabel.getElement().setAttribute("slot", "drop-label");
        upload.add(dropLabel);

        Span dropIcon = new Span("¸¸.•*¨*•♫♪");
        dropIcon.getElement().setAttribute("slot", "drop-label-icon");
        upload.add(dropIcon);

        upload.addSucceededListener(event -> {
            Component component = createComponent(event.getMIMEType(),
                    event.getFileName(),
                    buffer.getInputStream(event.getFileName()));
            createAndOpenDialog("Uploaded file", event.getFileName(),
                    component);
        });
        // end-source-example
        upload.setMaxFileSize(200 * 1024);

        addCard("Custom components upload demo", upload);
    }

    private void i18nSampleUpload() {
        // begin-source-example
        // source-example-heading: i18n translations example
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            Component component = createComponent(event.getMIMEType(),
                    event.getFileName(), buffer.getInputStream());
            createAndOpenDialog("Simple upload", event.getFileName(),
                    component);
        });

        UploadI18N i18n = new UploadI18N();
        i18n.setDropFiles(
                new UploadI18N.DropFiles().setOne("Перетащите файл сюда...")
                        .setMany("Перетащите файлы сюда...")).setAddFiles(
                new UploadI18N.AddFiles().setOne("Выбрать файл")
                        .setMany("Добавить файлы")).setCancel("Отменить")
                .setError(new UploadI18N.Error()
                        .setTooManyFiles("Слишком много файлов.")
                        .setFileIsTooBig("Слишком большой файл.")
                        .setIncorrectFileType("Некорректный тип файла."))
                .setUploading(new UploadI18N.Uploading().setStatus(
                        new UploadI18N.Uploading.Status()
                                .setConnecting("Соединение...")
                                .setStalled("Загрузка застопорилась.")
                                .setProcessing("Обработка файла..."))
                        .setRemainingTime(
                                new UploadI18N.Uploading.RemainingTime()
                                        .setPrefix("оставшееся время: ")
                                        .setUnknown(
                                                "оставшееся время неизвестно"))
                        .setError(new UploadI18N.Uploading.Error()
                                .setServerUnavailable("Сервер недоступен")
                                .setUnexpectedServerError(
                                        "Неожиданная ошибка сервера")
                                .setForbidden("Загрузка запрещена"))).setUnits(
                Stream.of("Б", "Кбайт", "Мбайт", "Гбайт", "Тбайт", "Пбайт",
                        "Эбайт", "Збайт", "Ибайт")
                        .collect(Collectors.toList()));

        upload.setI18n(i18n);
        // end-source-example
        upload.setMaxFileSize(200 * 1024);

        addCard("i18n translations example", upload);
    }

    private Component createComponent(String mimeType, String fileName,
            InputStream stream) {
        if (mimeType.startsWith("text")) {
            String text = "";
            try {
                text = IOUtils.toString(stream, "UTF-8");
            } catch (IOException e) {
                text = "exception reading stream";
            }
            return new Text(text);
        } else if (mimeType.startsWith("image")) {
            Image image = new Image();
            try {

                byte[] bytes = IOUtils.toByteArray(stream);
                image.getElement().setAttribute("src",
                        new StreamResource(fileName,
                                () -> new ByteArrayInputStream(bytes)));
                try (ImageInputStream in = ImageIO.createImageInputStream(
                        new ByteArrayInputStream(bytes))) {
                    final Iterator<ImageReader> readers = ImageIO
                            .getImageReaders(in);
                    if (readers.hasNext()) {
                        ImageReader reader = readers.next();
                        try {
                            reader.setInput(in);
                            image.setWidth(reader.getWidth(0) + "px");
                            image.setHeight(reader.getHeight(0) + "px");
                        } finally {
                            reader.dispose();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return image;
        }
        Div content = new Div();
        String text = String
                .format("Mime type: '%s'\nSHA-256 hash: '%s'", mimeType,
                        MessageDigestUtil.sha256(stream.toString()));
        content.setText(text);
        return new Div();

    }

    private void createAndOpenDialog(String title, String text,
            Component content) {
        addAndOpen(createDialog(title, text, content));
    }

    private GeneratedPaperDialog createDialog(String title, String text,
            Component content) {
        GeneratedPaperDialog dialog = new GeneratedPaperDialog();
        dialog.setAutoFitOnAttach(true);
        dialog.setVerticalAlign("top");
        dialog.setHorizontalAlign("left");
        dialog.getElement().getStyle().set("maxWidth", "90%");
        dialog.getElement().getStyle().set("maxHeight", "90%");

        HtmlComponent p = new HtmlComponent(Tag.P);
        p.getElement().setText(text);
        dialog.add(p);
        dialog.add(new H2(title));
        dialog.add(content);

        return dialog;
    }

    private void addAndOpen(GeneratedPaperDialog dialog) {
        getUI().ifPresent(ui -> ui.add(dialog));
        dialog.open();
    }
}
