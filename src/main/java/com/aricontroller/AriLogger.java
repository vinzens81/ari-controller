package com.aricontroller;

import com.aricontroller.control.domainobjects.shared.RoutingKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class AriLogger {

  private static final Marker INFRASTRUCTUREMARKER =
      MarkerFactory.getMarker(String.valueOf(LogMarker.INFRASTRUCTURE));
  private final Logger logger;

  public AriLogger(final Class<?> loggerName) {
    logger = LoggerFactory.getLogger(loggerName);
  }

  public void trace(final RoutingKey routingKey, final String message, Object... objects) {
    logger.trace(getRoutingKeyMarker(routingKey), message, objects);
  }

  public void debug(final RoutingKey routingKey, final String message, Object... objects) {
    logger.debug(getRoutingKeyMarker(routingKey), message, objects);
  }

  public void info(final RoutingKey routingKey, final String message, Object... objects) {
    logger.info(getRoutingKeyMarker(routingKey), message, objects);
  }

  public void warn(final RoutingKey routingKey, final String message, Object... objects) {
    logger.warn(getRoutingKeyMarker(routingKey), message, objects);
  }

  public void error(final RoutingKey routingKey, final String message, Object... objects) {
    logger.error(getRoutingKeyMarker(routingKey), message, objects);
  }

  @SuppressWarnings("unused")
  public void trace(final LogMarker marker, final String message, Object... objects) {
    logger.trace(INFRASTRUCTUREMARKER, message, objects);
  }

  @SuppressWarnings("unused")
  public void debug(final LogMarker marker, final String message, Object... objects) {
    logger.debug(INFRASTRUCTUREMARKER, message, objects);
  }

  @SuppressWarnings("unused")
  public void info(final LogMarker marker, final String message, Object... objects) {
    logger.info(INFRASTRUCTUREMARKER, message, objects);
  }

  @SuppressWarnings("unused")
  public void warn(final LogMarker marker, final String message, Object... objects) {
    logger.warn(INFRASTRUCTUREMARKER, message, objects);
  }

  @SuppressWarnings("unused")
  public void error(final LogMarker marker, final String message, Object... objects) {
    logger.error(INFRASTRUCTUREMARKER, message, objects);
  }

  private static Marker getRoutingKeyMarker(final RoutingKey routingKey) {
    Marker marker;
    if (routingKey != null) {
      marker = MarkerFactory.getMarker(routingKey.value());
    } else {
      marker = MarkerFactory.getMarker("NO_ROUTING_KEY_SET");
    }
    return marker;
  }

  public enum LogMarker {
    INFRASTRUCTURE
  }
}
