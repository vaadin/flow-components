import './env-setup.js';
import { ComboBox } from '@vaadin/combo-box';
import '../frontend/generated/jar-resources/comboboxConnector.js';
import type {} from '@web/test-runner-mocha';
import sinon from 'sinon';

export type ComboBoxConnector = {
  initLazy: (comboBox: ComboBox) => void;
};

export type ComboBoxServer = {
  setViewportRange: sinon.SinonSpy;
};

export type FlowComboBox = ComboBox & {
  $connector: ComboBoxConnector;
  $server: ComboBoxServer;
  _filterTimeout: number;
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
    setViewportRange: sinon.spy()
  };

  comboBoxConnector.initLazy(comboBox);
}
