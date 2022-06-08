package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.vaadin.component.spreadsheet.client.js.SpreadsheetServerRpcImpl;
import com.vaadin.component.spreadsheet.client.js.SpreadsheetServerRpcImpl.JsConsumer;

@SuppressWarnings("serial")
public class PopupButtonServerRpcImpl implements PopupButtonServerRpc {

    private JsConsumer<Void> popupButtonClickCallback;
    private JsConsumer<Void> popupCloseCallback;

    @Override
    public void onPopupButtonClick(int row, int column) {
        call(popupButtonClickCallback, row, column);
    }

    @Override
    public void onPopupClose(int row, int column) {
        call(popupCloseCallback, row, column);
    }

    public void setPopupCloseCallback(JsConsumer<Void> popupCloseCallback) {
        this.popupCloseCallback = popupCloseCallback;
    }

    public void setPopupButtonClickCallback(
            JsConsumer<Void> popupButtonClickCallback) {
        this.popupButtonClickCallback = popupButtonClickCallback;
    }

    private native void call(JsConsumer<?> fnc, Object... args) /*-{
      if (!fnc) {
          return;
      }
      var jsArr = [];
      for (var i = 0; i < args.length; i++) {
        var param = args[i]
        var gwtKey = Object.getOwnPropertyNames(param)
          .find(function(k) {return /^(a|value_0|value.*g\$)$/.test(k)});
        var value = gwtKey ? param[gwtKey] : param;
        jsArr.push(value)
      }
      fnc(jsArr);
    }-*/;
}
