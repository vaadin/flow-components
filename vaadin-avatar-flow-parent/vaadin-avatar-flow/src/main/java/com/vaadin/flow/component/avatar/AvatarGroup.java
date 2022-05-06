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
import com.vaadin.flow.internal.NodeOwner;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Avatar Group is used to group multiple Avatars together. It can be used, for
 * example, to show that there are multiple users viewing the same page or for
 * listing members of a project.
 * <p>
 * You can specify the max number of items an Avatar Group should display. Items
 * that overflow are grouped into a single Avatar that displays the overflow
 * count. The name of each hidden item is shown on hover in a tooltip. Clicking
 * the overflow item displays the overflowing avatars and names in a list.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-avatar-group")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/avatar-group/src/vaadin-avatar-group.js")
@NpmPackage(value = "@vaadin/avatar-group", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-avatar", version = "23.1.0-beta1")
public class AvatarGroup extends Component
        implements HasStyle, HasSize, HasTheme {

    /**
     * Item to be set as an avatar for the avatar group.
     *
     * @author Vaadin Ltd
     */
    public static class AvatarGroupItem implements Serializable {
        private String name;
        private String abbr;
        private String img;
        private Integer colorIndex;

        private AvatarGroup host;
        private StreamRegistration resourceRegistration;
        private Registration pendingRegistration;
        private Command pendingHandle;

        private AbstractStreamResource imageResource;

        /**
         * Creates a new empty avatar group item.
         * <p>
         * The avatar displays the user icon in the avatar and "Anonymous" in
         * the tooltip unless overridden by setting other properties.
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
            if (getHost() != null) {
                getHost().setClientItems();
            }
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
         * The abbreviation will be displayed in the avatar if no image has been
         * set.
         *
         * @param abbr
         *            the abbreviation
         */
        public void setAbbreviation(String abbr) {
            this.abbr = abbr;
            if (getHost() != null) {
                getHost().setClientItems();
            }
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
         * Gets the image that was set for the avatar.
         *
         * @return the image resource value or {@code null} if the resource has
         *         not been set
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
         * with {@link AvatarGroupItem#setImageResource(AbstractStreamResource)}
         *
         * @see AvatarGroupItem#setImageResource(AbstractStreamResource)
         * @param url
         *            the image url
         */
        public void setImage(String url) {
            unsetResource();

            this.img = url;
            if (getHost() != null) {
                getHost().setClientItems();
            }
        }

        /**
         * Sets the image for the avatar.
         * <p>
         * Setting the image as a resource with this method resets the image URL
         * that was set with {@link AvatarGroupItem#setImage(String)}
         *
         * @see AvatarGroupItem#setImage(String)
         * @param resource
         *            the resource value or {@code null} to remove the resource
         */
        public void setImageResource(AbstractStreamResource resource) {
            imageResource = resource;

            if (resource == null) {
                unsetResource();
                return;
            }

            // The following is the copy of functionality from the
            // ElementAttributeMap
            doSetResource(resource);
            if (getHost() != null
                    && getHost().getElement().getNode().isAttached()) {
                registerResource(resource);
            } else {
                deferRegistration(resource);
            }
            if (getHost() != null) {
                getHost().setClientItems();
            }
        }

        private void doSetResource(AbstractStreamResource resource) {
            final URI targetUri;
            if (VaadinSession.getCurrent() != null) {
                final StreamResourceRegistry resourceRegistry = VaadinSession
                        .getCurrent().getResourceRegistry();
                targetUri = resourceRegistry.getTargetURI(resource);
            } else {
                targetUri = StreamResourceRegistry.getURI(resource);
            }
            this.img = targetUri.toASCIIString();
        }

        private void unregisterResource() {
            StreamRegistration registration = resourceRegistration;
            Registration handle = pendingRegistration;
            if (handle != null) {
                handle.remove();
            }
            if (registration != null) {
                registration.unregister();
            }
            this.img = null;
        }

        private void deferRegistration(AbstractStreamResource resource) {
            if (pendingRegistration != null) {
                return;
            }

            pendingHandle = () -> {
                doSetResource(resource);
                registerResource(resource);
            };

            if (getHost() != null) {
                attachPendingRegistration(pendingHandle);
                pendingHandle = null;
            }
        }

        private void attachPendingRegistration(Command pendingHandle) {
            if (getHost().getElement().getNode().isAttached()) {
                pendingHandle.execute();
                return;
            }
            Registration handle = getHost().getElement().getNode()
                    // Do not convert to lambda
                    .addAttachListener(pendingHandle);
            pendingRegistration = handle;
        }

        private void registerResource(AbstractStreamResource resource) {
            assert resourceRegistration == null;
            StreamRegistration registration = getSession().getResourceRegistry()
                    .registerResource(resource);
            resourceRegistration = registration;
            Registration handle = pendingRegistration;
            if (handle != null) {
                handle.remove();
            }
            pendingRegistration = getHost().getElement().getNode()
                    .addDetachListener(
                            // Do not convert to lambda
                            new Command() {
                                @Override
                                public void execute() {
                                    AvatarGroupItem.this.unsetResource();
                                }
                            });
        }

        private void unsetResource() {
            imageResource = null;
            StreamRegistration registration = resourceRegistration;
            Optional<AbstractStreamResource> resource = Optional.empty();
            if (registration != null) {
                resource = Optional.ofNullable(registration.getResource());
            }
            unregisterResource();
            resource.ifPresent(this::deferRegistration);
        }

        private VaadinSession getSession() {
            NodeOwner owner = getHost().getElement().getNode().getOwner();
            assert owner instanceof StateTree;
            return ((StateTree) owner).getUI().getSession();
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
         * The color index defines which color will be used for the border of
         * the avatar. Color index N applies CSS variable
         * {@code --vaadin-user-color-N} to the border.
         *
         * @param colorIndex
         *            the color index or {@code null} to remove the index
         */
        public void setColorIndex(Integer colorIndex) {
            this.colorIndex = colorIndex;
            if (getHost() != null) {
                getHost().setClientItems();
            }
        }

        private AvatarGroup getHost() {
            return host;
        }

        private void setHost(AvatarGroup host) {
            this.host = host;
            if (pendingHandle != null) {
                attachPendingRegistration(pendingHandle);
                pendingHandle = null;
            }
        }
    }

    /**
     * The internationalization properties for {@link AvatarGroup}.
     */
    public static class AvatarGroupI18n implements Serializable {
        private String anonymous;
        private HashMap<String, String> activeUsers = new HashMap();

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
        public AvatarGroupI18n setAnonymous(String anonymous) {
            Objects.requireNonNull(anonymous,
                    "The translation should not be null");
            this.anonymous = anonymous;
            return this;
        }

        /**
         * Gets the translated phrase for avatar group accessible label when
         * having one active user.
         *
         * @return the translated word for the label. It will be
         *         <code>null</code>, If the translation wasn't set
         */
        public String getOneActiveUser() {
            return activeUsers.get("one");
        }

        /**
         * Sets the translated phrase for avatar group accessible label when
         * having one active user.
         *
         * @param oneActiveUser
         *            the translated word for the label, not <code>null</code>
         * @return this instance for method chaining
         */
        public AvatarGroupI18n setOneActiveUser(String oneActiveUser) {
            Objects.requireNonNull(oneActiveUser,
                    "The translation should not be null");
            activeUsers.put("one", oneActiveUser);
            return this;
        }

        /**
         * Gets the translated phrase for avatar group accessible label when
         * having many active users.
         *
         * @return the translated word for the label. It will be
         *         <code>null</code>, If the translation wasn't set
         */
        public String getManyActiveUsers() {
            return activeUsers.get("many");
        }

        /**
         * Sets the translated phrase for avatar group accessible label when
         * having many active users.
         * <p>
         * You can use word <code>{count}</code> in order to display current
         * count of active users. For example, "Currently {count} active users".
         *
         * @param manyActiveUsers
         *            the translated word for the label, not <code>null</code>
         * @return this instance for method chaining
         */
        public AvatarGroupI18n setManyActiveUsers(String manyActiveUsers) {
            Objects.requireNonNull(manyActiveUsers,
                    "The translation should not be null");
            activeUsers.put("many", manyActiveUsers);
            return this;
        }
    }

    private List<AvatarGroupItem> items = Collections.emptyList();
    private boolean pendingUpdate = false;

    private AvatarGroupI18n i18n;

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
        this.items.forEach(item -> item.setHost(null));

        this.items = new ArrayList<>(items);
        items.stream().forEach(item -> item.setHost(this));
        setClientItems();
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

    private void setClientItems() {
        if (!pendingUpdate) {
            pendingUpdate = true;
            getElement().getNode().runWhenAttached(
                    ui -> ui.beforeClientResponse(this, ctx -> {
                        getElement().setPropertyJson("items",
                                createItemsJsonArray(items));
                        pendingUpdate = false;
                    }));
        }
    }

    private JsonArray createItemsJsonArray(Collection<AvatarGroupItem> items) {
        JsonArray jsonItems = Json.createArray();
        for (AvatarGroupItem item : items) {
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
     * Adds the items to the list of displayed as avatars.
     *
     * @param items
     *            the items to add
     */
    public void add(AvatarGroupItem... items) {
        setItems(Stream.concat(this.items.stream(), Arrays.stream(items))
                .collect(Collectors.toList()));
    }

    /**
     * Removes the items from the list of displayed as avatars.
     *
     * @param items
     *            the items to remove
     */
    public void remove(AvatarGroupItem... items) {
        List<AvatarGroupItem> itemsToRemove = Arrays.asList(items);

        setItems(this.items.stream()
                .filter(item -> !itemsToRemove.contains(item))
                .collect(Collectors.toList()));
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
     * Gets the internationalization object previously set for this component.
     * <p>
     * Note: updating the object content that is gotten from this method will
     * not update the lang on the component if not set back using
     * {@link AvatarGroup#setI18n(AvatarGroupI18n)}
     *
     * @return the i18n object. It will be <code>null</code>, If the i18n
     *         properties weren't set.
     */
    public AvatarGroupI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not <code>null</code>
     */
    public void setI18n(AvatarGroupI18n i18n) {
        Objects.requireNonNull(i18n,
                "The I18N properties object should not be null");
        this.i18n = i18n;
        JsonObject i18nObject = (JsonObject) JsonSerializer.toJson(i18n);
        i18nObject.remove("manyActiveUsers");
        i18nObject.remove("oneActiveUser");

        JsonObject activeUsers = Json.createObject();
        activeUsers.put("many", i18n.getManyActiveUsers());
        activeUsers.put("one", i18n.getOneActiveUser());

        i18nObject.put("activeUsers", activeUsers);
        getElement().setPropertyJson("i18n", i18nObject);
    }

    /**
     * Sets the the maximum number of avatars to display.
     * <p>
     * By default, all the avatars are displayed. When max is set, the
     * overflowing avatars are grouped into one avatar.
     *
     * @param max
     *            the maximum number of avatars, or {@code null} to remove the
     *            limit
     */
    public void setMaxItemsVisible(Integer max) {
        getElement().setProperty("maxItemsVisible", max);
    }

    /**
     * Gets the maximum number of avatars to display, or {@code null} if no
     * limit has been set.
     *
     * @return the max number of avatars
     * @see AvatarGroup#setMaxItemsVisible(Integer)
     */
    public Integer getMaxItemsVisible() {
        String max = getElement().getProperty("maxItemsVisible");
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
        getThemeNames().addAll(
                Stream.of(variants).map(AvatarGroupVariant::getVariantName)
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
