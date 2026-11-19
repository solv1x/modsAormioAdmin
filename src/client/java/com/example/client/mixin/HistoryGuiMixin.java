package com.example.client.mixin;

import com.example.client.HistoryAnalyzer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HistoryGuiMixin {

    @Inject(method = "containerTick", at = @At("HEAD"))
    private void moderutil$onTick(CallbackInfo ci) {
        String title = ((Screen)(Object)this).getTitle().getString();
        if (title.startsWith("История:")) {
            // каждый тик тихо сканируем слоты — новые матчи добавляются один раз
            HistoryAnalyzer.collect(title);
        }
    }

    // единый вердикт печатается один раз — при закрытии меню истории
    @Inject(method = "removed", at = @At("HEAD"))
    private void moderutil$onClose(CallbackInfo ci) {
        String title = ((Screen)(Object)this).getTitle().getString();
        if (title.startsWith("История:")) {
            HistoryAnalyzer.finish();
        }
    }
}
