package net.fw14.createAddons.accessDenied.mixin;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import net.fw14.createAddons.accessDenied.networking.S2CAllowedPlayersSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StockTickerInteractionHandler.class)
public class StockTickerInteractionHandlerMixin {
    @Inject(at= @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;"),
            method = "interactWithLogisticsManagerAt", remap = false, cancellable = true)
    private static void sendExtraPacket(Player player, Level level, BlockPos targetPos, CallbackInfoReturnable<Boolean> cir) {
        var ticker = (StockTickerBlockEntity) level.getBlockEntity(targetPos);
        assert ticker != null;
        if (Create.LOGISTICS.mayAdministrate(ticker.behaviour.freqId, player)) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, S2CAllowedPlayersSyncPacket.fromNetworkId(ticker.behaviour.freqId));
        }
    }
}
