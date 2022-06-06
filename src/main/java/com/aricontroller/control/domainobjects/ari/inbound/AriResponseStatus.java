package com.aricontroller.control.domainobjects.ari.inbound;

public enum AriResponseStatus {
  // TODO Make this abstract from HTTP more.
  SUCCESS,
  BAD_REQUEST,
  FAILURE,
  RESOURCE_NOT_FOUND,
  UNPROCESSABLE_ENTITY,
  CONFLICT
}
