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

import com.vaadin.flow.js.JsFunctionParameterNames;
import com.vaadin.flow.js.JsSnippet;

/**
 * A Lit template that renders an item, declared by a method of a
 * {@link com.vaadin.flow.js.JsDefinition} interface. The build collects the
 * JavaScript into the bundle, so the browser does not compile the template from
 * a string, which a content security policy without <code>unsafe-eval</code>
 * forbids.
 * <p>
 * The JavaScript is the body of a function that returns the template:
 *
 * <pre>
 * &#64;JsDefinition
 * interface PersonTemplates extends Serializable {
 *
 *     &#64;JsExpression("""
 *             return html`&lt;span title=${$0} &#64;click=${functions.select}&gt;
 *                 ${item.name}&lt;/span&gt;`;
 *             """)
 *     CompiledLitTemplate name(String title);
 * }
 *
 * grid.addColumn(LitRenderer
 *         .&lt;Person&gt; of(JsSnippets.of(PersonTemplates.class).name("Name"))
 *         .withProperty("name", Person::getName)
 *         .withFunction("select", person -&gt; select(person)));
 * </pre>
 *
 * The function is called with these arguments:
 * <ul>
 * <li><code>html</code>: the tag of a Lit template. The markup is written
 * between backticks after it, as <code>html`...`</code>, and each
 * <code>${...}</code> in it is a binding to a JavaScript value: text content
 * (<code>${item.name}</code>), an attribute (<code>title=${...}</code>), a
 * property of the element (<code>.disabled=${...}</code>) or an event listener
 * (<code>&#64;click=${...}</code>). Lit creates the elements of the markup once
 * and afterwards only updates the bindings whose values changed.</li>
 * <li><code>live</code>: a Lit directive for a property that the user can also
 * change in the browser, such as <code>checked</code> of a checkbox. A binding
 * written as <code>.checked=${live(item.checked)}</code> compares the value
 * with the current value of the element, rather than with the last rendered
 * value, so that the value of the server is set again after the user has
 * changed it.</li>
 * <li><code>root</code>: the element the item is rendered into</li>
 * <li><code>appId</code>: the id of the application</li>
 * <li><code>itemKey</code>, <code>model</code>, <code>index</code>: the key of
 * the item, the model of the row of the component, for example its
 * <code>expanded</code> state in a tree grid, and the index of the item</li>
 * <li><code>item</code>: an object with the properties added with
 * {@link LitRenderer#withProperty}, rather than the Java object of the
 * item</li>
 * <li><code>functions</code>: the functions added with
 * {@link LitRenderer#withFunction}, as members, which call the server</li>
 * </ul>
 * The arguments of the declaring method are available as <code>$0</code>,
 * <code>$1</code>, &hellip; They are sent to the browser when the renderer is
 * set up, so they are the same for every item.
 *
 * @see LitRenderer#of(CompiledLitTemplate)
 * @see <a href="https://lit.dev/docs/templates/overview/">Lit templates</a>
 * @see <a href="https://lit.dev/docs/templates/directives/#live">The live
 *      directive</a>
 * @since 25.4
 */
@JsFunctionParameterNames({ "html", "root", "live", "appId", "itemKey", "model",
        "item", "index", "functions" })
public interface CompiledLitTemplate extends JsSnippet {
}
