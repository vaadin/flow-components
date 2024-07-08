/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.avatar.demo;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;

/**
 * View for {@link Avatar} demo.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-avatar")
public class AvatarView extends DemoView {

    StreamResource localAvatarResource;

    @Override
    public void initView() {
        localAvatarResource = new StreamResource("avatar+.png",
                () -> getClass().getResourceAsStream(
                        "/META-INF/resources/frontend/images/avatar.png"));

        createBasicAvatar();
        createAvatarWithCombinedProperties();
        createLocalizedAvatar();

        addCard("Resource helper method",
                new Text("This method is used in the examples above"));
    }

    private void createBasicAvatar() {
        // begin-source-example
        // source-example-heading: Basic usage
        Avatar anonymousAvatar = new Avatar();

        Avatar avatarWithAbbr = new Avatar();
        avatarWithAbbr.setAbbreviation("YY");

        Avatar avatarWithName = new Avatar();
        avatarWithName.setName("Yuriy Yevstihnyeyev");

        Avatar avatarWithImgUrl = new Avatar();
        avatarWithImgUrl.setImage("https://vaadin.com/avatars/avatar.png");

        Avatar avatarWithImageResource = new Avatar();

        StreamResource avatarResource = new StreamResource("user+.png",
                () -> getClass().getResourceAsStream(
                        "/META-INF/resources/frontend/images/user.png"));
        avatarWithImageResource.setImageResource(avatarResource);

        add(anonymousAvatar, avatarWithAbbr, avatarWithName, avatarWithImgUrl,
                avatarWithImageResource);
        // end-source-example

        // Not using external image urls
        avatarWithImgUrl.setImageResource(localAvatarResource);

        Div container = new Div(anonymousAvatar, avatarWithAbbr, avatarWithName,
                avatarWithImgUrl, avatarWithImageResource);

        addCard("Basic usage", container);
    }

    private void createAvatarWithCombinedProperties() {
        // begin-source-example
        // source-example-heading: Combined properties
        Avatar avatar = new Avatar();
        avatar.setImage("https://vaadin.com/avatars/user.png");

        add(avatar);
        // end-source-example

        avatar.setImageResource(localAvatarResource);

        CheckboxGroup checkboxGroup = new CheckboxGroup();
        checkboxGroup.setLabel("Set avatar's properties");
        checkboxGroup.setItems("setImage(\"avatars/avatar.png\")",
                "setName(\"Serhii Kulykov\")", "setAbbreviation(\"YY\")");
        checkboxGroup.setValue(
                Collections.singleton("setImage(\"avatars/avatar.png\")"));
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        checkboxGroup.addValueChangeListener(e -> {
            String valueString = e.getValue().toString();

            if (valueString.contains("setImage")) {
                avatar.setImageResource(localAvatarResource);
            } else {
                avatar.setImageResource(null);
            }

            if (valueString.contains("setName")) {
                avatar.setName("Serhii Kulykov");
            } else {
                avatar.setName(null);
            }

            if (valueString.contains("setAbbreviation")) {
                avatar.setAbbreviation("YY");
            } else {
                avatar.setAbbreviation(null);
            }
        });

        addCard("Combined properties", avatar, checkboxGroup);
    }

    private void createLocalizedAvatar() {
        // begin-source-example
        // source-example-heading: Localized Avatar
        Avatar anonymousAvatar = new Avatar();

        anonymousAvatar
                .setI18n(new Avatar.AvatarI18n().setAnonymous("anonyymi"));

        add(anonymousAvatar);
        // end-source-example
        Div container = new Div(anonymousAvatar);

        addCard("Localized Avatar", container);
    }

    // begin-source-example
    // source-example-heading: Resource helper method

    public static InputStream getFileStream(String filePath) {
        try {
            return new FileInputStream(filePath);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    // end-source-example
}
