/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
