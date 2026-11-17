/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import java.io.Serializable;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that {@link Map} runs in the browser, for
 * {@link Element#executeJs(Class)}. Unless stated otherwise, the methods are
 * called on the map element.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface MapJs extends Serializable {

    /**
     * Initializes the map connector on the map element.
     */
    @JsExpression("window.Vaadin.Flow.mapConnector.init(this)")
    void initConnector();

    /**
     * Sets the user projection for all maps in the UI. Called on the page
     * rather than on a map element, with
     * {@link com.vaadin.flow.component.page.Page#executeJs(Class)}.
     *
     * @param projection
     *            the user projection to use for all public facing API
     */
    @JsExpression("window.Vaadin.Flow.mapConnector.setUserProjection($0)")
    void setUserProjection(String projection);

    /**
     * Defines a custom coordinate projection for all maps in the UI. Called on
     * the page rather than on a map element, with
     * {@link com.vaadin.flow.component.page.Page#executeJs(Class)}.
     *
     * @param projectionName
     *            the name of the projection
     * @param wksDefinition
     *            the Well Known Text (WKS) definition of the projection
     */
    @JsExpression("window.Vaadin.Flow.mapConnector.defineProjection($0, $1)")
    void defineProjection(String projectionName, String wksDefinition);
}
