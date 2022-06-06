package com.aricontroller.control.domainobjects.ari.outbound;

import com.aricontroller.control.domainobjects.ari.Language;
import com.aricontroller.control.domainobjects.ari.Sound;
import com.aricontroller.control.domainobjects.ari.inbound.AriCommandId;
import com.aricontroller.control.domainobjects.shared.ChannelId;
import com.aricontroller.control.domainobjects.shared.PlaybackId;

public record PlaySoundAriCommand(
    AriCommandId ariCommandId,
    ChannelId channelId,
    PlaybackId playbackId,
    Language language,
    Sound sound)
    implements AriCommand {}
