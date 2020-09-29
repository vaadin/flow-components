package com.vaadin.flow.component.combobox.dataview;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataProvider;

import elemental.json.JsonValue;

public class ComboBoxLazyDataViewTest {

    private static final String TYPE_ERROR_MESSAGE = "ComboBoxLazyDataView "
            + "only supports 'BackEndDataProvider' or it's subclasses, "
            + "but was given a 'ListDataProvider'.\nUse either "
            + "'getLazyDataView()', 'getListDataView()' or "
            + "'getGenericDataView()' according to the used data type.";

    private String[] items = { "foo", "bar", "baz" };
    private ComboBoxLazyDataView<String> dataView;
    private ComboBox<String> comboBox;
    private DataCommunicatorTest.MockUI ui;
    private DataCommunicator<String> dataCommunicator;
    private ArrayUpdater arrayUpdater;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        BackEndDataProvider<String, String> dataProvider = DataProvider
                .fromFilteringCallbacks(query -> {
                    query.getOffset();
                    query.getLimit();
                    return Stream.of(items);
                }, query -> 3);

        comboBox = new ComboBox<>();
        ui = new DataCommunicatorTest.MockUI();
        ui.add(comboBox);

        ArrayUpdater.Update update = new ArrayUpdater.Update() {

            @Override
            public void clear(int start, int length) {

            }

            @Override
            public void set(int start, List<JsonValue> items) {

            }

            @Override
            public void commit(int updateId) {

            }
        };

        arrayUpdater = Mockito.mock(ArrayUpdater.class);
        Mockito.when(arrayUpdater.startUpdate(Mockito.anyInt()))
                .thenReturn(update);

        dataCommunicator = new DataCommunicator<>((item, jsonObject) -> {
        }, arrayUpdater, null, comboBox.getElement().getNode());

        dataCommunicator.setDataProvider(dataProvider, null);
        dataCommunicator.setPageSize(50);

        dataView = new ComboBoxLazyDataView<>(dataCommunicator, comboBox);
    }

    @Test
    public void setItemCountCallback_switchFromUndefinedSize_definedSize() {
        Assert.assertTrue(dataCommunicator.isDefinedSize());

        dataView.setItemCountUnknown();
        Assert.assertFalse(dataCommunicator.isDefinedSize());

        dataView.setItemCountCallback(query -> 5);
        Assert.assertTrue(dataCommunicator.isDefinedSize());
    }

    @Test
    public void setItemCountCallback_setAnotherCountCallback_itemCountChanged() {
        final AtomicInteger itemCount = new AtomicInteger(0);
        dataView.addItemCountChangeListener(
                event -> itemCount.set(event.getItemCount()));
        dataCommunicator.setRequestedRange(0, 50);

        fakeClientCommunication();

        Assert.assertEquals("Expected 3 items before setItemCountCallback()", 3,
                itemCount.getAndSet(0));

        dataView.setItemCountCallback(query -> 2);

        fakeClientCommunication();

        Assert.assertEquals("Expected 2 items after setItemCountCallback()", 2,
                itemCount.get());
    }

    @Test
    public void getLazyDataView_defaulDataProvider_dataViewReturned() {
        ComboBox<String> comboBox = new ComboBox<>();
        ComboBoxLazyDataView<String> lazyDataView = comboBox.getLazyDataView();

        Assert.assertNotNull(lazyDataView);
    }

    @Test
    public void lazyDataViewAPI_comboBoxNotOpenedYet_dataProviderVerificationPassed() {
        ComboBox<String> comboBox = new ComboBox<>();
        ComboBoxLazyDataView<String> dataView = comboBox
                .setItems(query -> Stream.of("foo"));
        dataView.setItemCountEstimate(1000);
        dataView.setItemCountEstimateIncrease(1000);
        dataView.setItemCountUnknown();
        dataView.getItemCountEstimateIncrease();
        dataView.getItemCountEstimate();
        dataView.setItemCountFromDataProvider();
        dataView.setItemCountCallback(query -> 0);
    }

    @Test
    public void setItemCountEstimate_defaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().setItemCountEstimate(1000);
    }

    @Test
    public void setItemCountEstimateIncrease_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().setItemCountEstimateIncrease(1000);
    }

    @Test
    public void setItemCountCallback_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().setItemCountCallback(query -> 0);
    }

    @Test
    public void setItemCountFromDataProvider_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().setItemCountFromDataProvider();
    }

    @Test
    public void setItemCountUnknown_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().setItemCountUnknown();
    }

    @Test
    public void getItemCountEstimate_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().getItemCountEstimate();
    }

    @Test
    public void getItemCountEstimateIncrease_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().getItemCountEstimateIncrease();
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

}
