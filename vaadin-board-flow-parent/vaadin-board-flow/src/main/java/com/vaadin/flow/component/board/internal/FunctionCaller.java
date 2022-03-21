package com.vaadin.flow.component.board.internal;

/*-
 * #%L
 * Vaadin Board for Vaadin 10
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;

public class FunctionCaller {

    /**
     * An internal helper for executing a JS function only once after other
     * updates have been done.
     *
     * @param component
     *            the component with an element to call the function on
     * @param function
     *            the name of the function to call
     */
    public static void callOnceOnClientReponse(Component component,
            String function) {
        String trackingProperty = "CALLONCE_" + function;
        Element element = component.getElement();
        if (element.hasProperty(trackingProperty)) {
            return;
        }
        element.setProperty(trackingProperty, true);

        element.callJsFunction(function);
        element.getNode().runWhenAttached(ui -> {
            ui.beforeClientResponse(component, context -> {
                element.removeProperty(trackingProperty);
            });
        });

    }
}
