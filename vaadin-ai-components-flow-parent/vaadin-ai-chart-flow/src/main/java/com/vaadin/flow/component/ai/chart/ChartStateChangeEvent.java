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
package com.vaadin.flow.component.ai.chart;

import java.util.EventObject;

/**
 * Event fired when the chart state changes.
 *
 * @author Vaadin Ltd
 */
public class ChartStateChangeEvent extends EventObject {

    private final ChartAIController.ChartState state;

    /**
     * Creates a new chart state change event.
     *
     * @param source
     *            the controller that fired the event
     * @param state
     *            the new chart state
     */
    public ChartStateChangeEvent(ChartAIController source,
            ChartAIController.ChartState state) {
        super(source);
        this.state = state;
    }

    @Override
    public ChartAIController getSource() {
        return (ChartAIController) super.getSource();
    }

    /**
     * Gets the current chart state after the change.
     *
     * @return the chart state
     */
    public ChartAIController.ChartState getState() {
        return state;
    }
}
