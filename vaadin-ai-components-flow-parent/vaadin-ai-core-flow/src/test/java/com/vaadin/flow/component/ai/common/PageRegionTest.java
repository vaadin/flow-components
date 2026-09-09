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
package com.vaadin.flow.component.ai.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PageRegion}'s constructor validation: the rectangle is
 * required and page numbers start at 1.
 */
class PageRegionTest {

    private static final Rect RECT = new Rect(0.1, 0.2, 0.3, 0.04);

    @Test
    void nullRectIsRejected() {
        var thrown = Assertions.assertThrows(NullPointerException.class,
                () -> new PageRegion(1, null));
        Assertions.assertEquals("Rect must not be null", thrown.getMessage(),
                "The guard must fail fast with its own message, not through "
                        + "a downstream NPE");
    }

    @Test
    void pageBelowOneIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new PageRegion(0, RECT));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new PageRegion(-1, RECT));
    }

    @Test
    void firstPageIsAccepted() {
        var region = new PageRegion(1, RECT);

        Assertions.assertEquals(1, region.page(),
                "Page numbers start at 1, so the first page is valid");
    }
}
