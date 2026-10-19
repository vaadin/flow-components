/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { AxisSizes } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/AxisSizes.js';

function sizes(list: number[], beyond: number): AxisSizes {
  return new AxisSizes(list.length, (index) => list[index - 1], beyond);
}

describe('AxisSizes — where one starts', () => {
  it('starts the first at 0', () => {
    expect(sizes([10, 20], 5).start(1)).to.equal(0);
  });

  it('starts one where the ones before it end', () => {
    expect(sizes([10, 20, 30], 5).start(3)).to.equal(30);
  });

  it('counts the ones past the sized ones at the size beyond them', () => {
    // 10 + 20, then 5 and 5.
    expect(sizes([10, 20], 5).start(5)).to.equal(40);
  });

  it('starts a sheet with no sized ones at the size beyond them', () => {
    expect(sizes([], 15).start(4)).to.equal(45);
  });
});

describe('AxisSizes — the size of one', () => {
  it('gives a sized one its size, a hidden one none', () => {
    const axis = sizes([10, 0, 30], 5);
    expect([axis.size(1), axis.size(2), axis.size(3)]).to.eql([10, 0, 30]);
  });

  it('gives one past the sized ones the size beyond them', () => {
    expect(sizes([10], 7).size(2)).to.equal(7);
  });
});

describe('AxisSizes — which one a distance reaches', () => {
  it('stays put for no distance', () => {
    expect(sizes([10, 20], 5).reach(2, 0)).to.equal(2);
  });

  it('takes one that starts exactly at the distance', () => {
    expect(sizes([10, 20, 30], 5).reach(1, 30)).to.equal(3);
  });

  it('takes the next one when the distance ends inside one', () => {
    expect(sizes([10, 20, 30], 5).reach(1, 25)).to.equal(3);
  });

  it('measures from where it starts', () => {
    // From the second: 20 brings it to the third.
    expect(sizes([10, 20, 30], 5).reach(2, 20)).to.equal(3);
  });

  it('takes the first of the hidden ones a distance ends at', () => {
    // The second and third have no size: all three start at 10.
    expect(sizes([10, 0, 0, 10], 5).reach(1, 10)).to.equal(2);
  });

  it('goes on past the sized ones at the size beyond them', () => {
    // 10 + 20 = 30, then 5 each: 42 needs three more.
    expect(sizes([10, 20], 5).reach(1, 42)).to.equal(6);
  });

  it('goes on from one already past the sized ones', () => {
    expect(sizes([10, 20], 5).reach(4, 10)).to.equal(6);
  });

  it('stops at the first past the sized ones when those beyond have no size', () => {
    expect(sizes([10, 20], 0).reach(1, 1000)).to.equal(3);
  });

  it('agrees with a walk from where it starts, on many axes', () => {
    let x = 3;
    const next = () => {
      x = (x * 1103515245 + 12345) % 2147483648;
      return x / 2147483648;
    };
    const pick = (n: number) => Math.floor(next() * n);
    for (let i = 0; i < 3000; i++) {
      const list = Array.from({ length: pick(40) }, () => (next() < 0.2 ? 0 : 1 + pick(30)));
      const beyond = 1 + pick(20);
      const axis = sizes(list, beyond);
      const from = 1 + pick(list.length + 5);
      const distance = pick(800);
      let index = from;
      let position = 0;
      while (position < distance) {
        position += axis.size(index);
        index++;
      }
      expect(axis.reach(from, distance), JSON.stringify({ list, beyond, from, distance })).to.equal(index);
    }
  });
});

describe('AxisSizes — which one a point lies in', () => {
  it('stays put for a point at the start or before it', () => {
    expect(sizes([10, 20], 5).holding(2, 0)).to.equal(2);
    expect(sizes([10, 20], 5).holding(2, -15)).to.equal(2);
  });

  it('takes the one the point is inside', () => {
    expect(sizes([10, 20, 30], 5).holding(1, 25)).to.equal(2);
  });

  it('takes one that starts exactly at the point', () => {
    expect(sizes([10, 20, 30], 5).holding(1, 30)).to.equal(3);
  });

  it('passes over hidden ones, which hold no point', () => {
    // The second and third have no size: the point at 10 is in the fourth.
    expect(sizes([10, 0, 0, 10], 5).holding(1, 10)).to.equal(4);
    // A hidden one to start from holds nothing either.
    expect(sizes([0, 10], 5).holding(1, 0)).to.equal(2);
  });

  it('goes on past the sized ones at the size beyond them', () => {
    // 10 + 20 = 30, then 5 each: 42 is in the third of those, the fifth.
    expect(sizes([10, 20], 5).holding(1, 42)).to.equal(5);
  });

  it('agrees with a walk from where it starts, on many axes', () => {
    let x = 7;
    const next = () => {
      x = (x * 1103515245 + 12345) % 2147483648;
      return x / 2147483648;
    };
    const pick = (n: number) => Math.floor(next() * n);
    for (let i = 0; i < 3000; i++) {
      const list = Array.from({ length: pick(40) }, () => (next() < 0.2 ? 0 : 1 + pick(30)));
      const beyond = 1 + pick(20);
      const axis = sizes(list, beyond);
      const from = 1 + pick(list.length + 5);
      const distance = pick(800);
      // The one whose own pixels, from where it starts, take in the point.
      let index = from;
      let end = axis.size(index);
      while (end <= distance) {
        index++;
        end += axis.size(index);
      }
      expect(axis.holding(from, distance), JSON.stringify({ list, beyond, from, distance })).to.equal(index);
    }
  });
});
