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

import java.io.Serializable;

/**
 * Represents a range of selected text within a field that implements
 * {@link HasSelection}.
 * <p>
 * The {@code start} and {@code end} indices follow the same semantics as
 * {@code HTMLInputElement.selectionStart} and
 * {@code HTMLInputElement.selectionEnd}: zero-based, with {@code start} the
 * index of the first selected character and {@code end} the index after the
 * last selected character. {@code start == end} represents a collapsed
 * selection (a cursor position).
 *
 * @param start
 *            the index of the first selected character, inclusive
 * @param end
 *            the index after the last selected character, exclusive
 * @param content
 *            the selected substring, or an empty string when the selection is
 *            collapsed
 */
public record SelectionRange(int start, int end,
        String content) implements Serializable {

    /**
     * Creates a new {@link SelectionRange}.
     *
     * @param start
     *            the index of the first selected character, inclusive; must be
     *            non-negative and not greater than {@code end}
     * @param end
     *            the index after the last selected character, exclusive
     * @param content
     *            the selected substring, not {@code null}
     */
    public SelectionRange {
        if (start < 0) {
            throw new IllegalArgumentException(
                    "start must be non-negative, got " + start);
        }
        if (end < start) {
            throw new IllegalArgumentException(
                    "end must be greater than or equal to start, got start="
                            + start + ", end=" + end);
        }
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }
    }

    /**
     * Returns the length of the selection.
     *
     * @return {@code end - start}
     */
    public int length() {
        return end - start;
    }

    /**
     * Returns whether the selection is empty (a collapsed cursor position).
     *
     * @return {@code true} if {@code start == end}
     */
    public boolean isEmpty() {
        return start == end;
    }

    /**
     * Returns an empty selection range at position 0.
     *
     * @return an empty {@link SelectionRange}
     */
    public static SelectionRange empty() {
        return new SelectionRange(0, 0, "");
    }
}
