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
package com.vaadin.flow.component.ai.orchestrator;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;

import com.vaadin.flow.component.ai.common.AiAttachment;
import com.vaadin.flow.component.ai.common.AttachmentContentType;
import com.vaadin.flow.component.ai.component.AiMessage;
import com.vaadin.flow.component.messages.MessageListItem;

/**
 * Wrapper for MessageListItem to implement AiMessage interface.
 */
class MessageListItemWrapper implements AiMessage {

    private static final int THUMBNAIL_MAX_SIZE = 200;

    private final MessageListItem item;

    MessageListItemWrapper(String text, String userName,
            List<AiAttachment> attachments) {
        item = new MessageListItem(text, Instant.now(), userName);
        if (attachments != null && !attachments.isEmpty()) {
            var messageAttachments = attachments.stream()
                    .map(MessageListItemWrapper::toMessageAttachment).toList();
            item.setAttachments(messageAttachments);
        }
    }

    MessageListItem getItem() {
        return item;
    }

    @Override
    public String getText() {
        return item.getText();
    }

    @Override
    public void setText(String text) {
        item.setText(text);
    }

    @Override
    public Instant getTime() {
        return item.getTime();
    }

    @Override
    public String getUserName() {
        return item.getUserName();
    }

    @Override
    public void appendText(String token) {
        item.appendText(token);
    }

    private static MessageListItem.Attachment toMessageAttachment(
            AiAttachment attachment) {
        // Only include thumbnail data URL for images to avoid sending
        // large file data (PDFs, videos, etc.) to the client
        var contentType = AttachmentContentType
                .fromMimeType(attachment.mimeType());
        var url = contentType == AttachmentContentType.IMAGE
                ? toThumbnailDataUrl(attachment)
                : null;
        return new MessageListItem.Attachment(attachment.name(), url,
                attachment.mimeType());
    }

    /**
     * Converts an image attachment to a thumbnail data URL.
     * <p>
     * If the image is larger than {@link #THUMBNAIL_MAX_SIZE} in either
     * dimension, it will be scaled down while preserving aspect ratio and
     * converted to JPEG format for smaller size.
     *
     * @param attachment
     *            the image attachment
     * @return a data URL with the thumbnail image, or the original data URL if
     *         scaling fails
     */
    private static String toThumbnailDataUrl(AiAttachment attachment) {
        try {
            var originalImage = ImageIO
                    .read(new ByteArrayInputStream(attachment.data()));
            if (originalImage == null) {
                return toDataUrl(attachment);
            }

            var originalWidth = originalImage.getWidth();
            var originalHeight = originalImage.getHeight();

            // If image is already small enough, return original
            if (originalWidth <= THUMBNAIL_MAX_SIZE
                    && originalHeight <= THUMBNAIL_MAX_SIZE) {
                return toDataUrl(attachment);
            }

            // Calculate scaled dimensions preserving aspect ratio
            var scale = Math.min((double) THUMBNAIL_MAX_SIZE / originalWidth,
                    (double) THUMBNAIL_MAX_SIZE / originalHeight);
            var scaledWidth = (int) (originalWidth * scale);
            var scaledHeight = (int) (originalHeight * scale);

            // Create scaled image
            var scaledImage = new BufferedImage(scaledWidth, scaledHeight,
                    BufferedImage.TYPE_INT_RGB);
            var g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();

            // Convert to JPEG for smaller size
            var outputStream = new ByteArrayOutputStream();
            ImageIO.write(scaledImage, "jpg", outputStream);
            var base64 = Base64.getEncoder()
                    .encodeToString(outputStream.toByteArray());
            return "data:image/jpeg;base64," + base64;
        } catch (IOException e) {
            // If scaling fails, return original data URL
            return toDataUrl(attachment);
        }
    }

    private static String toDataUrl(AiAttachment attachment) {
        return "data:" + attachment.mimeType() + ";base64,"
                + Base64.getEncoder().encodeToString(attachment.data());
    }
}
