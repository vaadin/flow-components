/**
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
package com.vaadin.flow.component.ai.orchestrator;

import com.vaadin.flow.component.ai.provider.LLMProvider;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Interface for AI plugins that extend orchestrator capabilities.
 * <p>
 * Plugins provide domain-specific tools and functionality to the AI
 * orchestrator. Each plugin can:
 * </p>
 * <ul>
 * <li>Contribute tools that the LLM can use</li>
 * <li>Add context to the system prompt</li>
 * <li>Manage its own state for persistence</li>
 * <li>React to lifecycle events</li>
 * </ul>
 * <p>
 * Example plugins: data visualization, form filling, document analysis, code
 * generation.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * // Create a custom plugin
 * public class MyPlugin implements AiPlugin {
 *     &#64;Override
 *     public List&lt;LLMProvider.Tool&gt; getTools() {
 *         return List.of(
 *             new LLMProvider.Tool() {
 *                 &#64;Override
 *                 public String getName() { return "myTool"; }
 *
 *                 &#64;Override
 *                 public String getDescription() { return "Does something useful"; }
 *
 *                 &#64;Override
 *                 public String execute(String arguments) {
 *                     // Tool implementation
 *                     return "Success";
 *                 }
 *             }
 *         );
 *     }
 *
 *     &#64;Override
 *     public String getSystemPromptContribution() {
 *         return "You have access to myTool which does X, Y, Z.";
 *     }
 * }
 *
 * // Use the plugin
 * AiOrchestrator orchestrator = AiOrchestrator.create(llmProvider)
 *     .withPlugin(new MyPlugin())
 *     .build();
 * </pre>
 *
 * @author Vaadin Ltd
 */
public interface AiPlugin extends Serializable {

    /**
     * Returns the tools this plugin provides to the LLM.
     * <p>
     * Tools are functions that the AI can call to perform actions. Each tool
     * should have a clear name, description, and parameter schema.
     * </p>
     *
     * @return list of tools, or empty list if plugin provides no tools
     */
    default List<LLMProvider.Tool> getTools() {
        return Collections.emptyList();
    }

    /**
     * Called when this plugin is attached to an orchestrator.
     * <p>
     * Plugins can use this to initialize resources, register listeners, or
     * access orchestrator features.
     * </p>
     *
     * @param orchestrator
     *            the orchestrator this plugin is attached to
     */
    default void onAttached(AiOrchestrator orchestrator) {
        // Default: no action
    }

    /**
     * Called when this plugin is detached from an orchestrator.
     * <p>
     * Plugins should clean up any resources, unregister listeners, etc.
     * </p>
     */
    default void onDetached() {
        // Default: no action
    }

    /**
     * Captures the current state of this plugin for persistence.
     * <p>
     * The returned object should be serializable and contain all information
     * needed to restore the plugin to its current state.
     * </p>
     *
     * @return plugin state object, or null if plugin has no state
     */
    default Object captureState() {
        return null;
    }

    /**
     * Restores this plugin to a previously captured state.
     * <p>
     * The state object should match what was returned by
     * {@link #captureState()}.
     * </p>
     *
     * @param state
     *            the state to restore
     */
    default void restoreState(Object state) {
        // Default: no action
    }

    /**
     * Returns a unique identifier for this plugin type.
     * <p>
     * Used for state persistence and plugin management. Default implementation
     * uses the class name.
     * </p>
     *
     * @return plugin identifier
     */
    default String getPluginId() {
        return getClass().getSimpleName();
    }
}
