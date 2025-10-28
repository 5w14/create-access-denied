package net.fw14.createAddons.accessDenied.mixin;

import com.simibubi.create.content.logistics.packagerLink.GlobalLogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(GlobalLogisticsManager.class)
public class LogisticsMixin {
    @Shadow public Map<UUID, LogisticsNetwork> logisticsNetworks;

    @Inject(at = @At("HEAD"), method = "mayInteract", cancellable = true, remap = false)
    public void overrideMayInteract(UUID networkId, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue())
            return;

        if (((LogisticNetworkExtensions)logisticsNetworks.get(networkId))
                .accessDenied$getAllowedPlayers().contains(player.getUUID()))
            cir.setReturnValue(true);
    }
}
