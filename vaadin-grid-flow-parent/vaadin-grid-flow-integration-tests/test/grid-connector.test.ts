import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { getBodyRowCount, getCellText } from './helpers.js';
import { init, gridConnector } from './shared.js';
import type { FlowGrid, GridConnector } from './shared.js';

describe('grid connector', () => {
  let grid: FlowGrid;
  let connector: GridConnector;

  beforeEach(() => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);
    connector = grid.$connector;
  });

  it('should not reinitialize the connector', () => {
    gridConnector.initLazy(grid);
    expect(grid.$connector).to.equal(connector);
  });

  it('should add root level items', async () => {
    connector.updateSize(1);
    connector.set(0, [{ key: '0', name: 'foo' }], null);
    connector.confirm(0);
    await nextFrame();

    expect(getBodyRowCount(grid)).to.equal(1);
    expect(getCellText(grid, 0, 0)).to.equal('foo');
  });
});
