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
 * Interface for AI plugins that extend orchestrator capabilities by providing
 * tools that the LLM can use.
 * <p>
 * Plugins provide domain-specific tools and functionality to the AI
 * orchestrator. Tools are functions that the AI can call to perform actions
 * like querying databases, creating visualizations, filling forms, etc.
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
 * }
 *
 * // Use the plugin
 * String systemPrompt = "You are helpful. " + MyPlugin.getSystemPrompt();
 * AiOrchestrator orchestrator = AiOrchestrator.builder(llmProvider, systemPrompt)
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
}
