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

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a common single interface for input fields {@link Component components} based on
 * an {@link HasElement element} that supports {@link HasLabel label}, {@link HasStyle styles},
 * {@link HasTooltip tooltip}, {@link HasEnabled enabled status}, {@link HasSize size} and
 * {@link HasValue value} definition.
 *
 * @author Vaadin Ltd
 * @since 24.1
 */
public interface InputField extends HasElement, HasEnabled, HasLabel, HasSize, HasStyle, HasTooltip {

    /**
     * {@link Component#findAncestor}
     */
    public <T> T findAncestor(Class<T> componentType);

    /**
     * {@link Component#getChildren}
     */
    public Stream<Component> getChildren();

    /**
     * {@link Component#getId}
     */
    public Optional<String> getId();

    /**
     * {@link Component#getParent}
     */
    public Optional<Component> getParent();

    /**
     * {@link Component#getTranslation(Locale,Object,Object...)}
     */
    public String getTranslation(Locale locale, Object key, Object... params);

    /**
     * {@link Component#getTranslation(Locale,String,Object...)}
     */
    public String getTranslation(Locale locale, String key, Object... params);

    /**
     * {@link Component#getTranslation(Object, Locale, Object...)}
     */
    @Deprecated
    public String getTranslation(Object key, Locale locale, Object... params);

    /**
     * {@link Component#getTranslation(Object,Object...)}
     */
    public String getTranslation(Object key, Object... params);

    /**
     * {@link Component#getTranslation(String,Locale,Object...)}
     */
    @Deprecated
    public String getTranslation(String key, Locale locale, Object... params);

    /**
     * {@link Component#getTranslation(String,Object...)}
     */
    public String getTranslation(String key, Object... params);

    /**
     * {@link Component#getUI}
     */
    public Optional<UI> getUI();

    /**
     * {@link Component#isAttached}
     */
    public boolean isAttached();

    /**
     * {@link Component#isVisible}
     */
    public boolean isVisible();

    /**
     * {@link Component#onEnabledStateChanged}
     */
    public void onEnabledStateChanged(boolean enabled);

    /**
     * {@link Component#removeFromParent}
     */
    public void removeFromParent();

    /**
     * {@link Component#scrollIntoView}
     */
    public void scrollIntoView();

    /**
     * {@link Component#scrollIntoView(ScrollOptions)}
     */
    public void scrollIntoView(ScrollOptions scrollOptions);

    /**
     * {@link Component#setId}
     */
    public void setId(String id);

    /**
     * {@link Component#setVisible}
     */
    public void setVisible(boolean visible);

}
