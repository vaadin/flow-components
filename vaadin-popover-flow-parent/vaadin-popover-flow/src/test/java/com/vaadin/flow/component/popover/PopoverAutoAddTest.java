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
package com.vaadin.flow.component.popover;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.internal.ModalRoot;
import com.vaadin.tests.MockUIExtension;

/**
 * @author Vaadin Ltd.
 */
class PopoverAutoAddTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void setTarget_autoAdded() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);

        ui.fakeClientCommunication();
        Assertions.assertEquals(ui.getUI().getElement(),
                popover.getElement().getParent());
    }

    @Test
    void setTarget_clearTarget_autoRemoved() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        ui.fakeClientCommunication();

        popover.setTarget(null);

        ui.fakeClientCommunication();
        Assertions.assertNull(popover.getElement().getParent());
    }

    @Test
    void setTarget_detachTarget_autoRemoved() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        ui.fakeClientCommunication();

        ui.remove(target);

        ui.fakeClientCommunication();
        Assertions.assertNull(popover.getElement().getParent());
    }

    @Test
    void setTarget_removeAll_noException() {
        Div target = new Div();
        Popover popover = new Popover();
        popover.setTarget(target);
        ui.add(target);

        ui.removeAll();

        Assertions.assertNull(popover.getElement().getParent());
        Assertions.assertEquals(0, ui.getUI().getChildren().count());
    }

    @Test
    void setTarget_changeTarget_notAutoRemoved() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        ui.fakeClientCommunication();

        Div other = new Div();
        ui.add(other);
        popover.setTarget(other);
        ui.fakeClientCommunication();

        Assertions.assertEquals(ui.getUI().getElement(),
                popover.getElement().getParent());
    }

    @Test
    void setTarget_changeTarget_detachOldTarget_notAutoRemoved() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        ui.fakeClientCommunication();

        Div other = new Div();
        ui.add(other);
        popover.setTarget(other);
        ui.fakeClientCommunication();

        ui.remove(target);
        ui.fakeClientCommunication();
        Assertions.assertEquals(ui.getUI().getElement(),
                popover.getElement().getParent());
    }

    @Test
    void setTarget_changeToDetachedTarget_autoRemoved() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        ui.fakeClientCommunication();

        Div other = new Div();
        popover.setTarget(other);
        ui.fakeClientCommunication();

        Assertions.assertNull(popover.getElement().getParent());
    }

    @Test
    void setTarget_changeUI_autoAdded() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        ui.fakeClientCommunication();

        // Create a new UI and move the component to it (@PreserveOnRefresh)
        target.getElement().removeFromTree(false);
        ui.replaceUI();
        ui.add(target);

        ui.fakeClientCommunication();
        Assertions.assertEquals(ui.getUI().getElement(),
                popover.getElement().getParent());
    }

    @Test
    void setTarget_openModal_popoverIsAttachedToUi() {
        Div target = new Div();
        Popover popover = new Popover();
        popover.setTarget(target);
        ui.add(target);

        Div modalElement = new Div();
        ui.getUI().setChildComponentModal(modalElement, true);
        ui.fakeClientCommunication();

        Assertions.assertEquals(ui.getUI(), popover.getParent().orElseThrow());
    }

    @Test
    void openModal_setTargetOutsideOfModal_popoverIsAttachedToUi() {
        Div modal = new Div();
        ui.add(modal);
        ui.getUI().setChildComponentModal(modal, true);

        Div target = new Div();
        Popover popover = new Popover();
        popover.setTarget(target);
        ui.add(target);

        Assertions.assertEquals(ui.getUI(), popover.getParent().orElseThrow());
    }

    @Test
    void popoverWithTargetInPopover_popoverAttachedToPopover() {
        var firstPopover = new Popover();
        ui.add(firstPopover);
        var target = new Div();
        firstPopover.add(target);
        var secondPopover = new Popover();
        secondPopover.setTarget(target);
        ui.fakeClientCommunication();
        Assertions.assertEquals(firstPopover,
                secondPopover.getParent().orElse(null),
                "Second popover should be attached to first popover");
    }

    @Test
    void popoverWithTargetInModalComponent_popoverAttachedToModal() {
        var modal = new TestModalComponent();
        ui.add(modal);
        var target = new Div();
        modal.getElement().appendChild(target.getElement());
        var popover = new Popover();
        popover.setTarget(target);
        ui.fakeClientCommunication();
        Assertions.assertEquals(modal, popover.getParent().orElse(null),
                "Popover should be attached to modal");
    }

    @Test
    void popoverWithTargetInModalComponent_targetRemoved_popoverDetached() {
        var modal = new TestModalComponent();
        ui.add(modal);
        var target = new Div();
        modal.getElement().appendChild(target.getElement());
        var popover = new Popover();
        popover.setTarget(target);
        ui.fakeClientCommunication();

        target.removeFromParent();
        ui.fakeClientCommunication();
        Assertions.assertFalse(popover.getParent().isPresent(),
                "Popover should be detached");
    }

    @Test
    void popoverWithTargetInModalContainer_popoverAttachedToModal() {
        var modal = new TestModalContainer();
        ui.add(modal);
        var target = new Div();
        modal.add(target);
        var popover = new Popover();
        popover.setTarget(target);
        ui.fakeClientCommunication();
        Assertions.assertEquals(modal, popover.getParent().orElse(null),
                "Popover should be attached to modal");
    }

    @Test
    void popoverWithTargetInModalSubContainer_popoverAttachedToModal() {
        var modal = new TestModalSubContainer();
        ui.add(modal);
        var target = new Div();
        modal.add(target);
        var popover = new Popover();
        popover.setTarget(target);
        ui.fakeClientCommunication();
        Assertions.assertEquals(modal, popover.getParent().orElse(null),
                "Popover should be attached to modal");
    }

    @Test
    void popoverWithTargetInModalContainer_targetRemoved_popoverDetached() {
        var modal = new TestModalContainer();
        ui.add(modal);
        var target = new Div();
        modal.add(target);
        var popover = new Popover();
        popover.setTarget(target);
        ui.fakeClientCommunication();

        target.removeFromParent();
        ui.fakeClientCommunication();
        Assertions.assertFalse(popover.getParent().isPresent(),
                "Popover should be detached");
    }

    @Test
    void targetWithinModalWithSlotDefined_popoverInheritsSlotAttribute() {
        var modal = new TestModalContainerWithSlot();
        ui.add(modal);
        var target = new Div();
        var popover = new Popover();
        popover.setTarget(target);
        modal.add(target);
        ui.fakeClientCommunication();

        Assertions.assertEquals("custom-slot",
                popover.getElement().getAttribute("slot"));

        ui.fakeClientCommunication();
        var newModal = new TestModalContainer();
        ui.add(newModal);
        newModal.add(target);
        ui.fakeClientCommunication();
        Assertions.assertFalse(popover.getElement().hasAttribute("slot"),
                "Popover should not have value for slot attribute");
    }

    @Test
    void popoverWithTargetAddedAsVirtualChild_popoverAttachedToModal() {
        var modal = new TestModalComponent();
        ui.add(modal);
        var target = new Div();
        modal.getElement().appendVirtualChild(target.getElement());
        var popover = new Popover();
        popover.setTarget(target);
        ui.fakeClientCommunication();
        Assertions.assertEquals(modal, popover.getParent().orElse(null),
                "Popover should be attached to modal");
    }

    @ModalRoot
    @Tag("div")
    public class TestModalContainer extends Component implements HasComponents {
        public TestModalContainer() {
            super();
        }
    }

    @ModalRoot
    @Tag("div")
    public class TestModalComponent extends Component {
        public TestModalComponent() {
            super();
        }
    }

    @ModalRoot(slot = "custom-slot")
    @Tag("div")
    public class TestModalContainerWithSlot extends Component
            implements HasComponents {
        public TestModalContainerWithSlot() {
            super();
        }
    }

    @Tag("div")
    public class TestModalSubContainer extends TestModalContainer {
        public TestModalSubContainer() {
            super();
        }
    }
}
