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
package com.vaadin.flow.data.renderer;

import java.io.Serializable;

import com.vaadin.flow.internal.nodefeature.ReturnChannelRegistration;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

import tools.jackson.databind.node.ArrayNode;

/**
 * The JavaScript that {@link LitRenderer} and {@link ComponentRenderer} run in
 * the browser, for {@link com.vaadin.flow.dom.Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface LitRendererJs extends Serializable {

    /**
     * Sets a renderer that compiles the template from a string. Called on the
     * element that owns the renderer.
     */
    @JsExpression("window.Vaadin.setLitRenderer(this, $0, $1, $2, $3, $4, $5);")
    void setLitRenderer(String rendererName, String templateExpression,
            ReturnChannelRegistration returnChannel, ArrayNode clientCallables,
            String propertyNamespace, String appId);

    /**
     * Sets a renderer that runs a template declared in the bundle. Called on
     * the element that owns the renderer.
     */
    @JsExpression("window.Vaadin.setLitRendererTemplate(this, $0, $1, $2, $3, $4, $5);")
    void setLitRendererTemplate(String rendererName,
            CompiledLitTemplate template,
            ReturnChannelRegistration returnChannel, ArrayNode clientCallables,
            String propertyNamespace, String appId);

    /**
     * Removes the renderer, unless another renderer replaced it. Called on the
     * element that owns the renderer.
     */
    @JsExpression("window.Vaadin.unsetLitRenderer(this, $0, $1);")
    void unsetLitRenderer(String rendererName, String propertyNamespace);

    /**
     * Patches the virtual container of a {@link ComponentRenderer}. Called on
     * the container.
     */
    @JsExpression("Vaadin.FlowComponentHost.patchVirtualContainer(this);")
    void patchVirtualContainer();
}
