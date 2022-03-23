package com.vaadin.flow.component.confirmdialog.examples;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/styling")
public class StylingPage extends Div {
    public StylingPage() {
        ConfirmDialog dialog = new ConfirmDialog();

        Button addDialog = new Button("Add dialog", e -> add(dialog));
        addDialog.setId("add-dialog");

        Button openDialog = new Button("Open dialog", e -> dialog.open());
        openDialog.setId("open-dialog");

        Button addClassNameFoo = new Button("Add class foo",
                e -> dialog.addClassName("foo"));
        addClassNameFoo.setId("add-foo");

        Button setClassNameBar = new Button("Set class bar", e -> {
            dialog.setClassName("foo bar");
            dialog.getClassNames().set("foo", false);
        });
        setClassNameBar.setId("set-bar");

        Button removeClassNames = new Button("Remove classes", e -> {
            dialog.removeClassNames("foo", "bar");
        });
        removeClassNames.setId("remove-all");

        dialog.add(setClassNameBar, removeClassNames);

        add(addDialog, openDialog, addClassNameFoo);
    }
}
