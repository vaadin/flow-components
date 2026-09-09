import { expect } from 'chai';
import './env-setup.js';
import '@vaadin/grid/src/all-imports.js';
// The paths the grid frontend files were available at before they were moved
// into the vaadin-grid folder. An add-on built against an earlier version
// imports them this way, so they have to keep working until the next major.
// This file deliberately imports nothing from the new paths, so that a passing
// test can only be explained by the deprecated entrypoints themselves.
import * as deprecatedGridConnector from '../frontend/generated/jar-resources/gridConnector.ts';
import '../frontend/generated/jar-resources/treeGridConnector.ts';
import * as deprecatedSelectionColumn from '../frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';

describe('grid connector - deprecated entrypoints', () => {
  it('should register the grid connector', () => {
    expect(window.Vaadin.Flow.gridConnector).to.exist;
  });

  it('should register the tree grid connector', () => {
    expect(window.Vaadin.Flow.treeGridConnector).to.exist;
  });

  it('should define the selection column element', () => {
    expect(customElements.get('vaadin-grid-flow-selection-column')).to.exist;
  });

  it('should re-export the grid connector class', () => {
    expect(deprecatedGridConnector.GridConnector).to.exist;
  });

  it('should re-export the selection column class', () => {
    expect(deprecatedSelectionColumn.GridFlowSelectionColumn).to.exist;
    expect(deprecatedSelectionColumn.GridFlowSelectionColumn).to.equal(
      customElements.get('vaadin-grid-flow-selection-column')
    );
  });
});
