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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1633.
 *
 * Setting min to Integer.MIN_VALUE to "remove" the limit makes the min the
 * step basis: with step=10, valid values shift from (..., -10, 0, 10, ...) to
 * (..., -8, 2, 18, ...) because Integer.MIN_VALUE is not treated as a magic
 * "no min" value in step validation (unlike NumberField's infinity).
 */
@Route("repro-1633")
public class Repro1633View extends Div {

    public Repro1633View() {
        IntegerField defaultMin = new IntegerField("step 10, default min");
        defaultMin.setStep(10);
        defaultMin.setStepButtonsVisible(true);
        defaultMin.setId("default-min");
        Span defaultMinState = new Span();
        defaultMinState.setId("default-min-state");
        defaultMin.addValueChangeListener(e -> defaultMinState
                .setText("value=" + e.getValue() + " invalid="
                        + defaultMin.isInvalid() + " message="
                        + defaultMin.getErrorMessage()));

        IntegerField explicitMin = new IntegerField(
                "step 10, min = Integer.MIN_VALUE");
        explicitMin.setStep(10);
        explicitMin.setStepButtonsVisible(true);
        explicitMin.setMin(Integer.MIN_VALUE);
        explicitMin.setId("explicit-min");
        Span explicitMinState = new Span();
        explicitMinState.setId("explicit-min-state");
        explicitMin.addValueChangeListener(e -> explicitMinState
                .setText("value=" + e.getValue() + " invalid="
                        + explicitMin.isInvalid() + " message="
                        + explicitMin.getErrorMessage()));

        add(defaultMin, defaultMinState, explicitMin, explicitMinState);
    }
}
