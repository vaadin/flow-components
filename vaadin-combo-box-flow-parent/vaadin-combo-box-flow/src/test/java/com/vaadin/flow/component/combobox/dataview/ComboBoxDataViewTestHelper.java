
package com.vaadin.flow.component.combobox.dataview;

import java.lang.reflect.Method;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;

final class ComboBoxDataViewTestHelper {

    private ComboBoxDataViewTestHelper() {
    }

    static void setClientSideFilter(ComboBox<String> comboBox,
            String clientFilter) {
        try {
            // Reset the client filter on server side as though it's sent from
            // client
            Method setRequestedRangeMethod = ComboBox.class.getDeclaredMethod(
                    "setRequestedRange", int.class, int.class, String.class);
            setRequestedRangeMethod.setAccessible(true);
            setRequestedRangeMethod.invoke(comboBox, 0, comboBox.getPageSize(),
                    clientFilter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void fakeClientCommunication(UI ui) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
