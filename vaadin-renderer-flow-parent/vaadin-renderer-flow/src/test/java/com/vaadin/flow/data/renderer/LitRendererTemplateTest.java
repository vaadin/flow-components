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
import java.text.NumberFormat;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsCall;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;
import com.vaadin.flow.server.CspMode;
import com.vaadin.flow.server.CspViolationException;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.shared.Registration;
import com.vaadin.tests.MockUIExtension;

/**
 * Covers which template a {@link LitRenderer} sends to the browser: a declared
 * {@link CompiledLitTemplate}, or a string that the browser compiles, which a
 * strict Content Security Policy forbids.
 */
class LitRendererTemplateTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private Element container;

    @JsDefinition
    interface TestTemplates extends Serializable {

        @JsExpression("return html`<span title=${$0}>${item.name}</span>`;")
        CompiledLitTemplate name(String title);
    }

    @BeforeEach
    void setup() {
        container = new Element("div");
        ui.getUI().getElement().appendChild(container);
        ui.dumpPendingJavaScriptInvocations();
    }

    @AfterEach
    void clearService() {
        VaadinService.setCurrent(null);
    }

    @Test
    void declaredTemplate_setsTemplateRenderer() {
        CompiledLitTemplate template = JsSnippets.of(TestTemplates.class)
                .name("Name");

        render(LitRenderer.<String> of(template));

        JsCall call = getSingleRendererCall();
        Assertions.assertEquals("setLitRendererTemplate", call.methodName());
        Assertions.assertEquals("renderer", call.arguments().get(0));
        Assertions.assertSame(template, call.arguments().get(1));
    }

    @Test
    void stringTemplate_setsStringRenderer() {
        render(LitRenderer.<String> of("<span>${item.name}</span>"));

        JsCall call = getSingleRendererCall();
        Assertions.assertEquals("setLitRenderer", call.methodName());
        Assertions.assertEquals("<span>${item.name}</span>",
                call.arguments().get(1));
    }

    @Test
    void strictCsp_declaredTemplate_rendered() {
        enableStrictCsp();
        CompiledLitTemplate template = JsSnippets.of(TestTemplates.class)
                .name("Name");

        Assertions.assertDoesNotThrow(
                () -> render(LitRenderer.<String> of(template)));
    }

    @Test
    void strictCsp_stringTemplate_throws() {
        enableStrictCsp();
        LitRenderer<String> renderer = LitRenderer
                .of("<span>${item.name}</span>");

        Assertions.assertThrows(CspViolationException.class,
                () -> render(renderer));
    }

    @Test
    void strictCsp_builtInRenderers_useDeclaredTemplates() {
        enableStrictCsp();

        List<LitRenderer<String>> renderers = List.of(
                new ComponentRenderer<>(item -> new TestDiv()),
                new TextRenderer<>(), new NativeButtonRenderer<>("Click"),
                new NumberRenderer<>(item -> 1, NumberFormat.getInstance()));

        for (LitRenderer<String> renderer : renderers) {
            render(renderer);
            JsCall call = getSingleRendererCall();
            Assertions.assertEquals("setLitRendererTemplate", call.methodName(),
                    renderer.getClass().getSimpleName()
                            + " should render with a declared template");
        }
    }

    @Test
    void subclassOverridingTemplateExpression_usesStringTemplate() {
        TextRenderer<String> renderer = new TextRenderer<>() {
            @Override
            protected String getTemplateExpression() {
                return "<b>${item.label}</b>";
            }
        };

        render(renderer);

        JsCall call = getSingleRendererCall();
        Assertions.assertEquals("setLitRenderer", call.methodName());
        Assertions.assertEquals("<b>${item.label}</b>",
                call.arguments().get(1));
    }

    @Test
    void subclassOverridingTemplateExpressionAndTemplate_usesDeclaredTemplate() {
        CompiledLitTemplate template = JsSnippets.of(TestTemplates.class)
                .name("Name");
        TextRenderer<String> renderer = new TextRenderer<>() {
            @Override
            protected String getTemplateExpression() {
                return "<b>${item.label}</b>";
            }

            @Override
            protected CompiledLitTemplate getTemplate() {
                return template;
            }
        };

        render(renderer);

        JsCall call = getSingleRendererCall();
        Assertions.assertEquals("setLitRendererTemplate", call.methodName());
        Assertions.assertSame(template, call.arguments().get(1));
    }

    @Test
    void reattach_setsTemplateRendererAgain() {
        render(LitRenderer
                .<String> of(JsSnippets.of(TestTemplates.class).name("Name")));
        ui.dumpPendingJavaScriptInvocations();

        container.removeFromParent();
        ui.getUI().getElement().appendChild(container);

        JsCall call = getSingleRendererCall();
        Assertions.assertEquals("setLitRendererTemplate", call.methodName());
    }

    @Test
    void removeRegistration_unsetsRenderer() {
        LitRenderer<String> renderer = LitRenderer
                .of(JsSnippets.of(TestTemplates.class).name("Name"));
        Registration registration = renderer
                .render(container, new KeyMapper<>(), "renderer")
                .getRegistration();
        ui.dumpPendingJavaScriptInvocations();

        registration.remove();

        JsCall call = getSingleRendererCall();
        Assertions.assertEquals("unsetLitRenderer", call.methodName());
        Assertions.assertEquals("renderer", call.arguments().get(0));
    }

    private void render(LitRenderer<String> renderer) {
        renderer.render(container, new KeyMapper<>(), "renderer");
    }

    /**
     * Gets the one renderer call made on the container, leaving out the virtual
     * container patch of a {@link ComponentRenderer}.
     */
    private JsCall getSingleRendererCall() {
        List<JsCall> calls = ui.dumpJsCalls(container, LitRendererJs.class)
                .stream().filter(call -> !"patchVirtualContainer"
                        .equals(call.methodName()))
                .toList();
        Assertions.assertEquals(1, calls.size());
        return calls.get(0);
    }

    private void enableStrictCsp() {
        Mockito.when(ui.getService().getDeploymentConfiguration().getCspMode())
                .thenReturn(CspMode.STRICT);
        VaadinService.setCurrent(ui.getService());
    }
}
