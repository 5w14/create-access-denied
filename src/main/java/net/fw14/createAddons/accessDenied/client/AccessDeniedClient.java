package net.fw14.createAddons.accessDenied.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import net.fw14.createAddons.accessDenied.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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
        gameProfileRepository.findProfilesByNames(new String[]{username}, new ProfileLookupCallback() {
            @Override
            public void onProfileLookupSucceeded(GameProfile gameProfile) {

                future.complete(gameProfile);
            }

            @Override
            public void onProfileLookupFailed(String s, Exception e) {
                future.completeExceptionally(e);

            }
        });
        return future;
    }
}
