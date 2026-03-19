package org.fiddlemc.fiddle.client;

import net.fabricmc.api.ClientModInitializer;
import org.fiddlemc.fiddle.client.clientview.ClientModDetection;

public class FiddleClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
        ClientModDetection.initialize();
    }

}
