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
package com.vaadin.flow.component.upload;

import java.util.Arrays;

/**
 * Specifies which device input to use for capturing a new file, for example a
 * photo with the camera, instead of choosing an existing file.
 *
 * @see Upload#setCapture(UploadCapture)
 * @see UploadButton#setCapture(UploadCapture)
 * @since 25.4
 */
public enum UploadCapture {
    /**
     * Use the user-facing device input, for example the front camera of a
     * phone.
     */
    USER("user"),
    /**
     * Use the outward-facing device input, for example the back camera of a
     * phone.
     */
    ENVIRONMENT("environment");

    private final String value;

    UploadCapture(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

    static UploadCapture fromValue(String value) {
        return Arrays.stream(values())
                .filter(capture -> capture.value.equals(value)).findFirst()
                .orElse(null);
    }
}
