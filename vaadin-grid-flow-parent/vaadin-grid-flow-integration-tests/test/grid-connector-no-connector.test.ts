import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { getBodyCellText, init } from './shared.js';
import type { Grid } from '@vaadin/grid';

/**
 * These tests target a <vaadin-grid> which is added on a page with gridConnector.js loaded
 * but the grid itself doesn't have a connector. An example of such a scenario is a
 * Flow view built with LitTemplate.
 */
describe('grid connector - no connector', () => {
  let grid: Grid;

  beforeEach(() => {
    // Have the connecor first initialize an unrelated grid instance
    init(document.createElement('vaadin-grid') as any);

    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-tree-column path="name"></vaadin-grid-tree-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);
    grid.itemIdPath = 'name';

    grid.dataProvider = ({ parentItem, page, pageSize }, cb) => {
      const pageItems = [...Array(2)].map((_, i) => {
        const indexInLevel = page * pageSize + i;
        return {
          name: `${parentItem ? parentItem.name + '-' : ''}${indexInLevel}`,
          children: true
        };
      });

      cb(pageItems, 2);
    };
  });

  it('should render the tree structure', async () => {
    grid.expandedItems = [{ name: '0' }, { name: '0-0' }];
    await nextFrame();

    expect(getBodyCellText(grid, 0, 0)).to.equal('0');
    expect(getBodyCellText(grid, 1, 0)).to.equal('0-0');
    expect(getBodyCellText(grid, 2, 0)).to.equal('0-0-0');
    expect(getBodyCellText(grid, 3, 0)).to.equal('0-0-1');
    expect(getBodyCellText(grid, 4, 0)).to.equal('0-1');
    expect(getBodyCellText(grid, 5, 0)).to.equal('1');
  });
});
