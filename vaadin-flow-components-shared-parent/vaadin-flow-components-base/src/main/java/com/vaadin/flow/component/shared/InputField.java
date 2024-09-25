/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;

/**
 * A common interface for input fields that can be used to iterate over a
 * collection of fields and set common properties.
 * <p>
 * The following interfaces are included:
 * <ul>
 * <li>{@link HasEnabled}</li>
 * <li>{@link HasHelper}</li>
 * <li>{@link HasLabel}</li>
 * <li>{@link HasStyle}</li>
 * <li>{@link HasTooltip}</li>
 * <li>{@link HasSize}</li>
 * <li>{@link HasValue}</li>
 * </ul>
 *
 * @param <E>
 *            the type of the value change event fired by this instance
 * @param <V>
 *            the value type
 *
 * @author Vaadin Ltd
 * @since 24.1
 */
public interface InputField<E extends HasValue.ValueChangeEvent<V>, V>
        extends HasEnabled, HasHelper, HasLabel, HasSize, HasStyle, HasTooltip,
        HasValue<E, V> {

}
