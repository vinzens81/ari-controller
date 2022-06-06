package com.aricontroller.boundary.messaging.inbox;

import com.aricontroller.boundary.messaging.ari.inbound.IncomingAriMessageJsonEnvelope;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;

@JsonDeserialize(using = InboxMessageEnvelope.InboxMessageEnvelopeDeserializer.class)
public interface InboxMessageEnvelope {

  class InboxMessageEnvelopeDeserializer extends JsonDeserializer<InboxMessageEnvelope> {

    @Override
    public InboxMessageEnvelope deserialize(final JsonParser jp, final DeserializationContext ctx)
        throws IOException {
      final ObjectCodec codec = jp.getCodec();
      final JsonNode node = codec.readTree(jp);

      if (node.has("callContext") && node.get("callContext").isTextual()) {
        return codec.treeToValue(node, IncomingAriMessageJsonEnvelope.class);
      }

      throw MismatchedInputException.from(
          jp,
          InboxMessageEnvelope.class,
          "None of the subtypes of %s matches input structure"
              .formatted(InboxMessageEnvelope.class.getSimpleName()));
    }
  }
}
