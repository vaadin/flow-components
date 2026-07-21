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
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.upload.UploadErrorReason;

class UploadErrorReasonTest {

    @Test
    void fromClientCode_knownCodes() {
        Assertions.assertEquals(UploadErrorReason.SERVER_UNAVAILABLE,
                UploadErrorReason.fromClientCode("serverUnavailable"));
        Assertions.assertEquals(UploadErrorReason.UNEXPECTED_SERVER_ERROR,
                UploadErrorReason.fromClientCode("unexpectedServerError"));
        Assertions.assertEquals(UploadErrorReason.FILE_TOO_LARGE,
                UploadErrorReason.fromClientCode("fileTooLarge"));
        Assertions.assertEquals(UploadErrorReason.FORBIDDEN,
                UploadErrorReason.fromClientCode("forbidden"));
        Assertions.assertEquals(UploadErrorReason.TIMEOUT,
                UploadErrorReason.fromClientCode("timeout"));
    }

    @Test
    void fromClientCode_unknownCode() {
        Assertions.assertEquals(UploadErrorReason.UNKNOWN,
                UploadErrorReason.fromClientCode("someNewCode"));
        Assertions.assertEquals(UploadErrorReason.UNKNOWN,
                UploadErrorReason.fromClientCode(null));
    }

    @Test
    void fromStatusCode_mapsLikeClientSide() {
        Assertions.assertEquals(UploadErrorReason.SERVER_UNAVAILABLE,
                UploadErrorReason.fromStatusCode(0));
        Assertions.assertEquals(UploadErrorReason.UNEXPECTED_SERVER_ERROR,
                UploadErrorReason.fromStatusCode(500));
        Assertions.assertEquals(UploadErrorReason.UNEXPECTED_SERVER_ERROR,
                UploadErrorReason.fromStatusCode(503));
        Assertions.assertEquals(UploadErrorReason.FILE_TOO_LARGE,
                UploadErrorReason.fromStatusCode(413));
        Assertions.assertEquals(UploadErrorReason.FORBIDDEN,
                UploadErrorReason.fromStatusCode(400));
        Assertions.assertEquals(UploadErrorReason.FORBIDDEN,
                UploadErrorReason.fromStatusCode(403));
        Assertions.assertEquals(UploadErrorReason.FORBIDDEN,
                UploadErrorReason.fromStatusCode(404));
    }

    @Test
    void fromStatusCode_nonErrorStatus_returnsUnknown() {
        Assertions.assertEquals(UploadErrorReason.UNKNOWN,
                UploadErrorReason.fromStatusCode(200));
        Assertions.assertEquals(UploadErrorReason.UNKNOWN,
                UploadErrorReason.fromStatusCode(302));
    }
}
