import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { init, setRootItems, getBodyCell, getBodyCellContent, getHeaderCellContent } from './shared.js';
import type { FlowGrid } from './shared.js';
import sinon from 'sinon';
import { GridColumn } from '@vaadin/grid';

describe('grid connector - item click', () => {
  let grid: FlowGrid;
  const columnFlowId = 'col0';
  let column: GridColumn & { _flowId: string };

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    column = grid.querySelector<GridColumn & { _flowId: string }>('[path="name"]')!;
    column._flowId = columnFlowId;

    setRootItems(grid.$connector, [
      { key: '0', name: 'foo' },
      { key: '1', name: 'bar' }
    ]);
    await nextFrame();
  });

  it('should dispatch an item-click event', async () => {
    const spy = sinon.spy();
    grid.addEventListener('item-click' as any, spy);
    getBodyCell(grid, 0, 0)?.click();
    expect(spy.calledOnce).to.be.true;
    expect(spy.firstCall.args[0].detail).to.include({ itemKey: '0', internalColumnId: columnFlowId });
  });

  it('should dispatch an item-double-click event', async () => {
    const spy = sinon.spy();
    grid.addEventListener('item-double-click' as any, spy);
    getBodyCell(grid, 0, 0)?.dispatchEvent(new MouseEvent('dblclick', { bubbles: true, composed: true }));
    expect(spy.calledOnce).to.be.true;
  });

  it('should not dispatch an item-click event if the click event is cancelled', async () => {
    const spy = sinon.spy();
    const cell = getBodyCell(grid, 0, 0);
    cell?.addEventListener('click', (e) => e.preventDefault());
    grid.addEventListener('item-click' as any, spy);
    cell?.click();
    expect(spy.called).to.be.false;
  });

  it('should not dispatch an item-click event on focusable click', async () => {
    column.renderer = (root: HTMLElement) => {
      root.innerHTML = '<input>';
    };

    const spy = sinon.spy();
    const content = getBodyCellContent(grid, 0, 0);
    const focusable = content?.querySelector('input');

    grid.addEventListener('item-click' as any, spy);
    focusable?.click();
    expect(spy.called).to.be.false;
  });

  it('should not dispatch an item-click event on label click', async () => {
    column.renderer = (root: HTMLElement) => {
      root.innerHTML = '<label>label</label>';
    };

    const spy = sinon.spy();
    const content = getBodyCellContent(grid, 0, 0);
    const focusable = content?.querySelector('label');

    grid.addEventListener('item-click' as any, spy);
    focusable?.click();
    expect(spy.called).to.be.false;
  });

  it('should not dispatch an item-click event on header click', async () => {
    column.headerRenderer = (root: HTMLElement) => {
      root.innerHTML = '<div>Header</div>';
    };

    const spy = sinon.spy();
    grid.addEventListener('item-click' as any, spy);
    getHeaderCellContent(column)?.click();
    expect(spy.called).to.be.false;
  });

  it('should not dispatch an item-click event on details click', async () => {
    grid.rowDetailsRenderer = (root) => {
      root.innerHTML = '<div class="details">Details</div>';
    };
    setRootItems(grid.$connector, [
      { key: '0', name: 'foo', detailsOpened: true },
      { key: '1', name: 'bar' }
    ]);

    const spy = sinon.spy();
    grid.addEventListener('item-click' as any, spy);
    const details = grid.querySelector('.details') as HTMLElement;
    details.click();
    expect(spy.called).to.be.false;
  });
});
