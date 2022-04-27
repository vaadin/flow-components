package com.vaadin.client.communication;

import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;
import com.vaadin.client.ServerConnector;
import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.SpreadsheetServerRpcImpl;

/**
 * spreadsheet: we override this class to provide our own rpc implementation
 */
public class RpcProxy {

    static native void consoleLog(String message) /*-{
      console.log( "rpcproxy", message );
  }-*/;

    public RpcProxy() {
    }

    public static <T extends ServerRpc> T create(Class<T> rpcInterface, ServerConnector connector) {

        consoleLog("asking for " + rpcInterface.getName());


        if (SpreadsheetServerRpc.class.equals(rpcInterface)) {
            consoleLog("Returning " + SpreadsheetServerRpcImpl.class.getName() + " from fake RpcProxy");
            return (T) new SpreadsheetServerRpcImpl();
        }

        throw new IllegalStateException("" + rpcInterface + " is not supported");
    }

}
