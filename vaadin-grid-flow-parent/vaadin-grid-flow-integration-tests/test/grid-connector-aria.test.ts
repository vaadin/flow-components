import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, setRootItems, getBodyCell } from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - aria attributes', () => {
  let grid: FlowGrid;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
    await nextFrame();
  });

  function getTable(grid: FlowGrid): HTMLElement {
    return grid.shadowRoot!.querySelector('#table')!;
  }

  describe('aria-multiselectable', () => {
    it('should set aria-multiselectable false in single selection mode', () => {
      grid.$connector.setSelectionMode('SINGLE');
      expect(getTable(grid).getAttribute('aria-multiselectable')).to.equal('false');
    });

    it('should set aria-multiselectable true in multi selection mode', () => {
      grid.$connector.setSelectionMode('SINGLE');
      grid.$connector.setSelectionMode('MULTI');
      expect(getTable(grid).getAttribute('aria-multiselectable')).to.equal('true');
    });

    it('should remove aria-multiselectable in none selection mode', () => {
      grid.$connector.setSelectionMode('MULTI');
      grid.$connector.setSelectionMode('NONE');
      expect(getTable(grid).getAttribute('aria-multiselectable')).to.be.null;
    });

    it('should apply aria-multiselectable on attach for a selection mode set while detached', async () => {
      const container = fixtureSync('<div></div>');
      const detachedGrid = document.createElement('vaadin-grid') as FlowGrid;
      init(detachedGrid);

      // Setting the selection mode before the grid is rendered should not throw
      detachedGrid.$connector.setSelectionMode('SINGLE');

      container.appendChild(detachedGrid);
      await nextFrame();

      const table = detachedGrid.shadowRoot!.querySelector('#table')!;
      expect(table.getAttribute('aria-multiselectable')).to.equal('false');
    });
  });

  describe('aria-selected', () => {
    it('should remove aria-selected from rows and cells in none selection mode', async () => {
      // Select an item first so switching to NONE re-renders the affected rows
      grid.$connector.doSelection([{ key: '0' }], false);
      grid.$connector.setSelectionMode('NONE');
      await nextFrame();

      const cell = getBodyCell(grid, 0, 0)!;
      const row = cell.parentElement!;
      expect(row.getAttribute('aria-selected')).to.be.null;
      expect(cell.getAttribute('aria-selected')).to.be.null;
    });

    it('should keep aria-selected on rows and cells in single selection mode', async () => {
      grid.$connector.setSelectionMode('SINGLE');
      await nextFrame();

      const cell = getBodyCell(grid, 0, 0)!;
      const row = cell.parentElement!;
      expect(row.getAttribute('aria-selected')).to.equal('false');
      expect(cell.getAttribute('aria-selected')).to.equal('false');
    });
  });
});
