package com.vaadin.flow.component.grid;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.data.provider.CallbackDataProvider;

public class AbstractGridSingleSelectionModelTest {

    private static final Person PERSON_C = new Person("c", 3);
    private static final Person PERSON_B = new Person("b", 2);
    private static final Person PERSON_A = new Person("a", 1);

    private Grid<Person> grid;
    private AbstractGridSingleSelectionModel<Person> selectionModel;
    private CallbackDataProvider<Person, Void> spy;

    @Before
    public void init() {
        grid = new Grid<>();
        selectionModel = (AbstractGridSingleSelectionModel<Person>) grid
                .getSelectionModel();
        final CallbackDataProvider<Person, Void> dataProvider = new CallbackDataProvider<>(
                query -> Stream.of(PERSON_A, PERSON_B, PERSON_C), query -> 3,
                Person::getName);
        spy = Mockito.spy(dataProvider);
        grid.setItems(spy);
    }

    @Test // #2354
    public void selectItem_dataProviderWithIdentifierProvider_identityUsedForEqualsComparison() {
        grid.select(PERSON_A);

        // called 3 times - once by selection model and twice by KeyMapper
        Mockito.verify(spy, Mockito.times(3)).getId(PERSON_A);
        Mockito.verify(spy, Mockito.never()).getId(PERSON_B);

        Mockito.reset(spy);
        grid.select(PERSON_B);

        // called 3 times - once by selection model and twice by KeyMapper
        Mockito.verify(spy, Mockito.times(3)).getId(PERSON_B);
        // called 2 times - once by selection model and once by KeyMapper
        Mockito.verify(spy, Mockito.times(2)).getId(PERSON_A);

        Mockito.reset(spy);
        grid.select(null);

        // called 2 times - once by selection model and once by KeyMapper
        Mockito.verify(spy, Mockito.times(2)).getId(PERSON_B);
        Mockito.verify(spy, Mockito.never()).getId(null);
    }

    @Test // #2354
    public void selectFromClient_dataProviderWithIdentifierProvider_identityUsedForEqualsComparison() {
        selectionModel.selectFromClient(PERSON_A);

        Mockito.verify(spy, Mockito.times(1)).getId(PERSON_A);
        Mockito.verify(spy, Mockito.never()).getId(PERSON_B);

        Mockito.reset(spy);
        selectionModel.selectFromClient(PERSON_B);

        Mockito.verify(spy, Mockito.times(1)).getId(PERSON_B);
        Mockito.verify(spy, Mockito.times(1)).getId(PERSON_A);

        Mockito.reset(spy);
        selectionModel.selectFromClient(null);

        // called 2 times - once by selection model and once by KeyMapper
        Mockito.verify(spy, Mockito.times(1)).getId(PERSON_B);
        Mockito.verify(spy, Mockito.never()).getId(null);
    }
}
