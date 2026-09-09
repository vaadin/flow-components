import { expect } from 'chai';
import { aTimeout, nextFrame } from '@vaadin/testing-helpers';
import {
  init,
  getBodyRowCount,
  getBodyCellText,
  setRootItems,
  GRID_CONNECTOR_ROOT_REQUEST_DELAY
} from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - lazy attach to the DOM', () => {
  let grid: FlowGrid;

  beforeEach(async () => {
    grid = document.createElement('vaadin-grid') as FlowGrid;
    const column = document.createElement('vaadin-grid-column');
    column.path = 'name';
    grid.appendChild(column);
    init(grid);
  });

  afterEach(() => {
    grid.remove();
  });

  describe('populated before attach', () => {
    beforeEach(async () => {
      setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
      await nextFrame();
      document.body.appendChild(grid);
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
});
