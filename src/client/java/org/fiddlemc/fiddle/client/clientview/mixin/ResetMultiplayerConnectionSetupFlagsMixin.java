package org.fiddlemc.fiddle.client.clientview.mixin;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import org.fiddlemc.fiddle.client.clientview.ClientModDetection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class ResetMultiplayerConnectionSetupFlagsMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructed(CallbackInfo ci) {
        // Reset multiplayer connection setup flags
        ClientModDetection.sentToServerThatWeHaveClientMod = false;
    }

}
