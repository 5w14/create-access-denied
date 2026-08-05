package net.fw14.createAddons.accessDenied.mixin;

import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import net.fw14.createAddons.accessDenied.config.Config;
import net.fw14.createAddons.accessDenied.extensions.LogisticNetworkExtensions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(LogisticsNetwork.class)
public class LogisticNetworkMixin implements LogisticNetworkExtensions {
    @Unique
    public Set<UUID> accessDenied$allowedPlayers = new HashSet<>();

    @Inject(at = @At("RETURN"), method = "read", remap = false)
    private static void readAllowedPlayers(CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<LogisticsNetwork> cir) {
        var network = cir.getReturnValue();

        if (!tag.contains("AllowedPlayers"))
            return;

        ListTag list = (ListTag) tag.get("AllowedPlayers");
        Set<UUID> players = new HashSet<>();
        players.addAll(list.stream().map(NbtUtils::loadUUID).toList());

        ((LogisticNetworkMixin)(Object)network).accessDenied$allowedPlayers = players;
    }

    @ModifyVariable(at = @At("STORE"), method = "write", remap = false)
    public CompoundTag writeAllowedPlayers(CompoundTag tag) {
        var list = new ListTag();
        var uuids = accessDenied$allowedPlayers.stream().map(NbtUtils::createUUID).toList();
        list.addAll(uuids);
        tag.put("AllowedPlayers", list);
        return tag;
    }

    @Unique
    @Override
    public Set<UUID> accessDenied$getAllowedPlayers() {
        return this.accessDenied$allowedPlayers;
    }

    @Unique
    @Override
    public void accessDenied$addAllowedPlayer(UUID uuid) {
        if (this.accessDenied$allowedPlayers.size() >= Config.PLAYER_LIMIT.get())
            return;

        this.accessDenied$allowedPlayers.add(uuid);
    }

    @Unique
    @Override
    public void accessDenied$removeAllowedPlayer(UUID uuid) {
        this.accessDenied$allowedPlayers.remove(uuid);
    }

    @Unique
    @Override
    public void accessDenied$clearAllowedPlayers() {
        this.accessDenied$allowedPlayers.clear();
    }
}
