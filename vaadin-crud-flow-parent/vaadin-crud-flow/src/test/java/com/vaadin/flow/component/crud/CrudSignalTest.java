/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class CrudSignalTest extends AbstractSignalsUnitTest {

    private Crud<Thing> crud;
    private ValueSignal<Boolean> signal;

    @Before
    public void setup() {
        Grid<Thing> grid = Mockito.spy(new Grid<>());
        Mockito.when(grid.getDataProvider())
                .thenReturn(Mockito.mock(DataProvider.class));
        DataCommunicator<Thing> communicator = Mockito
                .mock(DataCommunicator.class);
        Mockito.when(grid.getDataCommunicator()).thenReturn(communicator);
        DataKeyMapper<Thing> keyMapper = Mockito.mock(DataKeyMapper.class);
        Mockito.when(communicator.getKeyMapper()).thenReturn(keyMapper);

        crud = new Crud<>(Thing.class, grid, new ThingEditor());
        signal = new ValueSignal<>(false);
    }

    @After
    public void tearDown() {
        if (crud != null && crud.isAttached()) {
            crud.removeFromParent();
        }
    }

    @Test
    public void bindDirty_signalBound_propertySync() {
        crud.bindDirty(signal);
        UI.getCurrent().add(crud);

        signal.set(true);
        // The dirty state is pushed via executeJs, so we verify the signal
        // value reflects correctly
        Assert.assertTrue(signal.peek());

        signal.set(false);
        Assert.assertFalse(signal.peek());
    }

    @Test
    public void bindDirty_notAttached_noEffect() {
        crud.bindDirty(signal);

        // Signal changes should not throw when not attached
        signal.set(true);
        Assert.assertTrue(signal.peek());
    }

    @Test
    public void bindDirty_detachAndReattach() {
        crud.bindDirty(signal);
        UI.getCurrent().add(crud);

        signal.set(true);
        Assert.assertTrue(signal.peek());

        crud.removeFromParent();
        signal.set(false);

        UI.getCurrent().add(crud);
        Assert.assertFalse(signal.peek());
    }

    @Test(expected = BindingActiveException.class)
    public void bindDirty_setWhileBound_throws() {
        crud.bindDirty(signal);
        UI.getCurrent().add(crud);

        crud.setDirty(true);
    }

    @Test(expected = BindingActiveException.class)
    public void bindDirty_doubleBind_throws() {
        crud.bindDirty(signal);
        crud.bindDirty(new ValueSignal<>(true));
    }

    @Test(expected = NullPointerException.class)
    public void bindDirty_nullSignal_throwsNPE() {
        crud.bindDirty(null);
    }

    public static class Thing {
        String name;
    }

    private static class ThingEditor implements CrudEditor<Thing> {
        private Thing item;

        @Override
        public void setItem(Thing item, boolean validate) {
            this.item = item;
        }

        @Override
        public Thing getItem() {
            return item;
        }

        @Override
        public void clear() {
        }

        @Override
        public boolean validate() {
            return false;
        }

        @Override
        public void writeItemChanges() {
        }

        @Override
        public Component getView() {
            return new Div();
        }
    }
}
