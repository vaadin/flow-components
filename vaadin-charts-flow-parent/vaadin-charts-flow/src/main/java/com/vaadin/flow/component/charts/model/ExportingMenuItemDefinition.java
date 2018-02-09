package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

public class ExportingMenuItemDefinition extends AbstractConfigurationObject {

	private String _fn_onclick;
	private String text;
	private String textKey;

	public String getOnclick() {
		return _fn_onclick;
	}

	public void setOnonclick(String _fn_onclick) {
		this._fn_onclick = _fn_onclick;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTextKey() {
		return textKey;
	}

	public void setTextKey(String textKey) {
		this.textKey = textKey;
	}
}
