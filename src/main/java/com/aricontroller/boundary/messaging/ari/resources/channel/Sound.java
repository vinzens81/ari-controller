package com.aricontroller.boundary.messaging.ari.resources.channel;

public final record Sound(String name, String folder) {
  public static Sound of(com.aricontroller.control.domainobjects.ari.Sound modelSound) {
    return new Sound(modelSound.name(), modelSound.folder());
  }
}
