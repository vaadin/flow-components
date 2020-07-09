/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.component.avatar;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Server-side component for the <code>vaadin-avatar-group</code> element.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-avatar-group")
@JsModule("@vaadin/vaadin-avatar/src/vaadin-avatar-group.js")
@NpmPackage(value = "@vaadin/vaadin-avatar", version = "1.0.0-alpha2")
public class AvatarGroup extends Component
    implements HasStyle, HasSize, HasTheme {

    /**
     * Item to be set as an avatar for the avatar group.
     *
     * @author Vaadin Ltd
     */
    public static class AvatarGroupItem implements Serializable  {
        private String name;
        private String abbr;
        private String img;
        private Integer colorIndex;

        /**
         * Creates a new empty avatar group item.
         * <p>
         * The avatar displays the user icon in the avatar and "Anonymous"
         * in the tooltip unless overridden by setting other properties.
         */
        public AvatarGroupItem() {
        }

        /**
         * Creates a new avatar group item with the provided name.
         *
         * @param name
         *            the name for the avatar
         * @see AvatarGroupItem#setName(String)
         */
        public AvatarGroupItem(String name) {
            setName(name);
        }

        /**
         * Creates a new avatar group item with the provided name and url.
         *
         * @param name
         *            the name for the avatar
         * @param url
         *            the image url
         * @see AvatarGroupItem#setName(String)
         * @see AvatarGroupItem#setImage(String)
         */
        public AvatarGroupItem(String name, String url) {
            setName(name);
            setImage(url);
        }

        /**
         * Gets the name that was set for the avatar.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name for the avatar.
         * <p>
         * The name is displayed in a tooltip on hover.
         * <p>
         * Automatically deduced abbreviation is displayed in the avatar if no
         * abbreviation or image is set.
         *
         * @param name
         *            the name for the avatar
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the abbreviation that was set for the avatar.
         *
         * @return the abbreviation
         */
        public String getAbbreviation() {
            return abbr;
        }

        /**
         * Sets the abbreviation for the avatar.
         * <p>
         * The abbreviation will be displayed in the avatar if no image has
         * been set.
         *
         * @param abbr
         *            the abbreviation
         */
        public void setAbbreviation(String abbr) {
            this.abbr = abbr;
        }

        /**
         * Gets the image url that was set for the avatar.
         *
         * @return the image url
         */
        public String getImage() {
            return img;
        }

        /**
         * Sets the image url for the avatar.
         * <p>
         * The image will be displayed in the avatar even if abbreviation or
         * name is set.
         *
         * @param url
         *            the image url
         */
        public void setImage(String url) {
            this.img = url;
        }

        /**
         * Gets the color index for the avatar group item.
         *
         * @return the color index or {@code null} if the index has not been set
         */
        public Integer getColorIndex() {
            return colorIndex;
        }

        /**
         * Sets the color index for the avatar group item.
         * <p>
         * The color index defines which color will be used for the border
         * of the avatar. Color index N applies CSS variable
         * {@code --vaadin-user-color-N} to the border.
         *
         * @param colorIndex
         *            the color index or {@code null} to remove the index
         */
        public void setColorIndex(Integer colorIndex) {
            this.colorIndex = colorIndex;
        }
    }

    private List<AvatarGroupItem> items;

    /**
     * Creates an empty avatar group component.
     */
    public AvatarGroup() {
    }

    /**
     * Creates an avatar group with the provided items to be displayed as
     * avatars.
     */
    public AvatarGroup(Collection<AvatarGroupItem> items) {
        setItems(items);
    }

    /**
     * Creates an avatar group with the provided items to be displayed as
     * avatars.
     */
    public AvatarGroup(AvatarGroupItem... items) {
        setItems(items);
    }

    /**
     * Sets the items that will be displayed as avatars.
     *
     * @param items
     *            the items to set
     */
    public void setItems(Collection<AvatarGroupItem> items) {
        this.items = new ArrayList<>(items);

        getElement().setPropertyJson("items", createItemsJsonArray(items));
    }

    /**
     * Sets the items that will be displayed as avatars.
     *
     * @param items
     *            the items to set
     */
    public void setItems(AvatarGroupItem... items) {
        setItems(Arrays.asList(items));
    }

    private JsonArray createItemsJsonArray(Collection<AvatarGroupItem> items) {
        JsonArray jsonItems = Json.createArray();
        for (AvatarGroupItem item: items) {
            JsonObject jsonItem = Json.createObject();
            if (item.getName() != null) {
                jsonItem.put("name", item.getName());
            }

            if (item.getAbbreviation() != null) {
                jsonItem.put("abbr", item.getAbbreviation());
            }

            if (item.getImage() != null) {
                jsonItem.put("img", item.getImage());
            }

            if (item.getColorIndex() != null) {
                jsonItem.put("colorIndex", item.getColorIndex());
            }

            jsonItems.set(jsonItems.length(), jsonItem);
        }

        return jsonItems;
    }

    /**
     * Gets the items that were set for the avatar group in an unmodifiable
     * list.
     *
     * @return list of items
     */
    public List<AvatarGroupItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Sets the the maximum number of avatars to display.
     * <p>
     * By default, all the avatars are displayed. When max is set, the
     * overflowing avatars are grouped into one avatar.
     *
     * @param max
     *            the max number of avatars, or {@code null} to remove the max
     */
    public void setMax(Integer max) {
        getElement().setProperty("max", max);
    }

    /**
     * Gets the maximum number of avatars to display, or {@code null} if no max
     * has been set.
     *
     * @return the max number of avatars
     * @see AvatarGroup#setMax(Integer)
     */
    public Integer getMax() {
        String max = getElement().getProperty("max");
        if (max != null && !max.isEmpty()) {
            return Integer.parseInt(max);
        }

        return null;
    }

    /**
     * Adds theme variants to the avatar group component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(AvatarGroupVariant... variants) {
        getThemeNames()
                .addAll(Stream.of(variants)
                        .map(AvatarGroupVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the avatar group component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(AvatarGroupVariant... variants) {
        getThemeNames().removeAll(
                Stream.of(variants).map(AvatarGroupVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

}
