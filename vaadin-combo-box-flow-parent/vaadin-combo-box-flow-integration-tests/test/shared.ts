import './env-setup.js';
import { ComboBox } from '@vaadin/combo-box';
import { DataProviderController } from '@vaadin/component-base/src/data-provider-controller/data-provider-controller.js';
import '../frontend/generated/jar-resources/comboBoxConnector.js';
import * as sinon from 'sinon';

export type ComboBoxConnector = {
  initLazy: (comboBox: ComboBox) => void;
  reset: () => void;
  set: (index: number, items: unknown[], filter: string) => void;
  confirm: (id: number, filter: string) => void;
};

export type ComboBoxServer = {
  setViewportRange: sinon.SinonSpy;
  confirmUpdate: sinon.SinonSpy;
  resetDataCommunicator: sinon.SinonSpy;
};

export type FlowComboBox = ComboBox & {
  $connector: ComboBoxConnector;
  $server: ComboBoxServer;
  _filterTimeout: number;
  _filterDebouncer: unknown;
  __dataProviderController: DataProviderController<unknown, Record<string, unknown>>;
};

type Vaadin = {
  Flow: {
    comboBoxConnector: ComboBoxConnector;
  };
};

const Vaadin = window.Vaadin as Vaadin;

export const comboBoxConnector = Vaadin.Flow.comboBoxConnector;

export function init(comboBox: FlowComboBox): void {
  comboBox.$server = {
    setViewportRange: sinon.spy(),
    confirmUpdate: sinon.spy(),
    resetDataCommunicator: sinon.spy()
  };

  comboBoxConnector.initLazy(comboBox);
}
