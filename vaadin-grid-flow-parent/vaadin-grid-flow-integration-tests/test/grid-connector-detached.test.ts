import { expect } from 'chai';
import { aTimeout, fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import {
  init,
  getBodyRowCount,
  getBodyCellText,
  setRootItems,
  GRID_CONNECTOR_ROOT_REQUEST_DELAY
} from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - cache populated before attach', () => {
  let grid: FlowGrid;
  let container: HTMLElement;

  beforeEach(async () => {
    container = fixtureSync(`
      <div>
        <style>
          vaadin-grid::part(cell) {
            min-height: 36px;
          }
        </style>
      </div>
    `);

    // Create a grid and populate its cache through the connector while the
    // grid is still detached from the DOM. This happens e.g. when a grid is
    // rendered inside another grid's component renderer.
    grid = document.createElement('vaadin-grid') as FlowGrid;
    const column = document.createElement('vaadin-grid-column');
    column.setAttribute('path', 'name');
    grid.appendChild(column);

    init(grid);
    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);

    container.appendChild(grid);
    await nextFrame();
  });

  it('should render rows after attach', () => {
    expect(getBodyRowCount(grid)).to.equal(1);
    expect(getBodyCellText(grid, 0, 0)).to.equal('foo');
  });

  it('should not request data after attach', async () => {
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expect(grid.$server.setViewportRange).to.not.be.called;
  });
});
