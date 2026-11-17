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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.clipboard.Clipboard;
import com.vaadin.flow.component.trigger.internal.Action;
import com.vaadin.flow.component.trigger.internal.Trigger;
import com.vaadin.flow.component.trigger.internal.TriggerInputJs;
import com.vaadin.flow.component.trigger.internal.WriteToClipboardAction;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.dom.JsFunction;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.js.JsCall;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;
import com.vaadin.flow.server.CspMode;
import com.vaadin.flow.shared.Registration;
import com.vaadin.tests.MockUIExtension;

class LitRendererTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @JsDefinition
    interface CopyTemplates extends Serializable {

        @JsExpression("return html`<span>${item.email}</span><button @click=${() => functions.copy(item.email)}>Copy</button>`;")
        CompiledLitTemplate email();
    }

    @Test
    void doNotAllowFunctionNamesWithFunctions() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction(
                        "foo=0; alert(\"gotcha\"); const bar", item -> {
                        }));
    }

    @Test
    void doNotAllowFunctionNamesWithSpaces() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction("illegal name", item -> {
                }));
    }

    @Test
    void doNotAllowFunctionNamesWithDots() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction("illegal.name", item -> {
                }));
    }

    @Test
    void doNotAllowFunctionNamesWithParenthesis() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction("illegalname()", item -> {
                }));
    }

    @Test
    void allowAlphaNumericFunctionNames() {
        LitRenderer.of("<div></div>").withFunction("legalName1", item -> {
        });
    }

    @Test
    void supportGettingValueProviders() {
        LitRenderer<?> renderer = LitRenderer.of("<div></div>")
                .withProperty("foo", item -> 1).withProperty("bar", item -> 2);

        Assertions.assertTrue(
                renderer.getValueProviders().keySet().contains("foo"));
        Assertions.assertTrue(
                renderer.getValueProviders().keySet().contains("bar"));
        Assertions.assertTrue(renderer.getValueProviders().size() == 2);
    }

    @Test
    void clientAction_oneBindingSendsTheActionFunctionToTheClient() {
        TestDiv container = new TestDiv();
        ui.add(container);

        // A declared template, so that client actions work with a strict
        // Content Security Policy too
        LitRenderer<String> renderer = LitRenderer
                .<String> of(JsSnippets.of(CopyTemplates.class).email())
                .withProperty("email", ValueProvider.identity())
                .withClientAction("copy", Clipboard.writeText());

        renderer.render(container.getElement(), new KeyMapper<>(), "renderer");
        ui.getUI().getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();

        List<Object> arguments = getRendererArguments(container,
                "setCompiledLitRenderer");

        // The action names the template can call, and the functions behind
        // them: one function for the whole renderer, not one per item.
        Assertions.assertEquals("[\"copy\"]", arguments.get(6).toString());
        JsFunction[] actions = (JsFunction[]) arguments.get(7);
        Assertions.assertEquals(1, actions.length);
        JsFunction copy = actions[0];

        // The actions come from JavaScript declared in the bundle, so only
        // their identity and captures are sent.
        // The renderer calls each action with the event and the context that
        // carries the template's call arguments.
        JsCall copyCall = Objects.requireNonNull(copy.getDeclaredCall());
        Assertions.assertEquals(WriteToClipboardAction.WriteToClipboardJs.class,
                copyCall.definitionType());
        Assertions.assertEquals("writeToClipboard", copyCall.methodName());
        Assertions.assertEquals(List.of("event", "context"),
                copyCall.getFunctionParameterNames());

        // The copied value is the argument the template passes, so the same
        // function serves every rendered row.
        JsFunction text = (JsFunction) copy.getCaptures().get(0);
        JsCall textCall = Objects.requireNonNull(text.getDeclaredCall());
        Assertions.assertEquals(TriggerInputJs.class,
                textCall.definitionType());
        Assertions.assertEquals("readContextValue", textCall.methodName());
        Assertions.assertEquals(List.of("event", "context"),
                textCall.getFunctionParameterNames());
        Assertions.assertEquals(List.of("args", "0"), text.getCaptures());
    }

    @Test
    void noClientActions_clientStillGetsAnEmptyBundle() {
        ui.setCspMode(CspMode.OFF);
        TestDiv container = new TestDiv();
        ui.add(container);

        LitRenderer.<String> of("<span>${item}</span>")
                .render(container.getElement(), new KeyMapper<>(), "renderer");
        ui.getUI().getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();

        List<Object> arguments = getRendererArguments(container,
                "setLitRenderer");

        Assertions.assertEquals("[]", arguments.get(6).toString());
        Assertions.assertArrayEquals(new JsFunction[0],
                (JsFunction[]) arguments.get(7));
    }

    @Test
    void removingTheRenderer_unbindsItsClientActions() {
        ui.setCspMode(CspMode.OFF);
        TestDiv container = new TestDiv();
        ui.add(container);
        AtomicBoolean unbound = new AtomicBoolean();
        Action action = new Action() {
            @Override
            protected JsFunction toJs(Trigger trigger) {
                trigger.addCleanup(() -> unbound.set(true));
                return JsSnippets.of(TriggerInputJs.class).returnNull();
            }
        };

        Registration registration = LitRenderer.<String> of("<span></span>")
                .withClientAction("act", action)
                .render(container.getElement(), new KeyMapper<>(), "renderer")
                .getRegistration();
        Assertions.assertFalse(unbound.get());

        registration.remove();
        Assertions.assertTrue(unbound.get(),
                "removing the renderer must unbind its client actions");
    }

    @Test
    void doNotAllowClientActionNamesWithSpaces() {
        LitRenderer<Object> renderer = LitRenderer.of("");
        Action action = Clipboard.writeText();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> renderer.withClientAction("illegal name", action));
    }

    @Test
    void doNotAllowClientActionNameAlreadyUsedByAFunction() {
        LitRenderer<Object> renderer = LitRenderer.of("").withFunction("copy",
                item -> {
                });
        Action action = Clipboard.writeText();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> renderer.withClientAction("copy", action));
    }

    private List<Object> getRendererArguments(TestDiv container,
            String methodName) {
        return ui.dumpJsCalls(container.getElement(), LitRendererJs.class)
                .stream().filter(call -> methodName.equals(call.methodName()))
                .findFirst().orElseThrow().arguments();
    }
}
