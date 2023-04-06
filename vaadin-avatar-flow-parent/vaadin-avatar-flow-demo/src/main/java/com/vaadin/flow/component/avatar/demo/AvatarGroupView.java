
package com.vaadin.flow.component.avatar.demo;

import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.util.ArrayList;
import java.util.List;

import static com.vaadin.flow.component.avatar.demo.AvatarView.getFileStream;

/**
 * View for {@link AvatarGroup} demo.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-avatar-group")
public class AvatarGroupView extends DemoView {

    StreamResource localAvatarResource;

    @Override
    public void initView() {
        localAvatarResource = new StreamResource("avatar+.png",
                () -> getClass().getResourceAsStream(
                        "/META-INF/resources/frontend/images/avatar.png"));

        createBasicAvatarGroup();
        createMaxAvatarGroup();
        createLocalizedAvatarGroup();
    }

    private void createBasicAvatarGroup() {
        // begin-source-example
        // source-example-heading: Avatar Group
        AvatarGroup avatarGroup = new AvatarGroup();

        List<AvatarGroupItem> items = new ArrayList<>();

        items.add(new AvatarGroupItem("Yuriy Yevstihnyeyev"));

        AvatarGroupItem avatarWithAbbr = new AvatarGroupItem();
        avatarWithAbbr.setAbbreviation("SK");
        items.add(avatarWithAbbr);

        AvatarGroupItem avatarWithImageResource = new AvatarGroupItem();
        StreamResource avatarResource = new StreamResource("user+.png",
                () -> getClass().getResourceAsStream(
                        "/META-INF/resources/frontend/images/user.png"));
        avatarWithImageResource.setImageResource(avatarResource);
        items.add(avatarWithImageResource);

        items.add(new AvatarGroupItem("Jens Jansson"));
        items.add(new AvatarGroupItem("Yuriy Yevstihnyeyev",
                "https://vaadin.com/avatars/avatar.png"));

        avatarGroup.setItems(items);

        add(avatarGroup);
        // end-source-example

        // Not using external image urls
        items.get(4).setImage(null);
        items.get(4).setImageResource(localAvatarResource);
        avatarGroup.setItems(items);

        addCard("Avatar Group", avatarGroup);
    }

    private void createMaxAvatarGroup() {
        // begin-source-example
        // source-example-heading: Setting Max
        AvatarGroup avatarGroup = new AvatarGroup();

        avatarGroup.setMaxItemsVisible(3);

        List<AvatarGroupItem> items = new ArrayList<>();

        items.add(new AvatarGroupItem("Yuriy Yevstihnyeyev"));

        AvatarGroupItem avatarWithAbbr = new AvatarGroupItem();
        avatarWithAbbr.setAbbreviation("SK");
        items.add(avatarWithAbbr);

        items.add(new AvatarGroupItem("Leif Åstrand"));
        items.add(new AvatarGroupItem("Jens Jansson"));
        items.add(new AvatarGroupItem("Pekka Maanpää"));

        avatarGroup.setItems(items);

        add(avatarGroup);
        // end-source-example

        addCard("Setting Max", avatarGroup);
    }

    private void createLocalizedAvatarGroup() {
        // begin-source-example
        // source-example-heading: Localized Avatar Group
        AvatarGroup avatarGroup = new AvatarGroup();

        List<AvatarGroupItem> items = new ArrayList<>();

        items.add(new AvatarGroupItem());
        items.add(new AvatarGroupItem("Jens Jansson"));
        items.add(new AvatarGroupItem("Yuriy Yevstihnyeyev",
                "https://vaadin.com/avatars/avatar.png"));
        avatarGroup.setItems(items);
        add(avatarGroup);

        avatarGroup.setI18n(
                new AvatarGroup.AvatarGroupI18n().setAnonymous("anonyymi")
                        .setOneActiveUser("Yksi käyttäjä aktiivinen")
                        .setManyActiveUsers("{count} aktiivista käyttäjää"));

        // end-source-example

        // Not using external image urls
        items.get(2).setImage(null);
        items.get(2).setImageResource(localAvatarResource);
        avatarGroup.setItems(items);

        addCard("Localized Avatar Group", avatarGroup);
    }
}
