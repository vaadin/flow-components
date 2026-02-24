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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.ModularUploadFeatureFlagProvider;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.tests.EnableFeatureFlagRule;
import com.vaadin.tests.MockUIRule;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadDropZoneTest {
    @Rule
    public MockUIRule ui = new MockUIRule();
    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            ModularUploadFeatureFlagProvider.MODULAR_UPLOAD);

    private Div owner;
    private UploadManager manager;

    @Before
    public void setup() {
        owner = new Div();
        ui.add(owner);
        manager = new UploadManager(owner);
    }

    @Test
    public void constructor_default_createsDropZone() {
        UploadDropZone dropZone = new UploadDropZone();

        Assert.assertNotNull(dropZone);
        Assert.assertEquals("vaadin-upload-drop-zone",
                dropZone.getElement().getTag());
    }

    @Test
    public void constructor_withManager_linksToManager() {
        UploadDropZone dropZone = new UploadDropZone(manager);

        Assert.assertSame(manager, dropZone.getUploadManager());
    }

    @Test
    public void constructor_withContentAndManager_setsContentAndLinksToManager() {
        Span content = new Span("Drop files here");
        UploadDropZone dropZone = new UploadDropZone(content, manager);

        Assert.assertSame(content, dropZone.getContent());
        Assert.assertSame(manager, dropZone.getUploadManager());
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNull_throws() {
        new UploadDropZone((UploadManager) null);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withContentAndNullManager_throws() {
        new UploadDropZone(new Span(), null);
    }

    @Test
    public void setUploadManager_linksToManager() {
        UploadDropZone dropZone = new UploadDropZone();

        dropZone.setUploadManager(manager);

        Assert.assertSame(manager, dropZone.getUploadManager());
    }

    @Test
    public void getUploadManager_default_returnsNull() {
        UploadDropZone dropZone = new UploadDropZone();

        Assert.assertNull(dropZone.getUploadManager());
    }

    @Test
    public void implementsHasEnabled() {
        Assert.assertTrue(
                HasEnabled.class.isAssignableFrom(UploadDropZone.class));
    }

    @Test
    public void implementsHasSize() {
        Assert.assertTrue(HasSize.class.isAssignableFrom(UploadDropZone.class));
    }

    @Test
    public void setContent_setsChildComponent() {
        UploadDropZone dropZone = new UploadDropZone();
        Span span = new Span("Drop files here");

        dropZone.setContent(span);

        Assert.assertEquals(1, dropZone.getElement().getChildCount());
        Assert.assertSame(span, dropZone.getContent());
    }

    @Test
    public void setContent_replacesExistingContent() {
        UploadDropZone dropZone = new UploadDropZone();
        Span first = new Span("First");
        Span second = new Span("Second");

        dropZone.setContent(first);
        dropZone.setContent(second);

        Assert.assertEquals(1, dropZone.getElement().getChildCount());
        Assert.assertSame(second, dropZone.getContent());
    }

    @Test
    public void setContent_null_removesContent() {
        UploadDropZone dropZone = new UploadDropZone();
        dropZone.setContent(new Span("Content"));

        dropZone.setContent(null);

        Assert.assertEquals(0, dropZone.getElement().getChildCount());
        Assert.assertNull(dropZone.getContent());
    }

    @Test
    public void getContent_default_returnsNull() {
        UploadDropZone dropZone = new UploadDropZone();

        Assert.assertNull(dropZone.getContent());
    }
}
