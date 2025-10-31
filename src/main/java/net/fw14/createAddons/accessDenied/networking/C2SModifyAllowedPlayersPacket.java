package net.fw14.createAddons.accessDenied.networking;

import com.simibubi.create.Create;
import io.netty.buffer.ByteBuf;
import net.fw14.createAddons.accessDenied.AccessDenied;
import net.fw14.createAddons.accessDenied.extensions.LogisticNetworkExtensions;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public record C2SModifyAllowedPlayersPacket(UUID networkId, Mode mode, @Nullable UUID uuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SModifyAllowedPlayersPacket> TYPE = new CustomPacketPayload.Type<>(AccessDenied.resLoc("allowed_players_sync"));

    public static final StreamCodec<ByteBuf, C2SModifyAllowedPlayersPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, C2SModifyAllowedPlayersPacket::networkId,
            ByteBufCodecs.BYTE.map(Mode::fromIdF, Mode::type), C2SModifyAllowedPlayersPacket::mode,
            UUIDUtil.STREAM_CODEC, C2SModifyAllowedPlayersPacket::networkId,
            C2SModifyAllowedPlayersPacket::new
    );

    public static C2SModifyAllowedPlayersPacket add(UUID networkId, UUID playerUUID) {
        return new C2SModifyAllowedPlayersPacket(networkId, Mode.ADD, playerUUID);
    }
    public static C2SModifyAllowedPlayersPacket remove(UUID networkId, UUID playerUUID) {
        return new C2SModifyAllowedPlayersPacket(networkId, Mode.REMOVE, playerUUID);
    }
    public static C2SModifyAllowedPlayersPacket clear(UUID networkId) {
        return new C2SModifyAllowedPlayersPacket(networkId, Mode.CLEAR, new UUID(0, 0));
    }

    public static void handle(final C2SModifyAllowedPlayersPacket packet, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!Create.LOGISTICS.mayAdministrate(packet.networkId, ctx.player())) {
                return;
            }

            var network = (LogisticNetworkExtensions) Create.LOGISTICS.logisticsNetworks.get(packet.networkId);

            switch (packet.mode) {
                case ADD -> network.accessDenied$addAllowedPlayer(packet.uuid());
                case REMOVE -> network.accessDenied$removeAllowedPlayer(packet.uuid());
                case CLEAR -> network.accessDenied$clearAllowedPlayers();
                case NOOP -> {
                    return;
                }
            }

            Create.LOGISTICS.markDirty();
            PacketDistributor.sendToPlayer((ServerPlayer) ctx.player(), S2CAllowedPlayersSyncPacket.fromNetworkId(packet.networkId));
        });
    }
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public enum Mode {
        ADD((byte)1, true),
        REMOVE((byte)2, true),
        CLEAR((byte)3, false),
        NOOP((byte)0, false),

        ;

        private final byte type;
        private final boolean requiresId;

        Mode(byte type, boolean requiresId) {
            this.type = type;
            this.requiresId = requiresId;
        }

        public byte type() {
            return this.type;
        }

        public boolean requiresId() {
            return this.requiresId;
        }

        public static Optional<Mode> fromId(byte id) {
            return Arrays.stream(values()).filter(v -> v.type == id).findFirst();
        }
        public static Mode fromIdF(byte id) {
            return fromId(id).orElseGet(()->Mode.NOOP);
        }
    }
}
