package com.vaadin.client.communication;

import java.util.logging.Logger;

import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;
import com.vaadin.client.ServerConnector;
import com.vaadin.component.spreadsheet.client.js.SpreadsheetServerRpcImpl;
import com.vaadin.shared.communication.ServerRpc;

/**
 * spreadsheet: we override this class to provide our own rpc implementation
 */
public class RpcProxy {

    final static Logger consoleLog = Logger.getLogger("spreadsheet RpcProxy");

    public RpcProxy() {
    }

    public static <T extends ServerRpc> T create(Class<T> rpcInterface,
            ServerConnector connector) {

        consoleLog.info("asking for " + rpcInterface.getName());

        if (SpreadsheetServerRpc.class.equals(rpcInterface)) {
            consoleLog.info(
                    "Returning " + SpreadsheetServerRpcImpl.class.getName()
                            + " from fake RpcProxy");
            return (T) new SpreadsheetServerRpcImpl();
        }

        throw new IllegalStateException(
                "" + rpcInterface + " is not supported");
    }

}
