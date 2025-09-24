/*
 * Copyright 2000-2025 Vaadin Ltd.
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
