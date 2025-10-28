package net.fw14.createAddons.accessDenied.networking;

import com.simibubi.create.Create;
import net.fw14.createAddons.accessDenied.extensions.LogisticNetworkExtensions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class C2SModifyAllowedPlayersPacket {
    private final UUID networkId;
    private final Mode mode;
    private final @Nullable UUID playerUUID;

    public C2SModifyAllowedPlayersPacket(UUID networkId, Mode mode, @Nullable UUID playerUUID) {
        if (mode.requiresId && playerUUID == null)
            throw new RuntimeException("Mode " + mode + " requries player UUID!");

        this.networkId = networkId;
        this.mode = mode;
        this.playerUUID = playerUUID;
    }

    public static C2SModifyAllowedPlayersPacket read(FriendlyByteBuf buffer) {
        var networkId = buffer.readUUID();

        var oMode = Mode.fromId(buffer.readByte());
        if (oMode.isEmpty())
            throw new RuntimeException("Illegal Mode supplied!");

        var mode = oMode.get();
        UUID playerId = null;

        if (mode.requiresId) {
            playerId = buffer.readUUID();
        }

        return new C2SModifyAllowedPlayersPacket(networkId, mode, playerId);
    }

    public static C2SModifyAllowedPlayersPacket add(UUID networkId, UUID playerUUID) {
        return new C2SModifyAllowedPlayersPacket(networkId, Mode.ADD, playerUUID);
    }
    public static C2SModifyAllowedPlayersPacket remove(UUID networkId, UUID playerUUID) {
        return new C2SModifyAllowedPlayersPacket(networkId, Mode.REMOVE, playerUUID);
    }
    public static C2SModifyAllowedPlayersPacket clear(UUID networkId) {
        return new C2SModifyAllowedPlayersPacket(networkId, Mode.CLEAR, null);
    }

    public static void write(C2SModifyAllowedPlayersPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.networkId);
        buffer.writeByte(packet.mode.type);
        if (packet.mode.requiresId && packet.playerUUID != null)
            buffer.writeUUID(packet.playerUUID);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (!ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER))
            return;

        ctx.get().enqueueWork(() -> {
            if (!Create.LOGISTICS.mayAdministrate(networkId, ctx.get().getSender())) {
                return;
            }

            var network = (LogisticNetworkExtensions) Create.LOGISTICS.logisticsNetworks.get(this.networkId);

            switch (mode) {
                case ADD -> network.accessDenied$addAllowedPlayer(this.playerUUID);
                case REMOVE -> network.accessDenied$removeAllowedPlayer(this.playerUUID);
                case CLEAR -> network.accessDenied$clearAllowedPlayers();
            }
        });

        ctx.get().setPacketHandled(true);
    }

    public enum Mode {
        ADD((byte)1, true),
        REMOVE((byte)2, true),
        CLEAR((byte)3, false)

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
    }
}
