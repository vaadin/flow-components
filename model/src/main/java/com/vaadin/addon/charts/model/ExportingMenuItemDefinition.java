package com.vaadin.addon.charts.model;

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
