package spout.client.fabric.moredatadriven.minecraft.type.mixin;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import spout.common.moredatadriven.minecraft.type.BaseStateStringBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spout.common.moredatadriven.minecraft.type.BlockCodecs;

@Mixin(StairBlock.class)
public abstract class LazyBaseStateStairBlockMixin implements BaseStateStringBlock {

    @Shadow
    @Final
    @Mutable
    public static MapCodec<StairBlock> CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void spout$replaceCodec(CallbackInfo ci) {
        CODEC = BlockCodecs.stairCodec(StairBlock::new);
    }

    @Shadow
    @Final
    @Mutable
    private Block base;

    @Shadow
    @Final
    @Mutable
    protected BlockState baseState;

    @Unique
    public String spout$baseStateString;

    @Override
    public @Nullable String spout$getBaseStateString() {
        return this.spout$baseStateString;
    }

    @Override
    public void spout$setBaseStateString(@Nullable String baseStateString) {
        this.spout$baseStateString = baseStateString;
    }

}
