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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.ModularUploadFeatureFlagProvider;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class UploadDropZoneTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            ModularUploadFeatureFlagProvider.MODULAR_UPLOAD);

    private Div owner;
    private UploadManager manager;

    @BeforeEach
    void setup() {
        owner = new Div();
        ui.add(owner);
        manager = new UploadManager(owner);
    }

    @Test
    void constructor_default_createsDropZone() {
        UploadDropZone dropZone = new UploadDropZone();

        Assertions.assertNotNull(dropZone);
        Assertions.assertEquals("vaadin-upload-drop-zone",
                dropZone.getElement().getTag());
    }

    @Test
    void constructor_withManager_linksToManager() {
        UploadDropZone dropZone = new UploadDropZone(manager);

        Assertions.assertSame(manager, dropZone.getUploadManager());
    }

    @Test
    void constructor_withContentAndManager_setsContentAndLinksToManager() {
        Span content = new Span("Drop files here");
        UploadDropZone dropZone = new UploadDropZone(content, manager);

        Assertions.assertSame(content, dropZone.getContent());
        Assertions.assertSame(manager, dropZone.getUploadManager());
    }

    @Test
    void constructor_withNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new UploadDropZone((UploadManager) null));
    }

    @Test
    void constructor_withContentAndNullManager_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new UploadDropZone(new Span(), null));
    }

    @Test
    void setUploadManager_linksToManager() {
        UploadDropZone dropZone = new UploadDropZone();

        dropZone.setUploadManager(manager);

        Assertions.assertSame(manager, dropZone.getUploadManager());
    }

    @Test
    void getUploadManager_default_returnsNull() {
        UploadDropZone dropZone = new UploadDropZone();

        Assertions.assertNull(dropZone.getUploadManager());
    }

    @Test
    void implementsHasEnabled() {
        Assertions.assertTrue(
                HasEnabled.class.isAssignableFrom(UploadDropZone.class));
    }

    @Test
    void implementsHasSize() {
        Assertions.assertTrue(
                HasSize.class.isAssignableFrom(UploadDropZone.class));
    }

    @Test
    void setContent_setsChildComponent() {
        UploadDropZone dropZone = new UploadDropZone();
        Span span = new Span("Drop files here");

        dropZone.setContent(span);

        Assertions.assertEquals(1, dropZone.getElement().getChildCount());
        Assertions.assertSame(span, dropZone.getContent());
    }

    @Test
    void setContent_replacesExistingContent() {
        UploadDropZone dropZone = new UploadDropZone();
        Span first = new Span("First");
        Span second = new Span("Second");

        dropZone.setContent(first);
        dropZone.setContent(second);

        Assertions.assertEquals(1, dropZone.getElement().getChildCount());
        Assertions.assertSame(second, dropZone.getContent());
    }

    @Test
    void setContent_null_removesContent() {
        UploadDropZone dropZone = new UploadDropZone();
        dropZone.setContent(new Span("Content"));

        dropZone.setContent(null);

        Assertions.assertEquals(0, dropZone.getElement().getChildCount());
        Assertions.assertNull(dropZone.getContent());
    }

    @Test
    void getContent_default_returnsNull() {
        UploadDropZone dropZone = new UploadDropZone();

        Assertions.assertNull(dropZone.getContent());
    }
}
