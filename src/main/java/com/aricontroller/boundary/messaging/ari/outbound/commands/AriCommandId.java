package com.aricontroller.boundary.messaging.ari.outbound.commands;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public final record AriCommandId(String value) implements Serializable {
  public static AriCommandId generate() {
    return new AriCommandId(UUID.randomUUID().toString());
  }

  public static Optional<AriCommandId> fromString(final String value) {
    return Optional.ofNullable(value).filter(StringUtils::isNotBlank).map(AriCommandId::new);
  }
}
