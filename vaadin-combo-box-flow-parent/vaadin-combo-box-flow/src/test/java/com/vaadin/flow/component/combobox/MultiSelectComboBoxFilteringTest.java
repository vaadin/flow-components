/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.data.provider.DataCommunicatorTest;

public class MultiSelectComboBoxFilteringTest {
    private DataCommunicatorTest.MockUI ui;

    @Before
    public void setUp() {
        ui = new DataCommunicatorTest.MockUI();
    }

    @Test
    public void filter_addAndRefreshItems_doesNotToggleClientSideFiltering() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        ui.add(comboBox);

        List<String> items = new ArrayList<>(IntStream.range(0, 100)
                .mapToObj(i -> "Item " + i).collect(Collectors.toList()));
        comboBox.setItems(items);

        comboBox.getDataController().setRequestedRange(0, 50, "foo");
        fakeClientCommunication();
        Assert.assertFalse((Boolean) comboBox.getElement()
                .getPropertyRaw("_clientSideFilter"));

        items.add("foo");
        comboBox.getDataProvider().refreshAll();
        comboBox.getDataController().setRequestedRange(0, 50, "");
        fakeClientCommunication();
        Assert.assertFalse((Boolean) comboBox.getElement()
                .getPropertyRaw("_clientSideFilter"));
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
