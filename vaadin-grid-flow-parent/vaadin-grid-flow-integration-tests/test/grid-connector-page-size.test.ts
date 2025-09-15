import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import {
  init,
  getBodyRowCount,
  getBodyCellText,
  setRootItems,
} from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector', () => {
  let grid: FlowGrid;

  beforeEach(() => {
    grid = fixtureSync(`
      <vaadin-grid height="400px">
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
      <style>
        vaadin-grid::part(cell) {
          min-height: 36px;
        }
      </style>
    `);

    init(grid);
  });

  describe('small page size', () => {
    beforeEach(async () => {
      grid.pageSize = 5;
      grid.$connector.reset();
      await nextFrame();
      setRootItems(
        grid.$connector,
        Array.from({ length: 15 }, (_, i) => ({ key: `${i}`, name: `Item ${i}` }))
      );
      await nextFrame();
    });

    it('should render correct rows', () => {
      expect(getBodyRowCount(grid)).to.equal(15);

      for (let i = 0; i < 15; i++) {
        expect(getBodyCellText(grid, i, 0)).to.equal(`Item ${i}`);
      }
    });
  });
});
