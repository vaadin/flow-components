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
  confirmUpdate: sinon.SinonSpy;
};

export type ComboBoxConnectorApi = ComboBoxConnector & {
  set: (index: number, items: unknown[], filter: string) => void;
  confirm: (id: number, filter: string) => void;
};

export type FlowComboBox = ComboBox & {
  $connector: ComboBoxConnectorApi;
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
    setViewportRange: sinon.spy(),
    confirmUpdate: sinon.spy()
  };

  comboBoxConnector.initLazy(comboBox);
}

export function plainItems(startIndex: number, count: number): Array<{ key: string; label: string }> {
  return Array.from({ length: count }, (_, i) => ({
    key: String(startIndex + i + 1),
    label: `Item ${startIndex + i}`
  }));
}

// Items rendered via a server-side ComponentRenderer carry a property
// whose name ends in `_nodeid` — the namespace comes from
// `LitRenderer.getPropertyNamespace()` and looks like `lr_<hash>_nodeid`
// in production. The connector uses that suffix to decide whether to
// evict the page on a non-contiguous jump.
export function rendererItems(
  startIndex: number,
  count: number
): Array<{ key: string; label: string; lr_test_nodeid: number }> {
  return plainItems(startIndex, count).map((item, i) => ({
    ...item,
    lr_test_nodeid: 100 + i
  }));
}
