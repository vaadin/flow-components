package com.vaadin.addon.frp;

import java.util.Objects;

/**
 *
 */
public class Pair<T1, T2> {
  private T1 t1;
  private T2 t2;

  public Pair(final T1 t1, final T2 t2) {
    this.t1 = t1;
    this.t2 = t2;
  }

  public T1 getT1() {
    return t1;
  }

  public T2 getT2() {
    return t2;
  }

  @Override
  public String toString() {
    return "Pair{" +
        "t1=" + t1 +
        ", t2=" + t2 +
        '}';
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (! (o instanceof Pair)) return false;
    final Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(t1, pair.t1) &&
        Objects.equals(t2, pair.t2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(t1, t2);
  }

  public static <T1, T2> Pair<T1, T2> next(T1 a, T2 b) {
    return new Pair<>(a, b);
  }
}
