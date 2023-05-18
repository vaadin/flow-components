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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.i18n.I18NProvider;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a common single interface for input fields {@link Component
 * components} based on an {@link HasElement element} that supports
 * {@link HasLabel label}, {@link HasStyle styles}, {@link HasTooltip tooltip},
 * {@link HasEnabled enabled status}, {@link HasSize size} and {@link HasValue value}
 * definition.
 *
 * @author Vaadin Ltd
 * @since 24.1
 */
public interface InputField<E extends HasValue.ValueChangeEvent<V>, V> extends
        HasEnabled, HasLabel, HasSize, HasStyle, HasTooltip, HasValueAndElement<E, V> {

}
