/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.shared.Registration;

/**
 * Mixin interface for subscribing to the client-side `validated` event from a
 * component.
 */
public interface HasClientValidation extends Serializable {
    default Registration addUnparseableChangeListener(
            ComponentEventListener<UnparseableChangeEvent> listener) {
        return ComponentUtil.addListener((Component) this,
                UnparseableChangeEvent.class, listener);
    }

    default Registration addIncompleteChangeListener(
            ComponentEventListener<IncompleteChangeEvent> listener) {
        return ComponentUtil.addListener((Component) this,
                IncompleteChangeEvent.class, listener);
    }

    default Registration addHasInputValueChangedListener(
            ComponentEventListener<HasInputValueChangedEvent> listener) {
        return ComponentUtil.addListener((Component) this,
                HasInputValueChangedEvent.class, listener);
    }

    @DomEvent("unparseable-change")
    public static class UnparseableChangeEvent extends ComponentEvent<Component> {
        public UnparseableChangeEvent(Component source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("incomplete-change")
    public static class IncompleteChangeEvent extends ComponentEvent<Component> {
        public IncompleteChangeEvent(Component source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("has-input-value-changed")
    public static class HasInputValueChangedEvent extends ComponentEvent<Component> {
        public HasInputValueChangedEvent(Component source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
