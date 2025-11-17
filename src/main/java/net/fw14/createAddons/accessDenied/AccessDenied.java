package net.fw14.createAddons.accessDenied;

import com.mojang.logging.LogUtils;
import net.fw14.createAddons.accessDenied.networking.C2SModifyAllowedPlayersPacket;
import net.fw14.createAddons.accessDenied.networking.S2CAllowedPlayersSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AccessDenied.MODID)
@EventBusSubscriber(modid = AccessDenied.MODID)
public class AccessDenied {
    public static String PROTOCOL_VERSION = "1";
    public static final String MODID = "create_access_denied";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int PLAYER_LIMIT = 18;

    public static final Map<UUID, Set<UUID>> AllowedPlayersClientState = new HashMap<>();

    @SubscribeEvent
    public static void setupNetworking(RegisterPayloadHandlersEvent event) {
        try {
            var protocol = event.registrar(PROTOCOL_VERSION);
            protocol.playToClient(S2CAllowedPlayersSyncPacket.TYPE, S2CAllowedPlayersSyncPacket.CODEC, S2CAllowedPlayersSyncPacket::handle);
            protocol.playToServer(C2SModifyAllowedPlayersPacket.TYPE, C2SModifyAllowedPlayersPacket.CODEC, C2SModifyAllowedPlayersPacket::handle);
        } catch (Exception e) { }
    }


    public static ResourceLocation resLoc(String location) {
        return ResourceLocation.fromNamespaceAndPath(MODID, location);
    }
}
