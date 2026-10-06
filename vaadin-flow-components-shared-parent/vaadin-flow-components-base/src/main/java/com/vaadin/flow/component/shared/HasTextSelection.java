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

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.shared.internal.SelectionSignalSupport;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
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
public interface HasTextSelection extends HasElement {

    /**
     * Sets the text selection to the range
     * {@code [selectionStart, selectionEnd)} and focuses the field.
     * {@code selectionStart == selectionEnd} collapses the selection and moves
     * the cursor to that position.
     * <p>
     * Indices outside the current value are clamped by the browser; passing
     * {@code 0, Integer.MAX_VALUE} therefore selects the whole value.
     * <p>
     * Text selection depends on native browser behavior, which differs between
     * browsers and platforms. On mobile devices in particular, focusing the
     * field programmatically may not work.
     *
     * @param selectionStart
     *            the index of the first selected character, inclusive
     * @param selectionEnd
     *            the index after the last selected character, exclusive
     */
    default void setSelectionRange(int selectionStart, int selectionEnd) {
        getElement().executeJs(TextSelectionJs.class)
                .setSelectionRange(selectionStart, selectionEnd);
    }

    /**
     * Selects the entire current value and focuses the field.
     * <p>
     * Text selection depends on native browser behavior, which differs between
     * browsers and platforms. On mobile devices in particular, focusing the
     * field programmatically may not work.
     */
    default void selectAll() {
        setSelectionRange(0, Integer.MAX_VALUE);
    }

    /**
     * Moves the cursor to the given position, collapsing any current selection,
     * and focuses the field. Equivalent to {@link #setSelectionRange(int, int)
     * setSelectionRange(position, position)}.
     * <p>
     * Text selection depends on native browser behavior, which differs between
     * browsers and platforms. On mobile devices in particular, focusing the
     * field programmatically may not work.
     *
     * @param position
     *            the cursor position, zero-based
     */
    default void setCursorPosition(int position) {
        setSelectionRange(position, position);
    }

    /**
     * Collapses the current selection at its end position, leaving the cursor
     * there. Does not change the value or the focus.
     */
    default void deselect() {
        getElement().executeJs(TextSelectionJs.class).deselect();
    }

    /**
     * Returns a read-only signal with the current text selection of the field.
     * <p>
     * The signal updates when the selection or cursor position changes, either
     * through user interaction or through the methods of this interface.
     * Updates are debounced, so a burst of changes, such as typing or
     * drag-selecting, results in a single update with the final selection. You
     * can read the current selection with {@link Signal#peek()}, for example in
     * a click listener, or react to changes in an effect.
     * <p>
     * Each call returns the same signal instance. Until the field is attached
     * and reports its first selection, the value is
     * {@link SelectionRange#empty()}.
     * <p>
     * On iOS and Android, the signal does not update for selection changes made
     * by long-pressing to move the cursor or by dragging the selection handles.
     *
     * @return a signal with the current selection, never {@code null}
     */
    default Signal<SelectionRange> selectionSignal() {
        return SelectionSignalSupport.getOrCreate(this);
    }

    /**
     * For internal use only. May be renamed or removed in a future release.
     */
    @JsDefinition
    interface TextSelectionJs extends Serializable {

        /**
         * Sets the text selection of the input element and focuses the field.
         *
         * @param selectionStart
         *            the index of the first selected character, inclusive
         * @param selectionEnd
         *            the index after the last selected character, exclusive
         */
        // - Defer with setTimeout, a pending re-render could reset the range
        // - Set the range before focus, so that focus scrolls to the new caret
        // - Disable autoselect, otherwise focus selects the whole value
        // - Mark the focus as not from the client, same as Focusable.focus()
        @JsExpression("""
                setTimeout(() => {
                  const i = this.inputElement;
                  if (!i) return;
                  i.setSelectionRange($0, $1);
                  const autoselect = this.autoselect;
                  try {
                    this.autoselect = false;
                    this._nextFocusIsFromClient = false;
                    this.focus();
                  } finally {
                    this.autoselect = autoselect;
                    this._nextFocusIsFromClient = true;
                  }
                }, 0)
                """)
        void setSelectionRange(int selectionStart, int selectionEnd);

        /**
         * Collapses the selection of the input element at its end position.
         */
        // - Defer with setTimeout, same as setSelectionRange
        @JsExpression("""
                setTimeout(() => {
                  const i = this.inputElement;
                  if (!i) return;
                  const end = i.selectionEnd || 0;
                  i.setSelectionRange(end, end);
                }, 0)
                """)
        void deselect();

        /**
         * Adds listeners to the input element that report selection changes to
         * the server with a {@code vaadin-text-selection-change} event.
         */
        // - Install only once per element, the call is repeated on re-attach
        // - Debounce, so that a burst of changes (typing, drag-selecting)
        // results in a single round-trip with the final selection
        // - Skip unchanged selections, coalesced events can repeat them
        // - selectionchange covers caret moves, including collapsing a
        // selection by clicking into it; select is a fallback for browsers
        // without selectionchange on inputs; input and focus cover value
        // edits and the initial state
        @JsExpression("""
                if (this._textSelectionListenerInstalled) return;
                this._textSelectionListenerInstalled = true;
                let last;
                let timer;
                const report = () => {
                  const i = this.inputElement;
                  if (!i) return;
                  const start = i.selectionStart || 0;
                  const end = i.selectionEnd || 0;
                  const content = (i.value || '').substring(start, end);
                  const key = start + ':' + end + ':' + content;
                  if (key === last) return;
                  last = key;
                  this.dispatchEvent(new CustomEvent('vaadin-text-selection-change', {
                    detail: { start, end, content }
                  }));
                };
                const reportDebounced = () => {
                  clearTimeout(timer);
                  timer = setTimeout(report, 100);
                };
                (this.updateComplete || Promise.resolve()).then(() => {
                  const i = this.inputElement;
                  if (!i) return;
                  ['selectionchange', 'select', 'input', 'focus'].forEach(
                    (type) => i.addEventListener(type, reportDebounced));
                  report();
                });
                """)
        void installSelectionListener();
    }
}
