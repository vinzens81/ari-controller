package com.aricontroller.boundary.messaging.ari;

public final class Constants {
  public static final String APP_NAME = "app";
  public static final String CONTEXT_NAME = "stasis-app";
  public static final String CHANNEL_VARIABLE_CALL_CONTEXT = "CALL_CONTEXT";
  public static final String INTERNAL_CHANNEL_TARGET = "internalChannel";
  public static final String CHANNEL_VARIABLE_TARGET = "TARGET";

  private Constants() {
    throw new IllegalStateException("Utility class");
  }
}
