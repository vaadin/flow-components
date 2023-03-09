import './env-setup.js';
import '../../vaadin-grid-flow/src/main/resources/META-INF/resources/frontend/gridConnector.js';
import sinon from 'sinon';
import type { Grid } from '../../../node_modules/@vaadin/grid/vaadin-grid.js';
import type {} from '@web/test-runner-mocha';

export type GridConnector = {
  initLazy: (grid: Grid) => void;
  updateSize: (size: number) => void;
  set: (index: number, items: any[], parentKey: string | null) => void;
  confirm: (index: number) => void;
  setSelectionMode: (mode: 'SINGLE' | 'NONE') => void;
};

export type GridServer = {
  confirmUpdate: (index: number) => void;
  select: (key: string) => void;
  deselect: (key: string) => void;
  setDetailsVisible: (key: string) => void;
};

export type Item = {
  key: string;
  name: string;
  selected?: boolean;
};

export type FlowGrid = Grid<Item> & {
  $connector: GridConnector;
  $server: GridServer;
  __deselectDisallowed: boolean;
  __disallowDetailsOnClick: boolean;
  _effectiveSize: number;
};

type Vaadin = {
  Flow: {
    gridConnector: GridConnector;
  };
};

const Vaadin = window.Vaadin as Vaadin;
export const gridConnector = Vaadin.Flow.gridConnector;

export function init(grid: FlowGrid): void {
  // Stub the necessary server-side methods
  grid.$server = {
    confirmUpdate: sinon.stub(),
    select: sinon.spy(),
    deselect: sinon.spy(),
    setDetailsVisible: sinon.spy()
  };

  gridConnector.initLazy(grid);
}