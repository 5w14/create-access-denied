package net.fw14.createAddons.accessDenied.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor
    YggdrasilAuthenticationService getAuthenticationService();
}
