import './env-setup.js';
import '../frontend/generated/jar-resources/vaadin-combo-box/comboBoxConnector.ts';
import * as sinon from 'sinon';
import type {
  ComboBoxConnector as ConnectorComboBoxConnector,
  ComboBoxServer as ConnectorComboBoxServer,
  FlowComboBox as ConnectorFlowComboBox,
  Item as ConnectorItem
} from '../frontend/generated/jar-resources/vaadin-combo-box/vaadin-combo-box-types.js';

export type Item = ConnectorItem & {
  label?: string;
};

export type ComboBoxServer = {
  [K in keyof ConnectorComboBoxServer]: ConnectorComboBoxServer[K] & sinon.SinonSpy;
};

// The connector API retyped with the test Item, so that tests can pass item
// literals with test-specific properties without excess property errors
export type ComboBoxConnector = Omit<ConnectorComboBoxConnector, 'filter' | 'set' | 'updateData'> & {
  filter(item: Item, filter: string): boolean;
  set(index: number, items: Item[], filter: string): void;
  updateData(items: Item[]): void;
};

export type FlowComboBox = ConnectorFlowComboBox & {
  $connector: ComboBoxConnector;
  $server: ComboBoxServer;
};

export const comboBoxConnector = window.Vaadin.Flow.comboBoxConnector;

export function init(comboBox: FlowComboBox): void {
  comboBox.$server = {
    setViewportRange: sinon.spy(),
    confirmUpdate: sinon.spy(),
    resetDataCommunicator: sinon.spy()
  };

  comboBoxConnector.initLazy(comboBox);
}
