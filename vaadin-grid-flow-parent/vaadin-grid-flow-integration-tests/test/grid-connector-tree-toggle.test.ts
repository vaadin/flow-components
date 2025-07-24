import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { sendKeys } from '@web/test-runner-commands';
import {
  init,
  setRootItems,
  getBodyCellContent,
  expandItems,
} from './shared.js';
import type { FlowGrid, Item } from './shared.js';
import type { GridTreeToggle } from '@vaadin/grid/vaadin-grid-tree-toggle.js';

describe('grid connector - tree toggle', () => {
  let grid: FlowGrid;

  function getTreeToggle(rowIndex: number): GridTreeToggle | null | undefined {
    return getBodyCellContent(grid, rowIndex, 0)?.querySelector('vaadin-grid-tree-toggle');
  }

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-tree-column path="name"></vaadin-grid-tree-column>
      </vaadin-grid>
    `);

    init(grid);

    const rootItems: Item[] = [
      { key: '0', name: 'foo', children: true },
      { key: '1', name: 'bar', children: true },
    ]

    setRootItems(grid.$connector, rootItems);
    expandItems(grid.$connector, [rootItems[1]]);

    await nextFrame();
  });

  it('should make a server request when expanding item with click', () => {
    getTreeToggle(0)?.click();
    expect(grid.$server.updateExpandedState).to.be.calledOnce;
    expect(grid.$server.updateExpandedState.firstCall.args[0]).to.equal('0');
    expect(grid.$server.updateExpandedState.firstCall.args[1]).to.be.true;
  });

  it('should make a server request when expanding item with keyboard', async () => {
    await sendKeys({ press: 'Tab' });
    await sendKeys({ press: 'Tab' });
    await sendKeys({ press: 'Space' });

    expect(grid.$server.updateExpandedState).to.be.calledOnce;
    expect(grid.$server.updateExpandedState.firstCall.args[0]).to.equal('0');
    expect(grid.$server.updateExpandedState.firstCall.args[1]).to.be.true;
  });

  it('should make a server request when collapsing item with click', () => {
    getTreeToggle(1)?.click();
    expect(grid.$server.updateExpandedState).to.be.calledOnce;
    expect(grid.$server.updateExpandedState.firstCall.args[0]).to.equal('1');
    expect(grid.$server.updateExpandedState.firstCall.args[1]).to.be.false;
  });

  it('should make a server request when collapsing item with keyboard', async () => {
    await sendKeys({ press: 'Tab' });
    await sendKeys({ press: 'Tab' });
    await sendKeys({ press: 'Space' });

    expect(grid.$server.updateExpandedState).to.be.calledOnce;
    expect(grid.$server.updateExpandedState.firstCall.args[0]).to.equal('0');
    expect(grid.$server.updateExpandedState.firstCall.args[1]).to.be.true;
  });
});
