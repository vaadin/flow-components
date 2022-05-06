/*
 * Copyright 2000-2022 Vaadin Ltd.
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
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.server.AbstractStreamResource;
import elemental.json.JsonObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Avatar is a graphical representation of an object or entity, for example a
 * person or an organisation.
 * <p>
 * Avatar has three properties: name, abbreviation and image.
 * <p>
 * The name is shown on hover in a tooltip. When a name is set, Avatar will
 * auto-generate and display an abbreviation of the specified name. For example,
 * “Allison Torres” becomes “AT”, “John Smith” becomes “JS”, etc.
 * <p>
 * The abbreviation can also be set manually. Abbreviations should be kept to a
 * maximum of 2–3 characters.
 * <p>
 * Avatar can be used to display images, such as user profile pictures or
 * company logos. Abbreviations are not shown when images are used.
 * <p>
 * Note that this component is optimized for use with Collaboration Engine — a
 * simple way to build real-time collaboration into your app — but can also be
 * used standalone as a regular component.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-avatar")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/avatar/src/vaadin-avatar.js")
@NpmPackage(value = "@vaadin/avatar", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-avatar", version = "23.1.0-beta1")
public class Avatar extends Component implements HasStyle, HasSize, HasTheme {

    /**
     * The internationalization properties for {@link AvatarGroup}.
     */
    public static class AvatarI18n implements Serializable {
        private String anonymous;

        /**
         * Gets the translated word for {@code anonymous}. It's displayed in a
         * tooltip on hover if the name is not defined.
         *
         * @return the translated word for anonymous. It will be
         *         <code>null</code>, If the translation wasn't set
         */
        public String getAnonymous() {
            return anonymous;
        }

        /**
         * Sets the translated word for {@code anonymous}.
         *
         * @param anonymous
         *            the translated word for anonymous, not <code>null</code>
         * @return this instance for method chaining
         */
        public AvatarI18n setAnonymous(String anonymous) {
            Objects.requireNonNull(anonymous,
                    "The translation should not be null");
            this.anonymous = anonymous;
            return this;
        }
    }

    private AbstractStreamResource imageResource;
    private AvatarI18n i18n;

    /**
     * Creates a new empty avatar.
     * <p>
     * The avatar displays the user icon in the avatar and "Anonymous" in the
     * tooltip unless overridden by setting other properties.
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
     * Gets the internationalization object previously set for this component.
     * <p>
     * Note: updating the object content that is gotten from this method will
     * not update the lang on the component if not set back using
     * {@link Avatar#setI18n(AvatarI18n)}
     *
     * @return the i18n object. It will be <code>null</code>, If the i18n
     *         properties weren't set.
     */
    public AvatarI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not <code>null</code>
     */
    public void setI18n(AvatarI18n i18n) {
        Objects.requireNonNull(i18n,
                "The I18N properties object should not be null");
        this.i18n = i18n;
        JsonObject i18nObject = (JsonObject) JsonSerializer.toJson(i18n);
        getElement().setPropertyJson("i18n", i18nObject);
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
     * The abbreviation will be displayed in the avatar if no image has been
     * set.
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
     *         been set
     */
    public AbstractStreamResource getImageResource() {
        return imageResource;
    }

    /**
     * Sets the image url for the avatar.
     * <p>
     * The image will be displayed in the avatar even if abbreviation or name is
     * set.
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
     * The color index defines which color will be used for the border of the
     * avatar. Color index N applies CSS variable {@code --vaadin-user-color-N}
     * to the border.
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
                .addAll(Stream.of(variants).map(AvatarVariant::getVariantName)
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
