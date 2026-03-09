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
package com.vaadin.flow.component.ai.orchestrator;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.ai.provider.LLMProvider;

/**
 * Interface for AI controllers that extend orchestrator capabilities by
 * providing tools that the LLM can use.
 * <p>
 * Controllers provide domain-specific tools and functionality to the AI
 * orchestrator. Tools are functions that the AI can call to perform actions like
 * querying databases, creating visualizations, filling forms, etc.
 * </p>
 *
 * @author Vaadin Ltd
 */
public interface AIController extends Serializable {

    /**
     * Returns the tools this controller provides to the LLM.
     * <p>
     * Tools are functions that the AI can call to perform actions. Each tool
     * should have a clear name, description, and parameter schema.
     * </p>
     *
     * @return list of tools, or empty list if controller provides no tools
     */
    default List<LLMProvider.ToolDefinition> getTools() {
        return Collections.emptyList();
    }

    /**
     * Called by the orchestrator when an LLM request cycle has completed.
     * <p>
     * This method is invoked after all tool executions for a given user request
     * have finished and the LLM has generated its final response. Controllers
     * can use this callback to perform deferred operations, such as rendering
     * UI updates or committing state changes.
     * </p>
     * <p>
     * The default implementation does nothing.
     * </p>
     */
    default void onRequestCompleted() {
        // Default: do nothing
    }
}
