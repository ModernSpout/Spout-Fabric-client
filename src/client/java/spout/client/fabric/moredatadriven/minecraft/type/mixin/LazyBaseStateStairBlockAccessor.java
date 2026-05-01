package spout.client.fabric.moredatadriven.minecraft.type.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StairBlock.class)
public interface LazyBaseStateStairBlockAccessor {

    @Accessor("base")
    void setBase(Block base);

    @Accessor("baseState")
    void setBaseState(BlockState baseState);

}
