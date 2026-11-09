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
package com.vaadin.flow.component.ai.tests;

import java.util.List;
import java.util.UUID;

import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.ai.orchestrator.RequestListener;
import com.vaadin.flow.component.ai.orchestrator.ResponseListener;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.router.Route;

/**
 * Test page for the client-side lock that {@link FormAIController} puts on a
 * field made writable during a turn. The cost center field is read-only until
 * the trip type is "Business". Drives one AI turn in three server round trips:
 * start the turn, write the trip type through the controller's
 * {@code fill_form} tool, and finish the turn. "Check" shows the cost center's
 * server-side read-only state.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-field-lock")
public class AIFieldLockPage extends VerticalLayout {

    public AIFieldLockPage() {
        var tripType = new TextField("Trip type");
        tripType.setId("trip-type");

        var costCenter = new TextField("Cost center");
        costCenter.setId("cost-center");
        costCenter.setReadOnly(true);
        tripType.addValueChangeListener(event -> costCenter
                .setReadOnly(!"Business".equals(event.getValue())));

        var form = new VerticalLayout(tripType, costCenter);
        var controller = new FormAIController(form);

        var startTurn = new NativeButton("Start turn",
                event -> controller.onRequest(
                        new RequestListener.RequestEvent("Business trip",
                                UUID.randomUUID().toString(), List.of())));
        startTurn.setId("start-turn");

        var fill = new NativeButton("Fill", event -> controller.getTools()
                .stream().filter(tool -> tool.getName().equals("fill_form"))
                .findFirst().orElseThrow().execute(JacksonUtils.readTree(
                        "{\"values\": {\"trip-type\": \"Business\"}}")));
        fill.setId("fill");

        var finishTurn = new NativeButton("Finish turn",
                event -> controller.onResponse(
                        new ResponseListener.ResponseEvent("", null, null)));
        finishTurn.setId("finish-turn");

        var serverReadOnly = new Span();
        serverReadOnly.setId("server-read-only");
        var check = new NativeButton("Check", event -> serverReadOnly
                .setText(String.valueOf(costCenter.isReadOnly())));
        check.setId("check");

        add(form, startTurn, fill, finishTurn, check, serverReadOnly);
    }
}
