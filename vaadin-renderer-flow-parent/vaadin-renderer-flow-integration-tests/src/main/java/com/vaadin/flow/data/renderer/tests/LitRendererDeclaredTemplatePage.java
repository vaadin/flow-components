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
package com.vaadin.flow.data.renderer.tests;

import java.io.Serializable;
import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.CompiledLitTemplate;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;
import com.vaadin.flow.router.Route;

@Route("vaadin-renderer-flow/lit-renderer-declared-template")
public class LitRendererDeclaredTemplatePage extends Div {

    static final List<String> ITEMS = List.of("a", "b", "c");

    @JsDefinition
    interface Templates extends Serializable {

        /**
         * Renders each argument of the template, so that the test can check
         * that every name is bound to the value the client passes for it.
         */
        @JsExpression("""
                return html`<div class="args"
                        data-root=${root.className}
                        data-app-id=${appId}
                        data-item-key=${itemKey}
                        data-model-index=${model.index}
                        data-index=${index}
                        data-value=${item.value}
                        data-arg0=${$0}
                        data-arg1=${$1}
                        @click=${() => functions.clicked(itemKey)}>${item.value}</div>
                    <input type="checkbox" .checked=${live(item.checked)}>`;
                """)
        CompiledLitTemplate allArguments(String arg0, int arg1);
    }

    public LitRendererDeclaredTemplatePage() {
        LitRendererTestComponentWrapper component = new LitRendererTestComponentWrapper();
        component.setItems(ITEMS);

        Span appId = new Span(UI.getCurrent().getInternals().getAppId());
        appId.setId("app-id");

        Span clicked = new Span();
        clicked.setId("clicked");

        component
                .setRenderer(LitRenderer
                        .<String> of(JsSnippets.of(Templates.class)
                                .allArguments("first", 42))
                        .withProperty("value", ValueProvider.identity())
                        .withProperty("checked", item -> false)
                        .withFunction("clicked",
                                (item, args) -> clicked.setText(item + ":"
                                        + args.get(0).asString() + ":"
                                        + component.getItemKey(item))));

        NativeButton refresh = new NativeButton("Refresh",
                e -> component.setItems(ITEMS));
        refresh.setId("refresh");

        add(component, appId, clicked, refresh);
    }
}
