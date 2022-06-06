package com.aricontroller.boundary.messaging.ari.resources.channel;

public final record Language(String value) {
  public static Language of(com.aricontroller.control.domainobjects.ari.Language modelLanguage) {
    return new Language(modelLanguage.value());
  }
}
