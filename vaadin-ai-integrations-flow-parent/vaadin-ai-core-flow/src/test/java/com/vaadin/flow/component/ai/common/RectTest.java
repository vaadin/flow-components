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
 * Tests for {@link Rect}'s constructor validation: all values are fractions, so
 * x and y sit in the {@code 0..1} range and the rectangle has a size — width
 * and height are greater than {@code 0} and at most {@code 1}.
 */
class RectTest {

    @Test
    void boundaryValuesAreAccepted() {
        // The 0..1 range is inclusive at both ends for x and y, and
        // inclusive at 1 for width and height: a rectangle can start at
        // the page edge and span the whole page.
        Assertions.assertDoesNotThrow(() -> new Rect(0, 0, 1, 1));
        Assertions.assertDoesNotThrow(() -> new Rect(1, 1, 0.5, 0.5));
    }

    @Test
    void outOfRangeCornerIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Rect(-0.1, 0.2, 0.3, 0.04));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Rect(1.5, 0.2, 0.3, 0.04));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Rect(0.1, -0.2, 0.3, 0.04));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Rect(0.1, 1.2, 0.3, 0.04));
    }

    @Test
    void sizelessRectIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Rect(0.1, 0.2, 0, 0.04));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Rect(0.1, 0.2, 0.3, 0));
    }

    @Test
    void oversizedRectIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Rect(0.1, 0.2, 1.5, 0.04));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Rect(0.1, 0.2, 0.3, 1.5));
    }

    @Test
    void nanIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Rect(Double.NaN, 0.2, 0.3, 0.04));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Rect(0.1, 0.2, Double.NaN, 0.04));
    }
}
