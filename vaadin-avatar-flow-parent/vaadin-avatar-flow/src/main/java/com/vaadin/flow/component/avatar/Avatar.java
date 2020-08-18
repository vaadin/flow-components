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
import com.vaadin.flow.server.AbstractStreamResource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Server-side component for the <code>vaadin-avatar</code> element.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-avatar")
@JsModule("@vaadin/vaadin-avatar/src/vaadin-avatar.js")
@NpmPackage(value = "@vaadin/vaadin-avatar", version = "1.0.0-alpha6")
public class Avatar extends Component
    implements HasStyle, HasSize, HasTheme {

    private AbstractStreamResource imageResource;

    /**
     * Creates a new empty avatar.
     * <p>
     * The avatar displays the user icon in the avatar and "Anonymous"
     * in the tooltip unless overridden by setting other properties.
     */
    public Avatar() {
    }

    /**
     * Creates a new avatar with the provided name.
     *
     * @param name
     *            the name for the avatar
     * @see Avatar#setName(String)
     */
    public Avatar(String name) {
        setName(name);
    }

    /**
     * Creates a new avatar with the provided name and url.
     *
     * @param name
     *            the name for the avatar
     * @param url
     *            the image url
     * @see Avatar#setName(String)
     * @see Avatar#setImage(String)
     */
    public Avatar(String name, String url) {
        setName(name);
        setImage(url);
    }

    /**
     * Gets the name that was set for the avatar.
     *
     * @return the name
     */
    public String getName() {
        return getElement().getProperty("name");
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
        getElement().setProperty("name", name);
    }

    /**
     * Gets the abbreviation that was set for the avatar.
     *
     * @return the abbreviation
     */
    public String getAbbreviation() {
        return getElement().getProperty("abbr");
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
        getElement().setProperty("abbr", abbr);
    }

    /**
     * Gets the image url that was set for the avatar.
     *
     * @return the image url
     */
    public String getImage() {
        return getElement().getAttribute("img");
    }

    /**
     * Gets the image that was set for the avatar.
     *
     * @return the image resource value or {@code null} if the resource has not
     * been set
     */
    public AbstractStreamResource getImageResource() {
        return imageResource;
    }

    /**
     * Sets the image url for the avatar.
     * <p>
     * The image will be displayed in the avatar even if abbreviation or
     * name is set.
     * <p>
     * Setting the image with this method resets the image resource provided
     * with {@link Avatar#setImageResource(AbstractStreamResource)}
     *
     * @see Avatar#setImageResource(AbstractStreamResource)
     * @param url
     *            the image url
     */
    public void setImage(String url) {
        imageResource = null;

        if (url == null) {
            getElement().removeAttribute("img");
        } else {
            getElement().setAttribute("img", url);
        }
    }

    /**
     * Sets the image for the avatar.
     * <p>
     * Setting the image as a resource with this method resets the image URL
     * that was set with {@link Avatar#setImage(String)}
     *
     * @see Avatar#setImage(String)
     * @param resource
     *            the resource value or {@code null} to remove the resource
     */
    public void setImageResource(AbstractStreamResource resource) {
        imageResource = resource;
        if (resource == null) {
            getElement().removeAttribute("img");
            return;
        }

        getElement().setAttribute("img", resource);
    }

    /**
     * Gets the color index for the avatar.
     *
     * @return the color index or {@code null} if the index has not been set
     */
    public Integer getColorIndex() {
        String colorIndex = getElement().getProperty("colorIndex");
        if (colorIndex != null && !colorIndex.isEmpty()) {
            return Integer.parseInt(colorIndex);
        }

        return null;
    }

    /**
     * Sets the color index for the avatar.
     * <p>
     * The color index defines which color will be used for the border
     * of the avatar. Color index N applies CSS variable
     * {@code --vaadin-user-color-N} to the border.
     *
     * @param colorIndex
     *            the color index or {@code null} to remove the index
     */
    public void setColorIndex(Integer colorIndex) {
        getElement().setProperty("colorIndex", colorIndex);
    }

    /**
     * Adds theme variants to the avatar component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(AvatarVariant... variants) {
        getThemeNames()
                .addAll(Stream.of(variants)
                        .map(AvatarVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the avatar component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(AvatarVariant... variants) {
        getThemeNames().removeAll(
                Stream.of(variants).map(AvatarVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

}
