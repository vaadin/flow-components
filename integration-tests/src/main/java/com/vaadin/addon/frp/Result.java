package com.vaadin.addon.frp;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
public interface Result<T> {
  void bind(Consumer<T> success, Consumer<String> failure);

  static <T> Result<T> failure(String errorMessage) {
    return new Failure<>(errorMessage);
  }

  static <T> Result<T> success(T value) {
    return new Success<>(value);
  }

  static <T> Result<T> ofNullable(T value) {
    return ofNullable(value, "Object was null");
  }

  static <T> Result<T> ofNullable(T value, String failedMessage) {
    return (Objects.nonNull(value))
        ? Result.success(value)
        : Result.failure(failedMessage);
  }

  T get();

  T getOrElse(Supplier<T> supplier);

  Boolean isPresent();

  Boolean isAbsent();

  void ifPresent(Consumer<T> consumer);

  default Optional<T> toOptional() {
    return Optional.ofNullable(get());
  }

  static <T> Result<T> fromOptional(Optional<T> optional) {
    Objects.requireNonNull(optional);
    return ofNullable(optional.get(), "Optional hold a null value");
  }

  default <V, R> Result<R> thenCombine(V value, BiFunction<T, V, Result<R>> func) {
    return func.apply(get(), value);
  }

  default <V, R> CompletableFuture<Result<R>> thenCombineAsync(V value, BiFunction<T, V, Result<R>> func) {
    return CompletableFuture.supplyAsync(() -> func.apply(get(), value));
  }


  abstract class AbstractResult<T> implements Result<T> {
    protected final T value;

    public AbstractResult(T value) {
      this.value = value;
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
      Objects.requireNonNull(consumer);
      if (value != null) consumer.accept(value);
    }

    public Boolean isPresent() {
      return (value != null) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Boolean isAbsent() {
      return (value == null) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public T get() {
      return value;
    }

    @Override
    public T getOrElse(Supplier<T> supplier) {
      Objects.requireNonNull(supplier);
      return (value != null) ? value : supplier.get();
    }

  }

  class Success<T> extends AbstractResult<T> {

    public Success(T value) {
      super(value);
    }

    @Override
    public void bind(final Consumer<T> success, final Consumer<String> failure) {
      success.accept(value);
    }

  }

  class Failure<T> extends AbstractResult<T> {

    private final String errorMessage;

    public Failure(final String errorMessage) {
      super(null);
      this.errorMessage = errorMessage;
    }

    @Override
    public void bind(final Consumer<T> success, final Consumer<String> failure) {
      failure.accept(errorMessage);
    }
  }
}
