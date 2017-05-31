package com.vaadin.addon.frp;

import java.util.function.Function;

/**
 *
 */
@FunctionalInterface
public interface CheckedExecutor extends Function<Void, Result<Void>> {

  default Result<Void> execute() {
    return apply(null);
  }


  @Override
  default Result<Void> apply(Void t) {
    try {
      applyWithException();
      return Result.success(null);
    } catch (Exception e) {
      return Result.failure(e.getMessage());
    }
  }

  void applyWithException() throws Exception;

}