package com.vaadin.flow.component.board.test;

import org.junit.Test;

import com.vaadin.flow.component.board.test.AbstractTestCompUI;
import com.vaadin.flow.component.board.test.CompatBasicComponents;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.CheckBoxElement;
import com.vaadin.testbench.elements.CheckBoxGroupElement;
import com.vaadin.testbench.elements.ColorPickerElement;
import com.vaadin.testbench.elements.ComboBoxElement;
import com.vaadin.testbench.elements.DateFieldElement;
import com.vaadin.testbench.elements.GridElement;
import com.vaadin.testbench.elements.LabelElement;
import com.vaadin.testbench.elements.LinkElement;
import com.vaadin.testbench.elements.PanelElement;
import com.vaadin.testbench.elements.PasswordFieldElement;
import com.vaadin.testbench.elements.ProgressBarElement;
import com.vaadin.testbench.elements.RadioButtonGroupElement;
import com.vaadin.testbench.elements.SliderElement;
import com.vaadin.testbench.elements.TwinColSelectElement;
import com.vaadin.testbench.elements.VideoElement;

/**
 *
 */
public class CompatBasicComponentsIT extends AbstractParallelTest {

    @Test
    public void testButton()
        throws Exception {
        setUIClass(CompatBasicComponents.ButtonUI.class);
        openURL();
        ButtonElement testedElement = $(ButtonElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testCheckBoxGroup()
        throws Exception {
        setUIClass(CompatBasicComponents.CheckBoxGroupUI.class);
        openURL();
        CheckBoxGroupElement testedElement = $(CheckBoxGroupElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testCheckBox()
        throws Exception {
        setUIClass(CompatBasicComponents.CheckBoxUI.class);
        openURL();
        CheckBoxElement testedElement = $(CheckBoxElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testColorPicker()
        throws Exception {
        setUIClass(CompatBasicComponents.ColorPickerUI.class);
        openURL();
        ColorPickerElement testedElement = $(ColorPickerElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testComboBox()
        throws Exception {
        setUIClass(CompatBasicComponents.ComboBoxUI.class);
        openURL();
        ComboBoxElement testedElement = $(ComboBoxElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testDateField()
        throws Exception {
        setUIClass(CompatBasicComponents.DateFieldUI.class);
        openURL();
        DateFieldElement testedElement = $(DateFieldElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testGrid()
        throws Exception {
        setUIClass(CompatBasicComponents.GridUI.class);
        openURL();
        GridElement testedElement = $(GridElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testLabel()
        throws Exception {
        setUIClass(CompatBasicComponents.LabelUI.class);
        openURL();
        LabelElement testedElement = $(LabelElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testLink()
        throws Exception {
        setUIClass(CompatBasicComponents.LinkUI.class);
        openURL();
        LinkElement testedElement = $(LinkElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testPanel()
        throws Exception {
        setUIClass(CompatBasicComponents.PanelUI.class);
        openURL();
        PanelElement testedElement = $(PanelElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testPasswordField()
        throws Exception {
        setUIClass(CompatBasicComponents.PasswordFieldUI.class);
        openURL();
        PasswordFieldElement testedElement = $(PasswordFieldElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testProgressBar()
        throws Exception {
        setUIClass(CompatBasicComponents.ProgressBarUI.class);
        openURL();
        ProgressBarElement testedElement = $(ProgressBarElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testRadioButtonGroup()
        throws Exception {
        setUIClass(CompatBasicComponents.RadioButtonGroupUI.class);
        openURL();
        RadioButtonGroupElement testedElement = $(RadioButtonGroupElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testSlider()
        throws Exception {
        setUIClass(CompatBasicComponents.SliderUI.class);
        openURL();
        SliderElement testedElement = $(SliderElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testTwinColSelect()
        throws Exception {
        setUIClass(CompatBasicComponents.TwinColSelectUI.class);
        openURL();
        TwinColSelectElement testedElement = $(TwinColSelectElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testVideo()
        throws Exception {
        setUIClass(CompatBasicComponents.VideoUI.class);
        openURL();
        VideoElement testedElement = $(VideoElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    //  Flash does not work
    //    @Test
    //    public void testFlash() throws Exception {
    //      setUIClass(CompatBasicComponents.FlashUI.class);
    //      openURL();
    //      FlashElement testedElement = $(FlashElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
    //      testGenericWidth(testedElement);
    //    }

    //Tree is not working
    //    @Test
    //    public void testTree() throws Exception {
    //      setUIClass(CompatTreeUI.class);
    //      openURL();
    //      TreeElement testedElement = $(TreeElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
    //      testGenericWidth(testedElement);
    //    }

}
