package org.fiddlemc.fiddle.impl.content.block.mixin;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTypes;
import org.fiddlemc.fiddle.impl.branding.FiddleNamespace;
import org.fiddlemc.fiddle.impl.content.block.BevelBlock;
import org.fiddlemc.fiddle.impl.content.block.HalfTransparentSlabBlock;
import org.fiddlemc.fiddle.impl.content.block.HalfTransparentStairBlock;
import org.fiddlemc.fiddle.impl.content.block.TransparentSlabBlock;
import org.fiddlemc.fiddle.impl.content.block.TransparentStairBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockTypes.class)
public abstract class InsertIntoBlockTypesMixin {

    @Inject(
        method = "bootstrap",
        at = @At("HEAD")
    )
    private static void onBootstrap(
        Registry<MapCodec<? extends Block>> registry,
        CallbackInfoReturnable<MapCodec<? extends Block>> cir
    ) {
        Registry.register(registry, Identifier.fromNamespaceAndPath(FiddleNamespace.FIDDLE, "bevel"), BevelBlock.CODEC);
        Registry.register(registry, Identifier.fromNamespaceAndPath(FiddleNamespace.FIDDLE, "half_transparent_slab"), HalfTransparentSlabBlock.CODEC);
        Registry.register(registry, Identifier.fromNamespaceAndPath(FiddleNamespace.FIDDLE, "half_transparent_stair"), HalfTransparentStairBlock.CODEC);
        Registry.register(registry, Identifier.fromNamespaceAndPath(FiddleNamespace.FIDDLE, "transparent_slab"), TransparentSlabBlock.CODEC);
        Registry.register(registry, Identifier.fromNamespaceAndPath(FiddleNamespace.FIDDLE, "transparent_stair"), TransparentStairBlock.CODEC);
    }

}
