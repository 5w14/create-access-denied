package net.fw14.createAddons.accessDenied;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.logging.LogUtils;
import net.fw14.createAddons.accessDenied.mixin.MinecraftAccessor;
import net.fw14.createAddons.accessDenied.networking.C2SModifyAllowedPlayersPacket;
import net.fw14.createAddons.accessDenied.networking.S2CAllowedPlayersSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AccessDenied.MODID)
@Mod.EventBusSubscriber
public class AccessDenied {
    public static String PROTOCOL_VERSION = "1";
    public static final String MODID = "create_access_denied";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int PLAYER_LIMIT = 18;

    public static final Map<UUID, Set<UUID>> AllowedPlayersClientState = new HashMap<>();

    public static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MODID, "networking"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public AccessDenied() {
        setup();
        setupNetworking();
    }


    private static void setupNetworking() {
        int msgType = 0;
        NETWORK_CHANNEL.registerMessage(msgType++, S2CAllowedPlayersSyncPacket.class,
                S2CAllowedPlayersSyncPacket::write, S2CAllowedPlayersSyncPacket::read, S2CAllowedPlayersSyncPacket::handle);
        NETWORK_CHANNEL.registerMessage(msgType++, C2SModifyAllowedPlayersPacket.class,
                C2SModifyAllowedPlayersPacket::write, C2SModifyAllowedPlayersPacket::read, C2SModifyAllowedPlayersPacket::handle);
    }

    static GameProfileRepository gameProfileRepository;

    public static CompletableFuture<GameProfile> fetchProfileByUsername(String username) {
        var future = new CompletableFuture<GameProfile>();
        gameProfileRepository.findProfilesByNames(ArrayUtils.toArray(username), Agent.MINECRAFT, new ProfileLookupCallback() {
            @Override
            public void onProfileLookupSucceeded(GameProfile profile) {
                future.complete(profile);
            }

            @Override
            public void onProfileLookupFailed(GameProfile profile, Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    static void setup() {
        var service = ((MinecraftAccessor) Minecraft.getInstance()).getAuthenticationService();
        gameProfileRepository = service.createProfileRepository();
    }
}
