package net.fw14.createAddons.accessDenied.client;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import net.fw14.createAddons.accessDenied.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class AccessDeniedClient {
    static GameProfileRepository gameProfileRepository;
    public static CompletableFuture<GameProfile> fetchProfileByUsername(String username) {
        if (gameProfileRepository == null) {
            var service = ((MinecraftAccessor) Minecraft.getInstance()).getAuthenticationService();
            gameProfileRepository = service.createProfileRepository();
        }

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
}
