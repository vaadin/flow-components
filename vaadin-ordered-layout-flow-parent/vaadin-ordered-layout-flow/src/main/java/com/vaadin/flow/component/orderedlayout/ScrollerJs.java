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
package com.vaadin.flow.component.orderedlayout;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsInvokers;

/**
 * The JavaScript that {@link Scroller} runs on its element in the browser, for
 * {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
interface ScrollerJs extends JsInvokers {

    /**
     * Scrolls the scroller to the top.
     */
    @JsExpression("this.scrollTop = 0")
    void scrollToTop();

    /**
     * Scrolls the scroller to the bottom.
     */
    @JsExpression("this.scrollTop = this.scrollHeight - this.clientHeight")
    void scrollToBottom();
}
