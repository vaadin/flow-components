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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.signals.Signal;

/**
 * Mixin interface for field components that wrap a native HTML input and
 * support programmatic control of the text selection.
 * <p>
 * The methods mirror {@code HTMLInputElement.setSelectionRange()} /
 * {@code selectionStart} / {@code selectionEnd}: indices are zero-based, with
 * {@code selectionStart} the index of the first selected character and
 * {@code selectionEnd} the index after the last selected character.
 */
public interface HasSelection extends HasElement {

    /**
     * Selects the entire current value and focuses the field.
     * <p>
     * The browser paints the selection in a faded color when the field does not
     * have focus, so this method focuses the field by default to make the
     * highlight visible. Use {@link #selectAll(boolean)} with {@code false} to
     * apply the selection without moving focus.
     */
    default void selectAll() {
        selectAll(true);
    }

    /**
     * Selects the entire current value, optionally focusing the field.
     *
     * @param focus
     *            {@code true} to focus the field so the selection is rendered
     *            in the active color; {@code false} to leave focus untouched
     *            (the selection will be painted in the browser's faded inactive
     *            color)
     */
    default void selectAll(boolean focus) {
        // Defer with setTimeout so the call runs after any pending value or
        // focus reflection on the web component finishes — otherwise a
        // re-render of the input can wipe the selection we just set.
        getElement().executeJs(
                "setTimeout(() => { const i = this.inputElement; if (i) { if ($0) i.focus(); i.select(); } }, 0)",
                focus);
    }

    /**
     * Collapses the current selection at its end position, leaving the cursor
     * there. Has no visible effect on the value and does not change focus.
     */
    default void deselect() {
        getElement().executeJs(
                "setTimeout(() => { const i = this.inputElement; if (i) { const e = i.selectionEnd || 0; i.setSelectionRange(e, e); } }, 0)");
    }

    /**
     * Sets the text selection to the range
     * {@code [selectionStart, selectionEnd)} and focuses the field.
     * {@code selectionStart == selectionEnd} collapses the selection and moves
     * the cursor to that position.
     * <p>
     * Indices outside the current value are clamped by the browser; passing
     * {@code 0, Integer.MAX_VALUE} therefore selects the whole value.
     * <p>
     * Use {@link #setSelectionRange(int, int, boolean)} with {@code false} to
     * apply the selection without moving focus.
     *
     * @param selectionStart
     *            the index of the first selected character, inclusive
     * @param selectionEnd
     *            the index after the last selected character, exclusive
     */
    default void setSelectionRange(int selectionStart, int selectionEnd) {
        setSelectionRange(selectionStart, selectionEnd, true);
    }

    /**
     * Sets the text selection to the range
     * {@code [selectionStart, selectionEnd)}, optionally focusing the field.
     *
     * @param selectionStart
     *            the index of the first selected character, inclusive
     * @param selectionEnd
     *            the index after the last selected character, exclusive
     * @param focus
     *            {@code true} to focus the field so the selection is rendered
     *            in the active color; {@code false} to leave focus untouched
     */
    default void setSelectionRange(int selectionStart, int selectionEnd,
            boolean focus) {
        getElement().executeJs(
                "setTimeout(() => { const i = this.inputElement; if (i) { if ($2) i.focus(); i.setSelectionRange($0, $1); } }, 0)",
                selectionStart, selectionEnd, focus);
    }

    /**
     * Moves the cursor to the given position, collapsing any current selection,
     * and focuses the field. Equivalent to {@link #setSelectionRange(int, int)
     * setSelectionRange(position, position)}.
     *
     * @param position
     *            the cursor position, zero-based
     */
    default void setCursorPosition(int position) {
        setCursorPosition(position, true);
    }

    /**
     * Moves the cursor to the given position, collapsing any current selection,
     * optionally focusing the field. Equivalent to
     * {@link #setSelectionRange(int, int, boolean) setSelectionRange(position,
     * position, focus)}.
     *
     * @param position
     *            the cursor position, zero-based
     * @param focus
     *            {@code true} to focus the field; {@code false} to leave focus
     *            untouched
     */
    default void setCursorPosition(int position, boolean focus) {
        setSelectionRange(position, position, focus);
    }

    /**
     * Returns a {@link Signal} that reactively reflects the current text
     * selection in this field.
     * <p>
     * The signal value is updated whenever the user changes the selection or
     * cursor position (via mouse, keyboard, or programmatic
     * {@code setSelectionRange}). Reading via {@link Signal#peek()} or mapping
     * with {@link Signal#map} provides the current {@link SelectionRange}
     * without manual event-listener boilerplate.
     * <p>
     * The same signal instance is returned across calls. Until the field is
     * first attached to a UI, the signal returns {@link SelectionRange#empty}.
     *
     * @return a {@link Signal} carrying the current {@link SelectionRange};
     *         never {@code null}
     */
    default Signal<SelectionRange> selectionSignal() {
        return SelectionSignalSupport.getOrCreate((Component) this);
    }
}
