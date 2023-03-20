
package com.vaadin.flow.component.messages.tests;

import java.time.Instant;
import java.util.Locale;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResource;

@Route("vaadin-messages/message-list-test")
public class MessageListPage extends Div {

    public MessageListPage() {
        UI.getCurrent().setLocale(Locale.ENGLISH);

        MessageListItem foo = new MessageListItem("foo",
                Instant.parse("2021-01-01T12:00:00.00Z"), "sender",
                "/test.jpg");
        foo.setUserAbbreviation("AB");
        foo.setUserColorIndex(1);

        MessageListItem bar = new MessageListItem("bar");

        MessageList messageList = new MessageList(foo, bar);
        add(messageList);

        addButton("setText", () -> foo.setText("foo2"));
        addButton("setTime",
                () -> foo.setTime(Instant.parse("2000-02-02T12:00:00.00Z")));
        addButton("setUserName", () -> foo.setUserName("sender2"));
        addButton("setUserImage", () -> foo.setUserImage("/test2.jpg"));
        addButton("setAbbreviation", () -> foo.setUserAbbreviation("CD"));
        addButton("setUserColorIndex", () -> foo.setUserColorIndex(2));
        addButton("addThemeNames", () -> foo.addThemeNames("foo", "bar"));
        addButton("removeThemeNames", () -> foo.removeThemeNames("foo", "bar"));

        addButton("setItems", () -> messageList
                .setItems(new MessageListItem(null, null, "sender3")));

        addButton("setLocale", () -> UI.getCurrent().setLocale(Locale.ITALIAN));

        addButton("detachList", () -> remove(messageList));
        addButton("attachList", () -> addComponentAsFirst(messageList));

        addButton("setImageAsStreamResource", () -> {
            StreamResource resource = new StreamResource("message-list-img",
                    () -> getClass().getResourceAsStream(
                            "/META-INF/resources/frontend/images/avatar.png"));
            foo.setUserImageResource(resource);
        });

    }

    private void addButton(String id, Command action) {
        NativeButton button = new NativeButton(id, e -> action.execute());
        button.setId(id);
        add(button);
    }
}
