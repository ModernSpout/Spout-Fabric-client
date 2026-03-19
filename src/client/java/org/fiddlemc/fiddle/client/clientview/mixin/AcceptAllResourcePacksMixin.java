package org.fiddlemc.fiddle.client.clientview.mixin;

import net.minecraft.client.resources.server.ServerPackManager;
import org.fiddlemc.fiddle.client.clientview.ClientModDetection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the client silently accept all resource packs from Fiddle servers.
 */
@Mixin(ServerPackManager.class)
public class AcceptAllResourcePacksMixin {

    @Shadow
    private ServerPackManager.PackPromptStatus packPromptStatus;

    @Inject(method = "pushPack", at = @At("HEAD"))
    private void silentlyAcceptOnPush(CallbackInfo ci) {
        if (ClientModDetection.sentToServerThatWeHaveClientMod) {
            // Silently accept
            this.packPromptStatus = ServerPackManager.PackPromptStatus.ALLOWED;
        }
    }

}
