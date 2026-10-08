import './env-setup.js';
import { ComboBox } from '@vaadin/combo-box';
import '../frontend/generated/jar-resources/comboBoxConnector.js';
import * as sinon from 'sinon';

export type Item = {
  key: string;
  label?: string;
};

export type ComboBoxConnector = {
  initLazy: (comboBox: ComboBox) => void;
  reset: () => void;
  set: (index: number, items: Item[], filter: string) => void;
  confirm: (id: number, filter: string) => void;
  updateSize: (size: number) => void;
};

export type ComboBoxServer = {
  setViewportRange: sinon.SinonSpy;
  confirmUpdate: sinon.SinonSpy;
  resetDataCommunicator: sinon.SinonSpy;
};

export type FlowComboBox = ComboBox & {
  $connector: ComboBoxConnector;
  $server: ComboBoxServer;
  _filterDebouncer: unknown;
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
