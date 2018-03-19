/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.dialog;

import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;

/**
 * Unit tests for the Dialog.
 */
public class DialogTest {

    @Test
    public void createDialogWithComponents_componentsArePartOfGetChildren() {
        UI.setCurrent(new UI());
        try {
            Label label1 = new Label("Label 1");
            Label label2 = new Label("Label 2");
            Label label3 = new Label("Label 3");

            Dialog dialog = new Dialog(label1, label2);

            List<Component> children = dialog.getChildren()
                    .collect(Collectors.toList());
            Assert.assertEquals(2, children.size());
            Assert.assertThat(children, CoreMatchers.hasItems(label1, label2));

            dialog.add(label3);
            children = dialog.getChildren().collect(Collectors.toList());
            Assert.assertEquals(3, children.size());
            Assert.assertThat(children,
                    CoreMatchers.hasItems(label1, label2, label3));

            dialog.remove(label2);
            children = dialog.getChildren().collect(Collectors.toList());
            Assert.assertEquals(2, children.size());
            Assert.assertThat(children, CoreMatchers.hasItems(label1, label3));

            label1.getElement().removeFromParent();
            children = dialog.getChildren().collect(Collectors.toList());
            Assert.assertEquals(1, children.size());
            Assert.assertThat(children, CoreMatchers.hasItems(label3));

            dialog.removeAll();
            children = dialog.getChildren().collect(Collectors.toList());
            Assert.assertEquals(0, children.size());
        } finally {
            UI.setCurrent(null);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void setOpened_noUi() {
        Dialog dialog = new Dialog();
        dialog.setOpened(true);
    }
}
