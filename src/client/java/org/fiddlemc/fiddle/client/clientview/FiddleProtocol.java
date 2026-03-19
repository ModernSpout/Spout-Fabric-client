package org.fiddlemc.fiddle.client.clientview;

import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.fiddlemc.fiddle.client.moredatadriven.TemporaryRegistryModifiers;
import org.fiddlemc.fiddle.impl.branding.FiddleNamespace;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles the detection of the client mod by the server,
 * and the interpretation of related packets.
 */
public final class FiddleProtocol {

    private static final Identifier CLIENT_MOD_DETECTION_PACKET_ID = Identifier.fromNamespaceAndPath(FiddleNamespace.FIDDLE, "detect_client_mod");
    private static final int MIN_PROTOCOL_VERSION = 1;
    private static final int MAX_PROTOCOL_VERSION = 1;

    private static final Identifier CLIENT_MOD_CUSTOM_CONTENT_PACKET_ID = Identifier.fromNamespaceAndPath(FiddleNamespace.FIDDLE, "custom_content");
    private static final CustomPacketPayload.Type<?> CLIENT_MOD_CUSTOM_CONTENT_PACKET_PAYLOAD_TYPE = new CustomPacketPayload.Type<>(CLIENT_MOD_CUSTOM_CONTENT_PACKET_ID);

    private static final AtomicReference<ClientModState> state = new AtomicReference<>(ClientModState.IDLE);

    private FiddleProtocol() {
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
                            changeState(ClientModState.HANDSHAKE_STARTED, ClientModState.CLIENT_MOD_DETECTED);
                            responseProtocolVersion = bestProtocolVersion;
                        }
                        // Respond
                        FriendlyByteBuf response = new FriendlyByteBuf(PacketByteBufs.create());
                        response.writeVarInt(0);
                        response.writeVarInt(nonce);
                        response.writeVarInt(responseProtocolVersion);
                        return CompletableFuture.completedFuture(response);
                    }
                }
            }
            // We did not understand this protocol
            return CompletableFuture.completedFuture(null);
        });
        PayloadTypeRegistry.configurationS2C().register(CLIENT_MOD_CUSTOM_CONTENT_PACKET_PAYLOAD_TYPE, (StreamCodec) StreamCodec.unit((CustomPacketPayload) () -> CLIENT_MOD_CUSTOM_CONTENT_PACKET_PAYLOAD_TYPE));
        ClientConfigurationNetworking.registerGlobalReceiver(CLIENT_MOD_CUSTOM_CONTENT_PACKET_PAYLOAD_TYPE, (payload, context) -> {
            System.out.println("Received custom content " + FiddleProtocol.getState());
            changeState(ClientModState.CLIENT_MOD_DETECTED, ClientModState.RECEIVED_CUSTOM_CONTENT);
            // Add the received content
            TemporaryRegistryModifiers.prepareToAddCustomContent();
            TemporaryRegistryModifiers.addCustomContent(
                List.of(
                    Pair.of(Identifier.parse("example:test"), new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.parse("example:test")))))
                ),
                List.of(
                    Pair.of(Identifier.parse("example:test"), new Item(new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.parse("example:test")))))
                )
            );
            System.out.println("Added custom content " + FiddleProtocol.getState());
            changeState(ClientModState.RECEIVED_CUSTOM_CONTENT, ClientModState.ADDED_CUSTOM_CONTENT);
        });
        ClientLoginConnectionEvents.INIT.register((handler, client) -> {
            FiddleProtocol.changeState(ClientModState.IDLE, ClientModState.HANDSHAKE_STARTED);
        });
        ClientConfigurationConnectionEvents.INIT.register((handler, client) -> {
            while (true) {
                if (FiddleProtocol.getState() == ClientModState.CLIENT_MOD_DETECTED) {
                    return;
                }
                if (FiddleProtocol.tryChangeState(ClientModState.HANDSHAKE_STARTED, ClientModState.CLIENT_MOD_NOT_DETECTED)) {
                    return;
                }
                Thread.onSpinWait();
            }
        });
        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> {
            onDisconnect();
        });
        ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> {
            onDisconnect();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            onDisconnect();
        });
    }

    private static void onDisconnect() {
        // Clear custom content, if present
        while (true) {
            if (FiddleProtocol.getState() == ClientModState.ADDED_CUSTOM_CONTENT) {
                TemporaryRegistryModifiers.removeCustomContent();
                System.out.println("Post-remove: " + FiddleProtocol.getState());
                FiddleProtocol.changeState(ClientModState.ADDED_CUSTOM_CONTENT, ClientModState.REMOVED_CUSTOM_CONTENT);
                break;
            }
            if (FiddleProtocol.tryChangeState(Set.of(ClientModState.IDLE, ClientModState.HANDSHAKE_STARTED, ClientModState.CLIENT_MOD_DETECTED, ClientModState.CLIENT_MOD_NOT_DETECTED), ClientModState.REMOVED_CUSTOM_CONTENT)) {
                break;
            }
            Thread.onSpinWait();
        }
        System.out.println("Resetting to idle: " + FiddleProtocol.getState());
        // Reset to idle
        FiddleProtocol.changeState(ClientModState.REMOVED_CUSTOM_CONTENT, ClientModState.IDLE);
        System.out.println("Reset to idle: " + FiddleProtocol.getState());
    }

    public static boolean tryChangeState(ClientModState oldState, ClientModState newState) {
        System.out.println("Try change state (" + state.get() + "): " + oldState + " -> " + newState);
        return state.compareAndSet(oldState, newState);
    }

    public static void changeState(ClientModState oldState, ClientModState newState) {
        while (!tryChangeState(oldState, newState)) {
            Thread.onSpinWait();
        }
    }

    public static boolean tryChangeState(Set<ClientModState> oldStates, ClientModState newState) {
        System.out.println("Try change state (" + state.get() + "): " + oldStates + " -> " + newState);
        ClientModState currentState = state.get();
        if (oldStates.contains(currentState)) {
            if (state.compareAndSet(currentState, newState)) {
                return true;
            }
        }
        return false;
    }

    public static void changeState(Set<ClientModState> oldStates, ClientModState newState) {
        while (!tryChangeState(oldStates, newState)) {
            Thread.onSpinWait();
        }
    }

    public static ClientModState getState() {
        return state.get();
    }

}
