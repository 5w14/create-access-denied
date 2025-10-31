package net.fw14.createAddons.accessDenied.networking;

import com.simibubi.create.Create;
import io.netty.buffer.ByteBuf;
import net.fw14.createAddons.accessDenied.AccessDenied;
import net.fw14.createAddons.accessDenied.extensions.LogisticsManagerExtensions;
import net.fw14.createAddons.accessDenied.screen.AccessControlScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A packet that is sent to the owner of a logistics network
 * to display allowed players in GUI
 * */
public record S2CAllowedPlayersSyncPacket(UUID networkUUID, Set<UUID> allowedPlayers) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CAllowedPlayersSyncPacket> TYPE = new Type<>(AccessDenied.resLoc("allowed_players_sync"));

    public static final StreamCodec<ByteBuf, S2CAllowedPlayersSyncPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, S2CAllowedPlayersSyncPacket::networkUUID,
            ByteBufCodecs.<ByteBuf, UUID>list().apply(UUIDUtil.STREAM_CODEC)
                    .map(Set::copyOf, List::copyOf), S2CAllowedPlayersSyncPacket::allowedPlayers,
            S2CAllowedPlayersSyncPacket::new
    );

    public static S2CAllowedPlayersSyncPacket fromNetworkId(UUID network) {
        return new S2CAllowedPlayersSyncPacket(network, ((LogisticsManagerExtensions) Create.LOGISTICS).accessDenied$getAllowedPlayers(network));
    }

    public static void handle(final S2CAllowedPlayersSyncPacket data, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            AccessDenied.AllowedPlayersClientState.put(data.networkUUID, data.allowedPlayers);
            data.allowedPlayers.forEach((uuid) -> AccessControlScreen.ProfileCache.preCache(uuid, Minecraft.getInstance()));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}