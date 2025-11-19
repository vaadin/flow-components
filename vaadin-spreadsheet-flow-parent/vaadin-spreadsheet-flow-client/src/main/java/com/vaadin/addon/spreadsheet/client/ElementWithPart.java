/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * A JsType interface representing a native JavaScript {@code HTMLElement} that
 * includes a {@code part} attribute. This allows Java code to interact with the
 * {@code part} property of an element, which is used for styling components
 * within a shadow DOM.
 *
 * @see <a href=
 *      "https://developer.mozilla.org/en-US/docs/Web/API/Element/part">Element.part
 *      on MDN</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "HTMLElement")
public interface ElementWithPart {

    /**
     * Gets the part names of the element. The returned {@link DOMTokenList} is
     * a live representation of the 'part' attribute of the element.
     *
     * @return a {@link DOMTokenList} of the part names
     */
    @JsProperty
    DOMTokenList getPart();
}
