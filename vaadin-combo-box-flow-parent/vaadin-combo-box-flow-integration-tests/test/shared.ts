import './env-setup.js';
import { ComboBox } from '@vaadin/combo-box';
import '../frontend/generated/jar-resources/comboBoxConnector.js';
import * as sinon from 'sinon';

export type ComboBoxConnector = {
  initLazy: (comboBox: ComboBox) => void;
  reset: () => void;
};

export type ComboBoxServer = {
  setViewportRange: sinon.SinonSpy;
};

export type FlowComboBox = ComboBox & {
  $connector: ComboBoxConnector;
  $server: ComboBoxServer;
  _filterTimeout: number;
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
    setViewportRange: sinon.spy()
  };

  comboBoxConnector.initLazy(comboBox);
}
