package spout.client.fabric.moredatadriven.minecraft.type.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BrushableBlock.class)
public interface LazyTurnsIntoBrushableBlockAccessor {

    @Accessor("turnsInto")
    void setTurnsInto(Block base);

}
