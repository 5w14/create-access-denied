package net.fw14.createAddons.accessDenied.mixin;

import java.util.Set;
import java.util.UUID;

public interface LogisticNetworkExtensions {
    Set<UUID> accessDenied$getAllowedPlayers();
    void accessDenied$addAllowedPlayer(UUID uuid);
    void accessDenied$removeAllowedPlayer(UUID uuid);
    void accessDenied$clearAllowedPlayers();
}
