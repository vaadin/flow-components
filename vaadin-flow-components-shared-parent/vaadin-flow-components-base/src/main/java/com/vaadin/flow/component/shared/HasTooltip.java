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
package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.signals.Signal;

/**
 * Mixin interface for components that have special handling for tooltips on the
 * Web Component level.
 * <p>
 * Components that implement this interface get a
 * <code>&lt;vaadin-tooltip slot="tooltip"&gt;</code> element added inside the
 * component's light DOM and are expected to handle it appropriately on the
 * client-side.
 * <p>
 * Use this interface only if you are implementing a new component that also has
 * a Web Component counterpart with a custom tooltip support. Otherwise, use
 * {@link Tooltip#forComponent(Component)} instead.
 *
 *
 * @author Vaadin Ltd
 */
public interface HasTooltip extends HasElement {

    /**
     * Sets a tooltip text for the component as plain text.
     *
     * @param text
     *            The tooltip text
     *
     * @return the tooltip handle
     */
    default Tooltip setTooltipText(String text) {
        var tooltip = Tooltip.getForElement(getElement());
        if (tooltip == null) {
            tooltip = Tooltip.forHasTooltip(this);
        }
        tooltip.setText(text);
        return tooltip;
    }

    /**
     * Sets a tooltip text for the component in Markdown format.
     *
     * @param markdown
     *            The tooltip text in Markdown format
     *
     * @return the tooltip handle
     */
    default Tooltip setTooltipMarkdown(String markdown) {
        var tooltip = Tooltip.getForElement(getElement());
        if (tooltip == null) {
            tooltip = Tooltip.forHasTooltip(this);
        }
        tooltip.setMarkdown(markdown);
        return tooltip;
    }

    /**
     * Gets the tooltip handle of the component.
     *
     * @return the tooltip handle
     */
    default Tooltip getTooltip() {
        var tooltip = Tooltip.getForElement(getElement());
        if (tooltip == null) {
            tooltip = setTooltipText(null);
        }
        return tooltip;
    }

    /**
     * Binds the tooltip text to the given signal.
     * <p>
     * When a signal is bound, the tooltip text value is kept synchronized with
     * the signal value while the component is attached. When the component is
     * detached, signal value changes have no effect.
     * <p>
     * Passing {@code null} as the signal unbinds the existing binding.
     * <p>
     * While a signal is bound, any attempt to set the text value manually
     * (other than through the signal) throws {@code BindingActiveException}.
     * Attempting to bind a new signal while one is already bound also throws
     * {@code BindingActiveException}.
     *
     * @param textSignal
     *            the signal to bind to, or {@code null} to unbind
     * @see #setTooltipText(String)
     * @since 25.1
     */
    default void bindTooltipText(Signal<String> textSignal) {
        getTooltip().bindText(textSignal);
    }

    /**
     * Binds the tooltip markdown content to the given signal.
     * <p>
     * When a signal is bound, the tooltip markdown content value is kept
     * synchronized with the signal value while the component is attached. When
     * the component is detached, signal value changes have no effect.
     * <p>
     * Passing {@code null} as the signal unbinds the existing binding.
     * <p>
     * While a signal is bound, any attempt to set the markdown value manually
     * (other than through the signal) throws {@code BindingActiveException}.
     * Attempting to bind a new signal while one is already bound also throws
     * {@code BindingActiveException}.
     *
     * @param markdownSignal
     *            the signal to bind to, or {@code null} to unbind
     * @see #setTooltipMarkdown(String)
     * @since 25.1
     */
    default void bindTooltipMarkdown(Signal<String> markdownSignal) {
        getTooltip().bindMarkdown(markdownSignal);
    }

}
