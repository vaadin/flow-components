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
package com.vaadin.flow.component.shared;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SelectionRangeTest {

    @Test
    void length_returnsEndMinusStart() {
        Assertions.assertEquals(0, new SelectionRange(0, 0, "").length());
        Assertions.assertEquals(4, new SelectionRange(2, 6, "abcd").length());
        Assertions.assertEquals(7,
                new SelectionRange(0, 7, "Lorem i").length());
    }

    @Test
    void isEmpty_trueWhenStartEqualsEnd() {
        Assertions.assertTrue(new SelectionRange(0, 0, "").isEmpty());
        Assertions.assertTrue(new SelectionRange(7, 7, "").isEmpty());
        Assertions.assertFalse(new SelectionRange(2, 6, "abcd").isEmpty());
    }

    @Test
    void empty_returnsZeroZeroEmpty() {
        SelectionRange empty = SelectionRange.empty();
        Assertions.assertEquals(0, empty.start());
        Assertions.assertEquals(0, empty.end());
        Assertions.assertEquals("", empty.content());
        Assertions.assertTrue(empty.isEmpty());
    }

    @Test
    void equality_recordSemantics() {
        Assertions.assertEquals(new SelectionRange(2, 5, "abc"),
                new SelectionRange(2, 5, "abc"));
        Assertions.assertEquals(new SelectionRange(2, 5, "abc").hashCode(),
                new SelectionRange(2, 5, "abc").hashCode());
        Assertions.assertNotEquals(new SelectionRange(2, 5, "abc"),
                new SelectionRange(2, 6, "abcd"));
    }

    @Test
    void negativeStart_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SelectionRange(-1, 0, ""));
    }

    @Test
    void endBeforeStart_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SelectionRange(5, 2, ""));
    }

    @Test
    void nullContent_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SelectionRange(0, 0, null));
    }
}
