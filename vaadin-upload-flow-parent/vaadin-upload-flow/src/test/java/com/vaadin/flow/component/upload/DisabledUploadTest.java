/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.upload;

import org.junit.Assert;
import org.junit.Test;

public class DisabledUploadTest {
    @Test
    public void preventsStartingUploads() {
        Upload upload = new Upload();
        upload.setEnabled(false);

        IllegalStateException exception = Assert
                .assertThrows(IllegalStateException.class, () -> {
                    upload.getStreamVariable().streamingStarted(null);
                });
        Assert.assertTrue(exception.getMessage().contains(
                "Cannot start upload because the Upload component is disabled"));
    }
}
