import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { init, setRootItems, getBodyCell } from './shared.js';
import type { FlowGrid } from './shared.js';
import { GridColumn } from '@vaadin/grid';

describe('grid connector - generators', () => {
  let grid: FlowGrid;
  const columnFlowId = 'col0';

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="key"></vaadin-grid-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);
    
    const keyColumn = grid.querySelector<GridColumn & { _flowId: string }>('[path="key"]')!;
    keyColumn._flowId = columnFlowId;

    init(grid);
    await nextFrame();
  });

  it('should add row style to all row cells as class name', async () => {
    setRootItems(grid.$connector, [{ key: '0', name: 'foo', style: { row: 'foo' } }]);

    expect(getBodyCell(grid, 0, 0)?.classList.contains('foo')).to.be.true;
    expect(getBodyCell(grid, 0, 1)?.classList.contains('foo')).to.be.true;
  });

  it('should add column style to column cells as class name', async () => {
    setRootItems(grid.$connector, [{ key: '0', name: 'foo', style: { [columnFlowId]: 'foo' } }]);

    expect(getBodyCell(grid, 0, 0)?.classList.contains('foo')).to.be.true;
    expect(getBodyCell(grid, 0, 1)?.classList.contains('foo')).to.be.false;
  });

  it('should add row style to all row cells as part', async () => {
    setRootItems(grid.$connector, [{ key: '0', name: 'foo', part: { row: 'foo' } }]);

    expect(getBodyCell(grid, 0, 0)?.part.contains('foo')).to.be.true;
    expect(getBodyCell(grid, 0, 1)?.part.contains('foo')).to.be.true;
  });

  it('should add column style to column cells as part', async () => {
    setRootItems(grid.$connector, [{ key: '0', name: 'foo', part: { [columnFlowId]: 'foo' } }]);

    expect(getBodyCell(grid, 0, 0)?.part.contains('foo')).to.be.true;
    expect(getBodyCell(grid, 0, 1)?.part.contains('foo')).to.be.false;
  });
});
