package spout.client.fabric.moredatadriven.minecraft.type.mixin;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spout.common.moredatadriven.minecraft.type.BlockCodecs;
import spout.common.moredatadriven.minecraft.type.TurnsIntoIdentifierBlock;

@Mixin(BrushableBlock.class)
public abstract class LazyTurnsIntoBrushableBlockMixin implements TurnsIntoIdentifierBlock {

    @Shadow
    @Final
    @Mutable
    public static MapCodec<BrushableBlock> CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void spout$replaceCodec(CallbackInfo ci) {
        CODEC = BlockCodecs.brushableCodec(BrushableBlock::new);
    }

    @Shadow
    @Final
    @Mutable
    private Block turnsInto;

    @Unique
    public Identifier spout$turnsIntoIdentifier;

    @Override
    public @Nullable Identifier spout$getTurnsIntoIdentifier() {
        return this.spout$turnsIntoIdentifier;
    }

    @Override
    public void spout$setTurnsIntoIdentifier(@Nullable Identifier turnsIntoIdentifier) {
        this.spout$turnsIntoIdentifier = turnsIntoIdentifier;
    }

}
