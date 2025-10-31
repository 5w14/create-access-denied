package net.fw14.createAddons.accessDenied.mixin;

import com.simibubi.create.content.logistics.packagerLink.GlobalLogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import net.fw14.createAddons.accessDenied.extensions.LogisticNetworkExtensions;
import net.fw14.createAddons.accessDenied.extensions.LogisticsManagerExtensions;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(GlobalLogisticsManager.class)
public class LogisticsMixin implements LogisticsManagerExtensions {
    @Shadow(remap = false) public Map<UUID, LogisticsNetwork> logisticsNetworks;

    @Inject(at = @At("RETURN"), method = "mayInteract", cancellable = true, remap = false)
    public void overrideMayInteract(UUID networkId, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue())
            return;

        if (((LogisticNetworkExtensions)logisticsNetworks.get(networkId))
                .accessDenied$getAllowedPlayers().contains(player.getUUID()))
            cir.setReturnValue(true);
    }

    @Override
    @Unique
    public Set<UUID> accessDenied$getAllowedPlayers(UUID networkId) {
        var network = this.logisticsNetworks.getOrDefault(networkId, null);
        if (network == null)
            throw new RuntimeException("Could not get network with ID " + networkId);
        return ((LogisticNetworkExtensions) network).accessDenied$getAllowedPlayers();
    }
}
