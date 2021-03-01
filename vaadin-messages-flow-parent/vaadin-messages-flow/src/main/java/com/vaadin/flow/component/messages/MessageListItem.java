/*
 * Copyright 2000-2021 Vaadin Ltd.
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
import java.time.Instant;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Item to render as a message component inside a {@link MessageList}.
 *
 * @author Vaadin Ltd.
 * @see MessageList#setItems(Collection)
 */
public class MessageListItem implements Serializable {

    private MessageList host;

    private String text;
    private Instant time;

    private String userName;
    private String userAbbreviation;
    private String userImage;
    private Integer userColorIndex;

    /**
     * Creates an empty message list item. Use the setter methods to configure
     * what will be displayed in the message.
     */
    public MessageListItem() {
    }

    /**
     * Creates a message list item with the provided text, which will be
     * displayed as plain text in the message body.
     *
     * @param text
     *            the text content of the message
     * @see #setText(String)
     */
    public MessageListItem(String text) {
        this.text = text;
    }

    /**
     * Creates a message list item with the provided text content, time and user
     * name.
     * <p>
     * The text will be rendered as plain text in the message body. The time and
     * user name will also be displayed in the message component. The user name
     * is also used in the message's avatar.
     *
     * @param text
     *            the text content of the message
     * @param time
     *            the time of sending the message
     * @param userName
     *            the user name of the message sender
     * @see #setText(String)
     * @see #setTime(Instant)
     * @see #setUserName(String)
     */
    public MessageListItem(String text, Instant time, String userName) {
        this(text);
        this.time = time;
        this.userName = userName;
    }

    /**
     * Creates a message list item with the provided text content, time and user
     * name.
     * <p>
     * The text will be rendered as plain text in the message body. The time and
     * user name will also be displayed in the message component. The user image
     * will be displayed in an avatar in the message.
     *
     * @param text
     *            the text content of the message
     * @param time
     *            the time of sending the message
     * @param userName
     *            the user name of the message sender
     * @param userImage
     *            the URL of the message sender's image
     * @see #setText(String)
     * @see #setTime(Instant)
     * @see #setUserName(String)
     * @see #setUserImage(String)
     */
    public MessageListItem(String text, Instant time, String userName,
            String userImage) {
        this(text, time, userName);
        this.userImage = userImage;
    }

    /**
     * Gets the text content of the message.
     *
     * @return the message's text content, or {@code null} if none is set
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of the message. It will be rendered as plain text
     * in the message body.
     *
     * @param text
     *            the message's text content to set, or {@code null} to remove
     *            the content
     */
    public void setText(String text) {
        this.text = text;
        propsChanged();
    }

    /**
     * Gets the time of sending the message.
     *
     * @return the time of the message, or {@code null} if none is set
     */
    @JsonSerialize(using = ToStringSerializer.class)
    public Instant getTime() {
        return time;
    }

    /**
     * Sets the time of sending the message. It will be displayed in the message
     * component.
     *
     * @param time
     *            the time of the message to set, or {@code null} to remove the
     *            time
     */
    public void setTime(Instant time) {
        this.time = time;
        propsChanged();
    }

    /**
     * Gets the user name of the message sender.
     *
     * @return the message sender's user name, or {@code null} if none is set
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name of the message sender. It will be displayed in the
     * message component and used in the message's avatar.
     * <p>
     * In the avatar, the user name is presented in a tooltip on hover. It will
     * be also used to generate an abbreviation that is displayed as the avatar
     * content, when no {@link #setUserImage(String)} image} or
     * {@link #setUserAbbreviation(String)} abbreviation} is explicitly defined.
     *
     * @param userName
     *            the message sender's user name, or {@code null} to remove the
     *            user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
        propsChanged();
    }

    /**
     * Gets the abbreviation of the message sender.
     *
     * @return the message sender's abbreviation, or {@code null} if none is set
     */
    @JsonProperty("userAbbr")
    public String getUserAbbreviation() {
        return userAbbreviation;
    }

    /**
     * Sets the abbreviation of the message sender. It will be used in the
     * message's avatar, when no {@link #setUserImage(String)} image} is
     * defined.
     *
     * @param userAbbreviation
     *            the message sender's abbreviation, or {@code null} to remove
     *            the abbreviation
     */
    public void setUserAbbreviation(String userAbbreviation) {
        this.userAbbreviation = userAbbreviation;
        propsChanged();
    }

    /**
     * Gets the URL to the message sender's image.
     *
     * @return the URL to the message sender's image, or {@code null} if none is
     *         set
     */
    @JsonProperty("userImg")
    public String getUserImage() {
        return userImage;
    }

    /**
     * Sets the URL to the message sender's image. The image be displayed in an
     * avatar in the message component.
     *
     * @param userImage
     *            the URL to the message sender's image, or {@code null} to
     *            remove the image
     */
    public void setUserImage(String userImage) {
        this.userImage = userImage;
        propsChanged();
    }

    /**
     * Gets the color index of the message sender.
     *
     * @return the color index, or {@code null} if none is set
     */
    public Integer getUserColorIndex() {
        return userColorIndex;
    }

    /**
     * Sets the color index of the message sender. It's used in the avatar that
     * is displayed in the message component.
     * <p>
     * The color index defines which color will be used for the border of the
     * avatar. Color index N applies CSS variable {@code --vaadin-user-color-N}
     * to the border.
     *
     * @param userColorIndex
     *            the color index to set, or {@code null} to remove it
     */
    public void setUserColorIndex(Integer userColorIndex) {
        this.userColorIndex = userColorIndex;
        propsChanged();
    }

    private void propsChanged() {
        if (getHost() != null) {
            getHost().scheduleItemsUpdate();
        }
    }

    void setHost(MessageList host) {
        this.host = host;
    }

    @JsonIgnore
    MessageList getHost() {
        return host;
    }

}
