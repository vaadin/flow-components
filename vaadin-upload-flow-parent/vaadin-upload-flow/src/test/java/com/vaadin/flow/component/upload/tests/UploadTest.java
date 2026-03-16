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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

public class UploadTest {

    @Test
    public void uploadNewUpload() {
        // Test no NPE due missing UI when setAttribute is called.
        Upload upload = new Upload();
    }

    @Test
    public void implementsHasEnabled() {
        Assert.assertTrue(HasEnabled.class.isAssignableFrom(Upload.class));
    }

    @Test
    public void switchBetweenSingleAndMultiFileReceiver_assertMaxFilesProperty() {
        Upload upload = new Upload();

        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));

        upload.setReceiver(new MultiFileMemoryBuffer());
        Assert.assertNull(upload.getElement().getProperty("maxFiles"));

        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));
    }

    @Test
    public void implementsHasThemeVariant() {
        Assert.assertTrue(HasThemeVariant.class.isAssignableFrom(Upload.class));
    }

    // --- Accepted MIME Types ---

    @Test
    public void setAcceptedMimeTypes_setsValues() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*", "application/pdf");

        var accepted = upload.getAcceptedMimeTypes();
        Assert.assertEquals(2, accepted.size());
        Assert.assertTrue(accepted.contains("image/*"));
        Assert.assertTrue(accepted.contains("application/pdf"));
    }

    @Test
    public void setAcceptedMimeTypes_withNull_clears() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedMimeTypes((String[]) null);

        Assert.assertTrue(upload.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    public void setAcceptedMimeTypes_withEmptyArray_clears() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedMimeTypes();

        Assert.assertTrue(upload.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    public void setAcceptedMimeTypes_withNullValue_throws() {
        var upload = new Upload();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedMimeTypes("image/*", null));
    }

    @Test
    public void setAcceptedMimeTypes_withBlankValue_throws() {
        var upload = new Upload();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedMimeTypes("image/*", "   "));
    }

    @Test
    public void setAcceptedMimeTypes_withoutSlash_throws() {
        var upload = new Upload();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedMimeTypes("imagepng"));
    }

    @Test
    public void getAcceptedMimeTypes_defaultIsEmpty() {
        var upload = new Upload();
        Assert.assertTrue(upload.getAcceptedMimeTypes().isEmpty());
    }

    // --- Accepted File Extensions ---

    @Test
    public void setAcceptedFileExtensions_setsValues() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf", ".txt");

        var accepted = upload.getAcceptedFileExtensions();
        Assert.assertEquals(2, accepted.size());
        Assert.assertTrue(accepted.contains(".pdf"));
        Assert.assertTrue(accepted.contains(".txt"));
    }

    @Test
    public void setAcceptedFileExtensions_withNull_clears() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf");
        upload.setAcceptedFileExtensions((String[]) null);

        Assert.assertTrue(upload.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    public void setAcceptedFileExtensions_withEmptyArray_clears() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf");
        upload.setAcceptedFileExtensions(new String[0]);

        Assert.assertTrue(upload.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    public void setAcceptedFileExtensions_withNullValue_throws() {
        var upload = new Upload();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedFileExtensions(".pdf", null));
    }

    @Test
    public void setAcceptedFileExtensions_withBlankValue_throws() {
        var upload = new Upload();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedFileExtensions(".pdf", "   "));
    }

    @Test
    public void setAcceptedFileExtensions_withoutDot_throws() {
        var upload = new Upload();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedFileExtensions("pdf"));
    }

    @Test
    public void getAcceptedFileExtensions_defaultIsEmpty() {
        var upload = new Upload();
        Assert.assertTrue(upload.getAcceptedFileExtensions().isEmpty());
    }

    // --- Accept property derivation ---

    @Test
    public void setAcceptedMimeTypes_setsAcceptProperty() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*", "application/pdf");

        var accept = upload.getElement().getProperty("accept");
        Assert.assertEquals("image/*,application/pdf", accept);
    }

    @Test
    public void setAcceptedFileExtensions_setsAcceptProperty() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf", ".txt");

        var accept = upload.getElement().getProperty("accept");
        Assert.assertEquals(".pdf,.txt", accept);
    }

    @Test
    public void setBothMimeAndExtensions_combinesInAcceptProperty() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedFileExtensions(".pdf");

        var accept = upload.getElement().getProperty("accept");
        Assert.assertEquals("image/*,.pdf", accept);
    }

    // --- setAcceptedFileTypes ---

    @Test
    public void setAcceptedFileTypes_newGettersThrow() {
        var upload = new Upload();
        upload.setAcceptedFileTypes("image/*", ".pdf", "application/json",
                ".txt");

        Assert.assertThrows(
                "getAcceptedMimeTypes should throw when deprecated "
                        + "setAcceptedFileTypes is configured",
                IllegalStateException.class, upload::getAcceptedMimeTypes);
        Assert.assertThrows(
                "getAcceptedFileExtensions should throw when deprecated "
                        + "setAcceptedFileTypes is configured",
                IllegalStateException.class, upload::getAcceptedFileExtensions);
    }

    @Test
    public void configureFileRestrictionsSeparately_setAcceptedFileTypes_throws() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedFileExtensions(".pdf");

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileTypes((String[]) null));
    }

    @Test
    public void getAcceptedFileTypes_fromDeprecatedSetter() {
        var upload = new Upload();
        upload.setAcceptedFileTypes("image/*", ".pdf");

        var types = upload.getAcceptedFileTypes();
        Assert.assertEquals(2, types.size());
        Assert.assertTrue(types.contains("image/*"));
        Assert.assertTrue(types.contains(".pdf"));
    }

    @Test
    public void getAcceptedFileTypes_fromSeparateSetters_throws() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedFileExtensions(".pdf");

        Assert.assertThrows(IllegalStateException.class,
                upload::getAcceptedFileTypes);
    }

    @Test
    public void setAcceptedFileTypes_setsAcceptProperty() {
        var upload = new Upload();
        upload.setAcceptedFileTypes("image/*", ".pdf");

        var accept = upload.getElement().getProperty("accept");
        Assert.assertEquals("image/*,.pdf", accept);
    }

    @Test
    public void setAcceptedFileTypes_setAcceptedMimeTypes_throws() {
        var upload = new Upload();
        upload.setAcceptedFileTypes(".txt", "image/*");

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedMimeTypes("image/*"));
    }

    @Test
    public void setAcceptedFileTypes_setAcceptedFileExtensions_throws() {
        var upload = new Upload();
        upload.setAcceptedFileTypes(".txt");

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileExtensions(".pdf"));
    }

    @Test
    public void setAcceptedFileTypes_clear_setAcceptedFileExtensions_works() {
        var upload = new Upload();
        upload.setAcceptedFileTypes(".txt", "image/*");
        upload.setAcceptedFileTypes((String[]) null);

        upload.setAcceptedMimeTypes("image/*");
        Assert.assertEquals(List.of("image/*"), upload.getAcceptedMimeTypes());
    }

    @Test
    public void setAcceptedFileTypes_clear_setAcceptedMimeTypes_works() {
        var upload = new Upload();
        upload.setAcceptedFileTypes(".txt", "image/*");
        upload.setAcceptedFileTypes((String[]) null);

        upload.setAcceptedFileExtensions(".pdf");
        Assert.assertEquals(List.of(".pdf"),
                upload.getAcceptedFileExtensions());
    }

    @Test
    public void setAcceptedMimeTypes_setAcceptedFileTypes_throws() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileTypes(".txt"));
    }

    @Test
    public void setAcceptedFileExtensions_setAcceptedFileTypes_throws() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf");

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileTypes(".txt"));
    }

    @Test
    public void setAcceptedMimeTypes_clear_setAcceptedFileTypes_works() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedMimeTypes();

        upload.setAcceptedFileTypes("image/*", ".pdf");

        var accept = upload.getElement().getProperty("accept");
        Assert.assertEquals("image/*,.pdf", accept);
    }

    @Test
    public void setAcceptedFileExtensions_clear_setAcceptedFileTypes_works() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf");
        upload.setAcceptedFileExtensions();

        upload.setAcceptedFileTypes("image/*", ".pdf");

        var accept = upload.getElement().getProperty("accept");
        Assert.assertEquals("image/*,.pdf", accept);
    }

    // --- Receiver guard ---

    @Test
    public void setAcceptedMimeTypes_withReceiver_throws() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedMimeTypes("image/*"));
    }

    @Test
    public void setAcceptedFileExtensions_withReceiver_throws() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileExtensions(".pdf"));
    }

    @Test
    public void setReceiver_withAcceptedMimeTypes_throws() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setReceiver(new MemoryBuffer()));
    }

    @Test
    public void setReceiver_withAcceptedFileExtensions_throws() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf");

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setReceiver(new MemoryBuffer()));
    }

    @Test
    public void setAcceptedMimeTypes_clearWithNull_withReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        upload.setAcceptedMimeTypes((String[]) null);
        Assert.assertTrue(upload.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    public void setAcceptedMimeTypes_clearWithEmpty_withReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        upload.setAcceptedMimeTypes();
        Assert.assertTrue(upload.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    public void setAcceptedFileExtensions_clearWithNull_withReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        upload.setAcceptedFileExtensions((String[]) null);
        Assert.assertTrue(upload.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    public void setAcceptedFileExtensions_clearWithEmpty_withReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        upload.setAcceptedFileExtensions(new String[0]);
        Assert.assertTrue(upload.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    public void setFileRestrictionsSeparately_clear_setReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedFileExtensions(".pdf");

        // Clear both fields
        upload.setAcceptedMimeTypes((String[]) null);
        upload.setAcceptedFileExtensions((String[]) null);

        // Should not throw now
        upload.setReceiver(new MemoryBuffer());
    }

    @Test
    public void setAcceptedFileTypes_withReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        // Deprecated setter should still work with Receiver (client-side only)
        upload.setAcceptedFileTypes("image/*", ".pdf");
        Assert.assertEquals("image/*,.pdf",
                upload.getElement().getProperty("accept"));
    }

    @Test
    public void constructorWithReceiver_setAcceptedMimeTypes_throws() {
        var upload = new Upload(new MemoryBuffer());

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedMimeTypes("image/*"));
    }

    @Test
    public void constructorWithReceiver_setAcceptedFileExtensions_throws() {
        var upload = new Upload(new MemoryBuffer());

        Assert.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileExtensions(".pdf"));
    }
}
