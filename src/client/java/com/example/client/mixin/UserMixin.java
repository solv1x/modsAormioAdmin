package com.example.client.mixin;

import com.example.client.ConfigManager;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(User.class)
public class UserMixin {

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void onGetName(CallbackInfoReturnable<String> cir) {
        if (ConfigManager.customNickname != null && !ConfigManager.customNickname.isEmpty()) {
            cir.setReturnValue(ConfigManager.customNickname);
        }
    }
}