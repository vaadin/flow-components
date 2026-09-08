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
package com.vaadin.flow.component.messages;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.AbstractDownloadHandler;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.shared.Registration;

/**
 * A user of a {@link MessageList}, rendered as an avatar with a name in the
 * typing indicator of the list.
 * <p>
 * A user can belong to one message list at a time. Adding the same instance to
 * another list removes it from the previous one.
 *
 * @author Vaadin Ltd.
 * @since 25.3
 */
// Explicitly whitelist the properties to send to the client
@JsonIncludeProperties({ "name", "abbr", "img", "colorIndex", "className" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageListUser implements Serializable {

    private MessageList host;

    private String name;
    private String abbreviation;
    private String image;
    private Integer colorIndex;
    private Set<String> classNames = new LinkedHashSet<>();

    private DownloadHandler imageHandler;
    private StreamRegistration imageRegistration;
    private Registration hostAttachRegistration;
    private Registration hostDetachRegistration;

    /**
     * Creates an empty user. Use the setter methods to configure what is
     * displayed in the avatar.
     */
    public MessageListUser() {
    }

    /**
     * Creates a user with the given name.
     *
     * @param name
     *            the name of the user
     * @see #setName(String)
     */
    public MessageListUser(String name) {
        this.name = name;
    }

    /**
     * Creates a user with the given name and image URL.
     *
     * @param name
     *            the name of the user
     * @param imageUrl
     *            the URL of the avatar image
     * @see #setName(String)
     * @see #setImage(String)
     */
    public MessageListUser(String name, String imageUrl) {
        this.name = name;
        this.image = imageUrl;
    }

    /**
     * Gets the name of the user.
     *
     * @return the name of the user, or {@code null} if no name has been set
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the user. The name is displayed next to the avatar, and
     * identifies the user within the message list.
     *
     * @param name
     *            the name of the user, or {@code null} to remove the name
     */
    public void setName(String name) {
        this.name = name;
        notifyHost();
    }

    /**
     * Gets the abbreviation of the user.
     *
     * @return the abbreviation, or {@code null} if no abbreviation has been set
     */
    @JsonProperty("abbr")
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Sets the abbreviation of the user. The abbreviation is displayed in the
     * avatar when no image has been set. By default, the abbreviation is
     * deduced from the name.
     *
     * @param abbreviation
     *            the abbreviation, or {@code null} to remove the abbreviation
     */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
        notifyHost();
    }

    /**
     * Gets the URL of the avatar image.
     * <p>
     * When the image is set with {@link #setImageHandler(DownloadHandler)},
     * this returns the URL that the handler is served from, or {@code null}
     * while the user does not belong to an attached message list.
     *
     * @return the image URL, or {@code null} if no image has been set
     */
    @JsonProperty("img")
    public String getImage() {
        return image;
    }

    /**
     * Sets the URL of the avatar image. The image is displayed in the avatar
     * even when a name or an abbreviation has been set.
     * <p>
     * This removes an image set with {@link #setImageHandler(DownloadHandler)}.
     *
     * @param imageUrl
     *            the image URL, or {@code null} to remove the image
     */
    public void setImage(String imageUrl) {
        releaseImageHandler();
        this.image = imageUrl;
        notifyHost();
    }

    /**
     * Gets the download handler that serves the avatar image.
     *
     * @return the download handler, or {@code null} if the image has not been
     *         set with {@link #setImageHandler(DownloadHandler)}
     */
    @JsonIgnore
    public DownloadHandler getImageHandler() {
        return imageHandler;
    }

    /**
     * Sets a download handler that serves the avatar image. The handler is
     * registered as a resource of the message list that the user belongs to,
     * and is unregistered when the user is removed from the list or the list is
     * detached.
     * <p>
     * The {@code Content-Disposition} header is set to {@code inline} for the
     * handlers created by the factory methods in {@link DownloadHandler}, as
     * well as for other {@link AbstractDownloadHandler} implementations.
     * <p>
     * This removes an image URL set with {@link #setImage(String)}.
     *
     * @param downloadHandler
     *            the download handler, or {@code null} to remove the image
     */
    public void setImageHandler(DownloadHandler downloadHandler) {
        releaseImageHandler();
        this.image = null;

        if (downloadHandler instanceof AbstractDownloadHandler<?> handler) {
            // Change the disposition to inline in the pre-defined handlers,
            // where it is 'attachment' by default
            handler.inline();
        }
        this.imageHandler = downloadHandler;
        bindImageHandler();
        notifyHost();
    }

    /**
     * Gets the color index of the user.
     *
     * @return the color index, or {@code null} if no color index has been set
     */
    public Integer getColorIndex() {
        return colorIndex;
    }

    /**
     * Sets the color index of the user. Color index N applies the CSS variable
     * {@code --vaadin-user-color-N} to the border of the avatar.
     *
     * @param colorIndex
     *            the color index, or {@code null} to remove the color index
     */
    public void setColorIndex(Integer colorIndex) {
        this.colorIndex = colorIndex;
        notifyHost();
    }

    /**
     * Adds one or more class names to the avatar of this user.
     *
     * @param classNames
     *            the class names to add
     */
    public void addClassNames(String... classNames) {
        this.classNames.addAll(Arrays.asList(classNames));
        notifyHost();
    }

    /**
     * Removes one or more class names from the avatar of this user.
     *
     * @param classNames
     *            the class names to remove
     */
    public void removeClassNames(String... classNames) {
        this.classNames.removeAll(Arrays.asList(classNames));
        notifyHost();
    }

    /**
     * Gets the class names set on the avatar of this user.
     *
     * @return a space-delimited list of class names, or {@code null} if no
     *         class names have been set
     */
    public String getClassName() {
        if (classNames.isEmpty()) {
            return null;
        }
        return classNames.stream().collect(Collectors.joining(" "));
    }

    /**
     * Sets the message list that this user belongs to. Rebinds an image
     * download handler to the new host, so that the handler is served as a
     * resource of that message list.
     *
     * @param host
     *            the message list, or {@code null} to detach the user from its
     *            current message list
     */
    void setHost(MessageList host) {
        if (this.host == host) {
            return;
        }
        unbindImageHandler();
        this.host = host;
        bindImageHandler();
    }

    MessageList getHost() {
        return host;
    }

    private void notifyHost() {
        if (host != null) {
            host.typingUserChanged(this);
        }
    }

    /**
     * Registers the image download handler as a resource of the host message
     * list, and keeps the registration in sync with the attach state of the
     * host: the resource is unregistered when the host is detached, and
     * registered again when it is attached.
     */
    private void bindImageHandler() {
        if (host == null || imageHandler == null) {
            return;
        }
        var node = host.getElement().getNode();
        // Do not convert the listeners to lambdas, so that they stay
        // serializable with the session
        hostAttachRegistration = node.addAttachListener(new Command() {
            @Override
            public void execute() {
                MessageListUser.this.registerImage();
            }
        });
        hostDetachRegistration = node.addDetachListener(new Command() {
            @Override
            public void execute() {
                MessageListUser.this.unregisterImage();
            }
        });
        if (node.isAttached()) {
            registerImage();
        }
    }

    private void unbindImageHandler() {
        unregisterImage();
        if (hostAttachRegistration != null) {
            hostAttachRegistration.remove();
            hostAttachRegistration = null;
        }
        if (hostDetachRegistration != null) {
            hostDetachRegistration.remove();
            hostDetachRegistration = null;
        }
    }

    private void releaseImageHandler() {
        unbindImageHandler();
        imageHandler = null;
    }

    private void registerImage() {
        if (imageRegistration != null) {
            return;
        }
        var resource = new StreamResourceRegistry.ElementStreamResource(
                imageHandler, host.getElement());
        imageRegistration = getHostSession().getResourceRegistry()
                .registerResource(resource);
        image = imageRegistration.getResourceUri().toASCIIString();
        notifyHost();
    }

    private void unregisterImage() {
        if (imageRegistration != null) {
            imageRegistration.unregister();
            imageRegistration = null;
        }
        if (imageHandler != null) {
            image = null;
        }
    }

    private VaadinSession getHostSession() {
        var owner = host.getElement().getNode().getOwner();
        assert owner instanceof StateTree;
        return ((StateTree) owner).getUI().getSession();
    }
}
