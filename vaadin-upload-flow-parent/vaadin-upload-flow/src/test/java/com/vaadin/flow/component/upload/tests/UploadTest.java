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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

class UploadTest {

    @Test
    void uploadNewUpload() {
        // Test no NPE due missing UI when setAttribute is called.
        Upload upload = new Upload();
    }

    @Test
    void implementsHasEnabled() {
        Assertions.assertTrue(HasEnabled.class.isAssignableFrom(Upload.class));
    }

    @Test
    void switchBetweenSingleAndMultiFileReceiver_assertMaxFilesProperty() {
        Upload upload = new Upload();

        upload.setReceiver(new MemoryBuffer());
        Assertions.assertEquals(1,
                upload.getElement().getProperty("maxFiles", 0));

        upload.setReceiver(new MultiFileMemoryBuffer());
        Assertions.assertNull(upload.getElement().getProperty("maxFiles"));

        upload.setReceiver(new MemoryBuffer());
        Assertions.assertEquals(1,
                upload.getElement().getProperty("maxFiles", 0));
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(Upload.class));
    }

    // --- Accepted MIME Types ---

    @Test
    void setAcceptedMimeTypes_setsValues() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*", "application/pdf");

        var accepted = upload.getAcceptedMimeTypes();
        Assertions.assertEquals(2, accepted.size());
        Assertions.assertTrue(accepted.contains("image/*"));
        Assertions.assertTrue(accepted.contains("application/pdf"));
    }

    @Test
    void setAcceptedMimeTypes_withNull_clears() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedMimeTypes((String[]) null);

        Assertions.assertTrue(upload.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    void setAcceptedMimeTypes_withEmptyArray_clears() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedMimeTypes();

        Assertions.assertTrue(upload.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    void setAcceptedMimeTypes_withNullValue_throws() {
        var upload = new Upload();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedMimeTypes("image/*", null));
    }

    @Test
    void setAcceptedMimeTypes_withBlankValue_throws() {
        var upload = new Upload();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedMimeTypes("image/*", "   "));
    }

    @Test
    void setAcceptedMimeTypes_withoutSlash_throws() {
        var upload = new Upload();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedMimeTypes("imagepng"));
    }

    @Test
    void getAcceptedMimeTypes_defaultIsEmpty() {
        var upload = new Upload();
        Assertions.assertTrue(upload.getAcceptedMimeTypes().isEmpty());
    }

    // --- Accepted File Extensions ---

    @Test
    void setAcceptedFileExtensions_setsValues() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf", ".txt");

        var accepted = upload.getAcceptedFileExtensions();
        Assertions.assertEquals(2, accepted.size());
        Assertions.assertTrue(accepted.contains(".pdf"));
        Assertions.assertTrue(accepted.contains(".txt"));
    }

    @Test
    void setAcceptedFileExtensions_withNull_clears() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf");
        upload.setAcceptedFileExtensions((String[]) null);

        Assertions.assertTrue(upload.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    void setAcceptedFileExtensions_withEmptyArray_clears() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf");
        upload.setAcceptedFileExtensions(new String[0]);

        Assertions.assertTrue(upload.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    void setAcceptedFileExtensions_withNullValue_throws() {
        var upload = new Upload();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedFileExtensions(".pdf", null));
    }

    @Test
    void setAcceptedFileExtensions_withBlankValue_throws() {
        var upload = new Upload();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedFileExtensions(".pdf", "   "));
    }

    @Test
    void setAcceptedFileExtensions_withoutDot_throws() {
        var upload = new Upload();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> upload.setAcceptedFileExtensions("pdf"));
    }

    @Test
    void getAcceptedFileExtensions_defaultIsEmpty() {
        var upload = new Upload();
        Assertions.assertTrue(upload.getAcceptedFileExtensions().isEmpty());
    }

    // --- Accept property derivation ---

    @Test
    void setAcceptedMimeTypes_setsAcceptProperty() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*", "application/pdf");

        var accept = upload.getElement().getProperty("accept");
        Assertions.assertEquals("image/*,application/pdf", accept);
    }

    @Test
    void setAcceptedFileExtensions_setsAcceptProperty() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf", ".txt");

        var accept = upload.getElement().getProperty("accept");
        Assertions.assertEquals(".pdf,.txt", accept);
    }

    @Test
    void setBothMimeAndExtensions_combinesInAcceptProperty() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedFileExtensions(".pdf");

        var accept = upload.getElement().getProperty("accept");
        Assertions.assertEquals("image/*,.pdf", accept);
    }

    // --- setAcceptedFileTypes ---

    @Test
    void setAcceptedFileTypes_newGettersThrow() {
        var upload = new Upload();
        upload.setAcceptedFileTypes("image/*", ".pdf", "application/json",
                ".txt");

        Assertions.assertThrows(IllegalStateException.class,
                upload::getAcceptedMimeTypes,
                "getAcceptedMimeTypes should throw when deprecated "
                        + "setAcceptedFileTypes is configured");
        Assertions.assertThrows(IllegalStateException.class,
                upload::getAcceptedFileExtensions,
                "getAcceptedFileExtensions should throw when deprecated "
                        + "setAcceptedFileTypes is configured");
    }

    @Test
    void configureFileRestrictionsSeparately_setAcceptedFileTypes_throws() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedFileExtensions(".pdf");

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileTypes((String[]) null));
    }

    @Test
    void getAcceptedFileTypes_fromDeprecatedSetter() {
        var upload = new Upload();
        upload.setAcceptedFileTypes("image/*", ".pdf");

        var types = upload.getAcceptedFileTypes();
        Assertions.assertEquals(2, types.size());
        Assertions.assertTrue(types.contains("image/*"));
        Assertions.assertTrue(types.contains(".pdf"));
    }

    @Test
    void getAcceptedFileTypes_fromSeparateSetters_throws() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedFileExtensions(".pdf");

        Assertions.assertThrows(IllegalStateException.class,
                upload::getAcceptedFileTypes);
    }

    @Test
    void setAcceptedFileTypes_setsAcceptProperty() {
        var upload = new Upload();
        upload.setAcceptedFileTypes("image/*", ".pdf");

        var accept = upload.getElement().getProperty("accept");
        Assertions.assertEquals("image/*,.pdf", accept);
    }

    @Test
    void setAcceptedFileTypes_setAcceptedMimeTypes_throws() {
        var upload = new Upload();
        upload.setAcceptedFileTypes(".txt", "image/*");

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedMimeTypes("image/*"));
    }

    @Test
    void setAcceptedFileTypes_setAcceptedFileExtensions_throws() {
        var upload = new Upload();
        upload.setAcceptedFileTypes(".txt");

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileExtensions(".pdf"));
    }

    @Test
    void setAcceptedFileTypes_clear_setAcceptedFileExtensions_works() {
        var upload = new Upload();
        upload.setAcceptedFileTypes(".txt", "image/*");
        upload.setAcceptedFileTypes((String[]) null);

        upload.setAcceptedMimeTypes("image/*");
        Assertions.assertEquals(List.of("image/*"),
                upload.getAcceptedMimeTypes());
    }

    @Test
    void setAcceptedFileTypes_clear_setAcceptedMimeTypes_works() {
        var upload = new Upload();
        upload.setAcceptedFileTypes(".txt", "image/*");
        upload.setAcceptedFileTypes((String[]) null);

        upload.setAcceptedFileExtensions(".pdf");
        Assertions.assertEquals(List.of(".pdf"),
                upload.getAcceptedFileExtensions());
    }

    @Test
    void setAcceptedMimeTypes_setAcceptedFileTypes_throws() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileTypes(".txt"));
    }

    @Test
    void setAcceptedFileExtensions_setAcceptedFileTypes_throws() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf");

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileTypes(".txt"));
    }

    @Test
    void setAcceptedMimeTypes_clear_setAcceptedFileTypes_works() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");
        upload.setAcceptedMimeTypes();

        upload.setAcceptedFileTypes("image/*", ".pdf");

        var accept = upload.getElement().getProperty("accept");
        Assertions.assertEquals("image/*,.pdf", accept);
    }

    @Test
    void setAcceptedFileExtensions_clear_setAcceptedFileTypes_works() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf");
        upload.setAcceptedFileExtensions();

        upload.setAcceptedFileTypes("image/*", ".pdf");

        var accept = upload.getElement().getProperty("accept");
        Assertions.assertEquals("image/*,.pdf", accept);
    }

    // --- Receiver guard ---

    @Test
    void setAcceptedMimeTypes_withReceiver_throws() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedMimeTypes("image/*"));
    }

    @Test
    void setAcceptedFileExtensions_withReceiver_throws() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileExtensions(".pdf"));
    }

    @Test
    void setReceiver_withAcceptedMimeTypes_throws() {
        var upload = new Upload();
        upload.setAcceptedMimeTypes("image/*");

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setReceiver(new MemoryBuffer()));
    }

    @Test
    void setReceiver_withAcceptedFileExtensions_throws() {
        var upload = new Upload();
        upload.setAcceptedFileExtensions(".pdf");

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setReceiver(new MemoryBuffer()));
    }

    @Test
    void setAcceptedMimeTypes_clearWithNull_withReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        upload.setAcceptedMimeTypes((String[]) null);
        Assertions.assertTrue(upload.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    void setAcceptedMimeTypes_clearWithEmpty_withReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        upload.setAcceptedMimeTypes();
        Assertions.assertTrue(upload.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    void setAcceptedFileExtensions_clearWithNull_withReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        upload.setAcceptedFileExtensions((String[]) null);
        Assertions.assertTrue(upload.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    void setAcceptedFileExtensions_clearWithEmpty_withReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        upload.setAcceptedFileExtensions(new String[0]);
        Assertions.assertTrue(upload.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    void setFileRestrictionsSeparately_clear_setReceiver_doesNotThrow() {
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
    void setAcceptedFileTypes_withReceiver_doesNotThrow() {
        var upload = new Upload();
        upload.setReceiver(new MemoryBuffer());

        // Deprecated setter should still work with Receiver (client-side only)
        upload.setAcceptedFileTypes("image/*", ".pdf");
        Assertions.assertEquals("image/*,.pdf",
                upload.getElement().getProperty("accept"));
    }

    @Test
    void constructorWithReceiver_setAcceptedMimeTypes_throws() {
        var upload = new Upload(new MemoryBuffer());

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedMimeTypes("image/*"));
    }

    @Test
    void constructorWithReceiver_setAcceptedFileExtensions_throws() {
        var upload = new Upload(new MemoryBuffer());

        Assertions.assertThrows(IllegalStateException.class,
                () -> upload.setAcceptedFileExtensions(".pdf"));
    }
}
