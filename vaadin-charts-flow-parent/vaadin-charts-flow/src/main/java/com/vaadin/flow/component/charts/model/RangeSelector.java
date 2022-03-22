package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.charts.model.style.ButtonTheme;
import java.util.ArrayList;
import java.util.Arrays;
import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.model.style.Style;

/**
 * The range selector is a tool for selecting ranges to display within the
 * chart. It provides buttons to select preconfigured ranges in the chart, like
 * 1 day, 1 week, 1 month etc. It also provides input boxes where min and max
 * dates can be manually input.
 */
public class RangeSelector extends AbstractConfigurationObject {

    private Boolean allButtonsEnabled;
    private ButtonPosition buttonPosition;
    private Number buttonSpacing;
    private ButtonTheme buttonTheme;
    private ArrayList<RangeSelectorButton> buttons;
    private Boolean enabled;
    private Number height;
    private Color inputBoxBorderColor;
    private Number inputBoxHeight;
    private Number inputBoxWidth;
    private String inputDateFormat;
    private String _fn_inputDateParser;
    private String inputEditDateFormat;
    private Boolean inputEnabled;
    private ButtonPosition inputPosition;
    private Style inputStyle;
    private Style labelStyle;
    private Number selected;

    public RangeSelector() {
    }

    /**
     * @see #setAllButtonsEnabled(Boolean)
     */
    public Boolean getAllButtonsEnabled() {
        return allButtonsEnabled;
    }

    /**
     * Whether to enable all buttons from the start. By default buttons are only
     * enabled if the corresponding time range exists on the X axis, but
     * enabling all buttons allows for dynamically loading different time
     * ranges.
     * <p>
     * Defaults to: false
     */
    public void setAllButtonsEnabled(Boolean allButtonsEnabled) {
        this.allButtonsEnabled = allButtonsEnabled;
    }

    /**
     * @see #setButtonPosition(ButtonPosition)
     */
    public ButtonPosition getButtonPosition() {
        if (buttonPosition == null) {
            buttonPosition = new ButtonPosition();
        }
        return buttonPosition;
    }

    /**
     * A fixed pixel position for the buttons. Supports two properties,
     * <code>x</code> and <code>y<code>.
     */
    public void setButtonPosition(ButtonPosition buttonPosition) {
        this.buttonPosition = buttonPosition;
    }

    /**
     * @see #setButtonSpacing(Number)
     */
    public Number getButtonSpacing() {
        return buttonSpacing;
    }

    /**
     * The space in pixels between the buttons in the range selector.
     * <p>
     * Defaults to: 0
     */
    public void setButtonSpacing(Number buttonSpacing) {
        this.buttonSpacing = buttonSpacing;
    }

    /**
     * @see #setButtonTheme(ButtonTheme)
     */
    public ButtonTheme getButtonTheme() {
        if (buttonTheme == null) {
            buttonTheme = new ButtonTheme();
        }
        return buttonTheme;
    }

    /**
     * <p>
     * A collection of attributes for the buttons. The object takes SVG
     * attributes like <code>fill</code>, <code>stroke</code>,
     * <code>stroke-width</code>, as well as <code>style</code>, a collection of
     * CSS properties for the text.
     * </p>
     *
     * <p>
     * The object can also be extended with states, so you can set
     * presentational options for <code>hover</code>, <code>select</code> or
     * <code>disabled</code> button states.
     * </p>
     *
     * <p>
     * CSS styles for the text label.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the buttons are styled by the
     * <code>.highcharts-range-selector-buttons .highcharts-button</code> rule
     * with its different states.
     * </p>
     */
    public void setButtonTheme(ButtonTheme buttonTheme) {
        this.buttonTheme = buttonTheme;
    }

    /**
     * @see #setButtons(RangeSelectorButton...)
     */
    public RangeSelectorButton[] getButtons() {
        if (buttons == null) {
            return new RangeSelectorButton[] {};
        }
        RangeSelectorButton[] arr = new RangeSelectorButton[buttons.size()];
        buttons.toArray(arr);
        return arr;
    }

    /**
     * <p>
     * An array of configuration objects for the buttons.
     * </p>
     *
     * Defaults to
     *
     * <pre>
     * buttons: [{
     * 		type: 'month',
     * 		count: 1,
     * 		text: '1m'
     * 	}, {
     * 		type: 'month',
     * 		count: 3,
     * 		text: '3m'
     * 	}, {
     * 		type: 'month',
     * 		count: 6,
     * 		text: '6m'
     * 	}, {
     * 		type: 'ytd',
     * 		text: 'YTD'
     * 	}, {
     * 		type: 'year',
     * 		count: 1,
     * 		text: '1y'
     * 	}, {
     * 		type: 'all',
     * 		text: 'All'
     * 	}]
     * </pre>
     */
    public void setButtons(RangeSelectorButton... buttons) {
        this.buttons = new ArrayList<RangeSelectorButton>(
                Arrays.asList(buttons));
    }

    /**
     * Adds button to the buttons array
     *
     * @param button
     *            to add
     * @see #setButtons(RangeSelectorButton...)
     */
    public void addButton(RangeSelectorButton button) {
        if (this.buttons == null) {
            this.buttons = new ArrayList<RangeSelectorButton>();
        }
        this.buttons.add(button);
    }

