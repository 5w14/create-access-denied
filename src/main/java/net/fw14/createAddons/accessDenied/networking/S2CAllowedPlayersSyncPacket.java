package net.fw14.createAddons.accessDenied.networking;

import com.simibubi.create.Create;
import net.fw14.createAddons.accessDenied.AccessDenied;
import net.fw14.createAddons.accessDenied.extensions.LogisticsManagerExtensions;
import net.fw14.createAddons.accessDenied.screen.AccessControlScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.network.NetworkDirection;
import net.neoforged.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * A packet that is sent to the owner of a logistics network
 * to display allowed players in GUI
 * */
public class S2CAllowedPlayersSyncPacket {
    private final UUID networkUUID;
    private final Set<UUID> allowedPlayers;

    public S2CAllowedPlayersSyncPacket(UUID network, Set<UUID> set) {
        this.networkUUID = network;
        this.allowedPlayers = set;
    }

    public static S2CAllowedPlayersSyncPacket fromNetworkId(UUID network) {
        return new S2CAllowedPlayersSyncPacket(network, ((LogisticsManagerExtensions) Create.LOGISTICS).accessDenied$getAllowedPlayers(network));
    }

    public static S2CAllowedPlayersSyncPacket read(FriendlyByteBuf buffer) {
        var networkUUID = buffer.readUUID();
        var count = buffer.readByte();
        var set = new HashSet<UUID>();
        for (int i = 0; i < count; i++) {
            var uuid = buffer.readUUID();
            set.add(uuid);
        }
        return new S2CAllowedPlayersSyncPacket(networkUUID, set);
    }

    public static void write(S2CAllowedPlayersSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.networkUUID);
        var count = packet.allowedPlayers.size();
        buffer.writeByte(count);
        packet.allowedPlayers.forEach(buffer::writeUUID);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (!ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_CLIENT))
            return;

        ctx.get().enqueueWork(() -> {
            AccessDenied.AllowedPlayersClientState.put(networkUUID, allowedPlayers);
            allowedPlayers.forEach((uuid) -> AccessControlScreen.ProfileCache.preCache(uuid, Minecraft.getInstance()));
        });

        ctx.get().setPacketHandled(true);
    }
}