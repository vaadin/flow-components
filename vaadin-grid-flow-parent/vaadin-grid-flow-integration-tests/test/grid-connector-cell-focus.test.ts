import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { sendKeys } from '@web/test-runner-commands';
import { init, setRootItems, getBodyCell } from './shared.js';
import type { FlowGrid } from './shared.js';
import { GridColumn } from '@vaadin/grid';
import sinon from 'sinon';

describe('grid connector - cell focus', () => {
  let grid: FlowGrid;
  const columnFlowId = 'col0';

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    const keyColumn = grid.querySelector<GridColumn & { _flowId: string }>('[path="name"]')!;
    keyColumn._flowId = columnFlowId;

    init(grid);
    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
    await nextFrame();
  });

  it('should dispatch a grid-cell-focus event on cell focus', async () => {
    const spy = sinon.spy();
    (grid as HTMLElement).addEventListener('grid-cell-focus', spy);

    getBodyCell(grid, 0, 0)?.focus();

    expect(spy.calledOnce).to.be.true;
    expect(spy.firstCall.args[0].detail).to.eql({ itemKey: '0', internalColumnId: 'col0', section: 'body' });
  });

  it('should not dispatch a grid-cell-focus event on details cell focus', async () => {
    grid.rowDetailsRenderer = (root) => {
      root.innerHTML = '<div class="details">Details</div>';
    };

    setRootItems(grid.$connector, [{ key: '0', name: 'foo', detailsOpened: true }]);

    getBodyCell(grid, 0, 0)?.focus();
    await sendKeys({ press: 'ArrowUp' });

    const spy = sinon.spy();
    (grid as HTMLElement).addEventListener('grid-cell-focus', spy);
    await sendKeys({ press: 'ArrowDown' });
    expect(spy.called).to.be.false;
  });
});
