package com.aricontroller.exception;

// TODO: Better name?
public final class UnsupportedTypeException extends RuntimeException {

  public UnsupportedTypeException(Object object) {
    super("Type %s is not supported here.".formatted(object.getClass()));
  }

  public UnsupportedTypeException(String context, Object object) {
    super("%s - Type %s is not supported here.".formatted(context, object.getClass()));
  }
}
