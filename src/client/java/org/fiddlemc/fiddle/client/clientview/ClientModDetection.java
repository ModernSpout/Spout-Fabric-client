package org.fiddlemc.fiddle.client.clientview;

import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.fiddlemc.fiddle.impl.branding.FiddleNamespace;
import java.util.concurrent.CompletableFuture;

/**
 * Handles the detection of the client mod by the server.
 */
public final class ClientModDetection {

    private static final Identifier CLIENT_MOD_DETECTION_PACKET_ID = Identifier.fromNamespaceAndPath(FiddleNamespace.FIDDLE, "detect_client_mod");
    private static final int MIN_PROTOCOL_VERSION = 1;
    private static final int MAX_PROTOCOL_VERSION = 1;

    public static volatile boolean sentToServerThatWeHaveClientMod;

    private ClientModDetection() {
        throw new UnsupportedOperationException();
    }

    public static void initialize() {
        ClientLoginNetworking.registerGlobalReceiver(CLIENT_MOD_DETECTION_PACKET_ID, (client, handler, buf, callbacksConsumer) -> {
            // First, the server will send a 0, if not, then there must be a protocol difference that we are unaware of
            int zero = buf.readVarInt();
            if (zero == 0) {
                // Read the nonce
                int nonce = buf.readVarInt();
                // Read the protocol versions supported by the server (and perform basic validation on what we read)
                int minServerProtocolVersion = buf.readVarInt();
                if (minServerProtocolVersion >= 1) {
                    int maxServerProtocolVersion = buf.readVarInt();
                    if (maxServerProtocolVersion >= minServerProtocolVersion) {
                        // The best protocol version is the highest supported by both client and server
                        int bestProtocolVersion = Math.min(maxServerProtocolVersion, MAX_PROTOCOL_VERSION);
                        boolean isBestProtocolVersionAcceptable = bestProtocolVersion >= minServerProtocolVersion && bestProtocolVersion >= MIN_PROTOCOL_VERSION;
                        int responseProtocolVersion = -1; // In case of failure, respond with an invalid version
                        if (isBestProtocolVersionAcceptable) {
                            sentToServerThatWeHaveClientMod = true;
                            responseProtocolVersion = bestProtocolVersion;
                        }
                        // Respond
                        FriendlyByteBuf response = new FriendlyByteBuf(PacketByteBufs.create());
                        response.writeVarInt(0);
                        response.writeVarInt(nonce);
                        // response.writeVarInt(responseProtocolVersion);
                        response.writeVarInt(-1);
                        return CompletableFuture.completedFuture(response);
                    }
                }
            }
            // We did not understand this protocol
            return CompletableFuture.completedFuture(null);
        });
    }

}
