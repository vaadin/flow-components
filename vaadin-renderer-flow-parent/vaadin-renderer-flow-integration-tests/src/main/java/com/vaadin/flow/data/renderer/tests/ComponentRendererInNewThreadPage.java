/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd.
 *
 */
@Route("vaadin-renderer-flow/component-renderer-in-new-thread")
public class ComponentRendererInNewThreadPage extends Div {

    private final AtomicInteger nullUiCountOnTemplateExpression = new AtomicInteger(
            0);
    private final AtomicInteger nonNullUiCountOnTemplateExpression = new AtomicInteger(
            0);

    public ComponentRendererInNewThreadPage() {
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
        NativeButton addComponentButton = new NativeButton("Add component",
                e -> {
                    LongRunningTask longRunningTask = new LongRunningTask(
                            component -> e.getSource().getUI()
                                    .ifPresent(ui -> ui.access(() -> {
                                        add(component);

                                        Span nullUiCountOnTemplateExpressionLog = new Span(
                                                nullUiCountOnTemplateExpression
                                                        .toString());
                                        nullUiCountOnTemplateExpressionLog
                                                .setId("null-ui-count");

                                        Span nonNullUiCountOnTemplateExpressionLog = new Span(
                                                nonNullUiCountOnTemplateExpression
                                                        .toString());
                                        nonNullUiCountOnTemplateExpressionLog
                                                .setId("non-null-ui-count");

                                        add(nullUiCountOnTemplateExpressionLog,
                                                nonNullUiCountOnTemplateExpressionLog);
                                    })));
                    longRunningTask.start();
                });
        addComponentButton.setId("add-component");
        add(addComponentButton);
    }

    class LongRunningTask extends Thread {
        private final Consumer<Component> finishedCallback;

        public LongRunningTask(Consumer<Component> finishedCallback) {
            this.finishedCallback = finishedCallback;
        }

        @Override
        public void run() {
            LitRendererTestComponent component = new LitRendererTestComponent();
            component.setItems("Item");
            RendererWithCustomTemplateExpression renderer = new RendererWithCustomTemplateExpression(
                    NativeLabel::new, HasText::setText);
            component.setRenderer(renderer);
            finishedCallback.accept(component);
        }
    }

    class RendererWithCustomTemplateExpression
            extends ComponentRenderer<NativeLabel, String> {

        public RendererWithCustomTemplateExpression(
                SerializableSupplier<NativeLabel> componentSupplier,
                SerializableBiConsumer<NativeLabel, String> itemConsumer) {
            super(componentSupplier, itemConsumer);
        }

        @Override
        protected String getTemplateExpression() {
            if (UI.getCurrent() == null) {
                nullUiCountOnTemplateExpression.incrementAndGet();
            } else {
                nonNullUiCountOnTemplateExpression.incrementAndGet();
            }
            return "DummyExpression";
        }
    }
}
