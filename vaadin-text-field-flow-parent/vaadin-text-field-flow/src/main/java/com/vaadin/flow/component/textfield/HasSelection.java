/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.function.SerializableConsumer;
import elemental.json.JsonObject;

import java.io.Serializable;

/**
 * Mixin interface for input fields that use cursor and can select a portion characters
 * in it. Provides implementations for methods like setSelectionRange() and selectAll().
 * The default implementations work for web components that contain the low level input element in "this.inputElement".
 */
public interface HasSelection extends HasElement {

    /**
     * Selects all text in the field and moves the focus to the field.
     */
    default void selectAll() {
        getElement().executeJs("this.inputElement.select();");
    };

    /**
     * Sets the start and end positions of the current text selection.
     * <p>Note, that the method simply proxies the parameters to the similarly named method in the browser, without sanity checks or any synchronization.</p>
     *
     * @param selectionStart The 0-based index of the first selected character. An index greater than the length of the element's value is treated as pointing to the end of the value.
     * @param selectionEnd The 0-based index for the end of the selection (exclusive). An index greater than the length of the element's value is treated as pointing to the end of the value.
     */
    default void setSelectionRange(int selectionStart, int selectionEnd) {
        getElement().executeJs("this.inputElement.setSelectionRange($0,$1);",selectionStart,selectionEnd);
    };

    /**
     * Sets the cursor position to given index.
     *
     * @param cursorPosition the cursor position
     */
    default void setCursorPosition(int cursorPosition) {
        setSelectionRange(cursorPosition, cursorPosition);
    };

    interface SelectionRangeCallback extends Serializable {
        /**
         * This method is called with the current selection of the
         * field.
         *
         * @param start the start of the selection (inclusive)
         * @param end the end of the selection (inclusive)
         * @param content the string content currently selected
         */
        void selectionRange(int start, int end, String content);
    }

    /**
     * Asynchronously gets the current selection for this field.
     *
     * @param callback the callback to notify the selection
     */
    default void getSelectionRange(SelectionRangeCallback callback) {
        getElement().executeJs("" +
                "var res = {};" +
                "res.start = this.inputElement.selectionStart;" +
                "res.end = this.inputElement.selectionEnd;" +
                "res.content = this.inputElement.value.substring(res.start, res.end);" +
                "return res;").then(jsonValue -> {
            if (jsonValue instanceof JsonObject) {
                JsonObject jso = (JsonObject) jsonValue;
                callback.selectionRange(
                        (int) jso.getNumber("start"),
                        (int) jso.getNumber("end"),
                        jso.getString("content")
                );
            }
        });
    };

    /**
     * Asynchronously gets the current cursor position for this field.
     *
     * @param callback the callback to notify the position
     */
    default void getCursorPosition(SerializableConsumer<Integer> callback) {
        this.getSelectionRange( (start, e, c) -> {
            callback.accept(start);
        });
    };

}
