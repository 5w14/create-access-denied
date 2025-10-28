package net.fw14.createAddons.accessDenied.extensions;

import java.util.Set;
import java.util.UUID;

public interface LogisticsManagerExtensions {
    Set<UUID> accessDenied$getAllowedPlayers(UUID networkId);
}
