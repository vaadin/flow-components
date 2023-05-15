import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { init, gridConnector, getBodyRowCount, getBodyCellText, setRootItems } from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector', () => {
  let grid: FlowGrid;

  beforeEach(() => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);
  });

  it('should not reinitialize the connector', () => {
    const connector = grid.$connector;
    gridConnector.initLazy(grid);
    expect(grid.$connector).to.equal(connector);
  });

  it('should add root level items', async () => {
    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
    await nextFrame();

    expect(getBodyRowCount(grid)).to.equal(1);
    expect(getBodyCellText(grid, 0, 0)).to.equal('foo');
  });

  describe('empty grid', () => {
    it('should not have loading state when refreshing grid', async () => {
      setRootItems(grid.$connector, []);
      await nextFrame();

      // Force grid to refresh data
      grid.clearCache();
      await nextFrame();

      expect(grid.hasAttribute('loading')).to.be.false;
    });

    it('should not request items when refreshing grid', async () => {
      setRootItems(grid.$connector, []);
      await nextFrame();

      // Force grid to refresh data
      grid.clearCache();
      await nextFrame();

      expect(grid.$server.setRequestedRange.called).to.be.false;
    });
  });
});
