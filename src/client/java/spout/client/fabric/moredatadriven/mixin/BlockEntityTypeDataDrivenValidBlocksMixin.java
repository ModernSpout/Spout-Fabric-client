package spout.client.fabric.moredatadriven.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeDataDrivenValidBlocksMixin {

    @Unique
    public @Nullable TagKey<Block> spout$cachedValidBlocksTag;

    public TagKey<Block> spout$getValidBlocksTag() {
        if (this.spout$cachedValidBlocksTag == null) {
            Identifier key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey((BlockEntityType) (Object) this);
            this.spout$cachedValidBlocksTag = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(key.getNamespace(), "entity/" + key.getPath()));
        }
        return this.spout$cachedValidBlocksTag;
    }

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void spout$checkDataDrivenTag(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(this.spout$getValidBlocksTag())) {
            cir.setReturnValue(true);
        }
    }

}
