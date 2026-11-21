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
package com.vaadin.flow.component.dialog;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsInvokers;

/**
 * The JavaScript that {@link Dialog} runs on its element in the browser, for
 * {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
interface DialogJs extends JsInvokers {

    /**
     * Adds a listener that lets the server decide whether the dialog closes
     * when the overlay is about to close, unless the close event was prevented
     * by another listener on the document.
     */
    @JsExpression("""
              this.$.overlay.addEventListener('vaadin-overlay-close', () => {
                document.addEventListener('vaadin-overlay-close', (e) => {
                  if (!e.defaultPrevented && e.detail.overlay === this.$.overlay) {
                    e.preventDefault();
                    this.$server.handleClientClose();
                  }
                }, { once: true });
              });
            """)
    void registerClientCloseHandler();

    /**
     * Sets an inline style property on the overlay part of the dialog.
     *
     * @param name
     *            the style property name, such as {@code min-width}
     * @param value
     *            the style value, or {@code null} to remove it
     */
    @JsExpression("this.$.overlay.$.overlay.style[$0]=$1")
    void setOverlayStyle(String name, String value);
}
