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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class UploadSlotsTest {
    @Test
    void getUploadButton_defaultButtonExists() {
        Upload upload = new Upload();
        Component button = upload.getUploadButton();
        assertEquals("add-button", button.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(button));
    }

    @Test
    void setUploadButton_buttonIsAdded() {
        Upload upload = new Upload();
        NativeButton button = new NativeButton("Add files");
        upload.setUploadButton(button);
        assertEquals(button, upload.getUploadButton());
        assertEquals("add-button", button.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(button));
    }

    @Test
    void setUploadButtonNull_defaultButtonIsRestored() {
        Upload upload = new Upload();
        Component defaultButton = upload.getUploadButton();

        NativeButton button = new NativeButton("Add files");
        upload.setUploadButton(button);

        upload.setUploadButton(null);

        assertEquals(defaultButton, upload.getUploadButton());
        assertEquals(upload, getParent(defaultButton));
    }

    @Test
    void getDropLabel_defaultLabelExists() {
        Upload upload = new Upload();
        Component label = upload.getDropLabel();
        assertEquals("drop-label", label.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(label));
    }

    @Test
    void setDropLabel_labelIsAdded() {
        Upload upload = new Upload();
        Span label = new Span("Drop files here");
        upload.setDropLabel(label);
        assertEquals(label, upload.getDropLabel());
        assertEquals("drop-label", label.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(label));
    }

    @Test
    void setDropLabelNull_defaultLabelIsRestored() {
        Upload upload = new Upload();
        Component defaultLabel = upload.getDropLabel();

        Span label = new Span("Drop files here");
        upload.setDropLabel(label);

        upload.setDropLabel(null);

        assertEquals(defaultLabel, upload.getDropLabel());
        assertEquals(upload, getParent(defaultLabel));
    }

    @Test
    void getDropLabelIcon_defaultIconExists() {
        Upload upload = new Upload();
        Component icon = upload.getDropLabelIcon();
        assertEquals("drop-label-icon", icon.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(icon));
    }

    @Test
    void setDropLabelIcon_iconIsAdded() {
        Upload upload = new Upload();
        Span icon = new Span("->");
        upload.setDropLabelIcon(icon);
        assertEquals(icon, upload.getDropLabelIcon());
        assertEquals("drop-label-icon", icon.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(icon));
    }

    @Test
    void setDropLabelIconNull_defaultIconIsRestored() {
        Upload upload = new Upload();
        Component defaultIcon = upload.getDropLabelIcon();

        Span icon = new Span("->");
        upload.setDropLabelIcon(icon);

        upload.setDropLabelIcon(null);

        assertEquals(defaultIcon, upload.getDropLabelIcon());
        assertEquals(upload, getParent(defaultIcon));
    }

    private static Component getParent(Component component) {
        return component.getParent().orElse(null);
    }
}
