/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.state;

import java.io.Serializable;

/**
 * Generic interface for capturing and restoring AI-related component state.
 * <p>
 * This interface provides a unified mechanism for managing state across
 * different AI features (charts, grids, dashboards, forms, etc.). Feature
 * modules provide their own implementations.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * // Create state support for a chart
 * AiStateSupport&lt;ChartState&gt; chartStateSupport = new ChartStateSupport(
 *         chart);
 *
 * // Capture current state
 * ChartState snapshot = chartStateSupport.capture();
 *
 * // Save to database or session
 * persistToDatabase(snapshot);
 *
 * // Later: restore from saved state
 * ChartState savedState = loadFromDatabase();
 * chartStateSupport.restore(savedState);
 * </pre>
 *
 * @param <S>
 *            the type of state object (must be Serializable)
 * @author Vaadin Ltd
 */
public interface AiStateSupport<S extends Serializable> {

    /**
     * Captures the current state of the component.
     *
     * @return a serializable state object representing the current state
     */
    S capture();

    /**
     * Restores the component to a previously captured state.
     *
     * @param state
     *            the state to restore
     */
    void restore(S state);
}
