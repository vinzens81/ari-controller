package com.aricontroller.control.controller.akkaagnosticlogic.shared;

import static java.util.Collections.emptySet;

import java.util.Set;
import org.apache.commons.collections4.SetUtils;

public record Effects(Set<Effect> effects, boolean stopSession) {
  public static Effects of(final Effect effect) {
    return new Effects(Set.of(effect), false);
  }

  public static Effects empty() {
    return new Effects(emptySet(), false);
  }

  public Effects withEffect(final Effect effect) {
    return withEffects(Set.of(effect));
  }

  public Effects withStopSession() {
    return new Effects(effects, true);
  }

  public Effects withEffects(final Set<Effect> newEffects) {
    return new Effects(SetUtils.union(effects(), newEffects), stopSession);
  }
}