    /**
     * Removes first occurrence of button in buttons array
     *
     * @param button
     *            to remove
     * @see #setButtons(RangeSelectorButton...)
     */
    public void removeButton(RangeSelectorButton button) {
        this.buttons.remove(button);
    }

    public RangeSelector(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the range selector.
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setHeight(Number)
     */
    public Number getHeight() {
        return height;
    }

    /**
     * The height of the range selector, used to reserve space for buttons and
     * input.
     * <p>
     * Defaults to: 35
     */
    public void setHeight(Number height) {
        this.height = height;
    }

    /**
     * @see #setInputBoxBorderColor(Color)
     */
    public Color getInputBoxBorderColor() {
        return inputBoxBorderColor;
    }

    /**
     * The border color of the date input boxes.
     * <p>
     * Defaults to: #cccccc
     */
    public void setInputBoxBorderColor(Color inputBoxBorderColor) {
        this.inputBoxBorderColor = inputBoxBorderColor;
    }

    /**
     * @see #setInputBoxHeight(Number)
     */
    public Number getInputBoxHeight() {
        return inputBoxHeight;
    }

    /**
     * The pixel height of the date input boxes.
     * <p>
     * Defaults to: 17
     */
    public void setInputBoxHeight(Number inputBoxHeight) {
        this.inputBoxHeight = inputBoxHeight;
    }

    /**
     * @see #setInputBoxWidth(Number)
     */
    public Number getInputBoxWidth() {
        return inputBoxWidth;
    }

    /**
     * The pixel width of the date input boxes.
     * <p>
     * Defaults to: 90
     */
    public void setInputBoxWidth(Number inputBoxWidth) {
        this.inputBoxWidth = inputBoxWidth;
    }

    /**
     * @see #setInputDateFormat(String)
     */
    public String getInputDateFormat() {
        return inputDateFormat;
    }

    /**
     * The date format in the input boxes when not selected for editing.
     * Defaults to <code>%b %e, %Y</code>.
     * <p>
     * Defaults to: %b %e %Y,
     */
    public void setInputDateFormat(String inputDateFormat) {
        this.inputDateFormat = inputDateFormat;
    }

    public String getInputDateParser() {
        return _fn_inputDateParser;
    }

    public void setInputDateParser(String _fn_inputDateParser) {
        this._fn_inputDateParser = _fn_inputDateParser;
    }

    /**
     * @see #setInputEditDateFormat(String)
     */
    public String getInputEditDateFormat() {
        return inputEditDateFormat;
    }

    /**
     * The date format in the input boxes when they are selected for editing.
     * This must be a format that is recognized by JavaScript Date.parse.
     * <p>
     * Defaults to: %Y-%m-%d
     */
    public void setInputEditDateFormat(String inputEditDateFormat) {
        this.inputEditDateFormat = inputEditDateFormat;
    }

    /**
     * @see #setInputEnabled(Boolean)
     */
    public Boolean getInputEnabled() {
        return inputEnabled;
    }

    /**
     * Enable or disable the date input boxes. Defaults to enabled when there is
     * enough space, disabled if not (typically mobile).
     */
    public void setInputEnabled(Boolean inputEnabled) {
        this.inputEnabled = inputEnabled;
    }

    /**
     * @see #setInputPosition(ButtonPosition)
     */
    public ButtonPosition getInputPosition() {
        if (inputPosition == null) {
            inputPosition = new ButtonPosition();
        }
        return inputPosition;
    }

    /**
     * Positioning for the input boxes. Allowed properties are
     * <code>align</code>, <code>verticalAlign</code>, <code>x</code> and
     * <code>y</code>.
     * <p>
     * Defaults to: { align: "right" }
     */
    public void setInputPosition(ButtonPosition inputPosition) {
        this.inputPosition = inputPosition;
    }

    /**
     * @see #setInputStyle(Style)
     */
    public Style getInputStyle() {
        if (inputStyle == null) {
            inputStyle = new Style();
        }
        return inputStyle;
    }

    /**
     * <p>
     * CSS for the HTML inputs in the range selector.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the inputs are styled by the
     * <code>.highcharts-range-input text</code> rule in SVG mode, and
     * <code>input.highcharts-range-selector</code> when active.
     * </p>
     */
    public void setInputStyle(Style inputStyle) {
        this.inputStyle = inputStyle;
    }

    /**
     * @see #setLabelStyle(Style)
     */
    public Style getLabelStyle() {
        if (labelStyle == null) {
            labelStyle = new Style();
        }
        return labelStyle;
    }

    /**
     * <p>
     * CSS styles for the labels - the Zoom, From and To texts.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the labels are styled by the
     * <code>.highcharts-range-label</code> class.
     * </p>
     */
    public void setLabelStyle(Style labelStyle) {
        this.labelStyle = labelStyle;
    }

    /**
     * @see #setSelected(Number)
     */
    public Number getSelected() {
        return selected;
    }

    /**
     * The index of the button to appear pre-selected.
     * <p>
     * Defaults to: undefined
     */
    public void setSelected(Number selected) {
        this.selected = selected;
    }
}
