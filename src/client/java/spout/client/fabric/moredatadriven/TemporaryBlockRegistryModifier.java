package spout.client.fabric.moredatadriven;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import spout.common.moredatadriven.minecraft.type.ApplyLazyBlockValues;
import spout.client.fabric.moredatadriven.minecraft.type.PropertiesExtensions;
import spout.client.fabric.moredatadriven.minecraft.type.mixin.ItemBlockRenderTypesAccessor;
import org.jspecify.annotations.Nullable;
import java.util.List;

/**
 * The {@link TemporaryRegistryModifier} for {@link BuiltInRegistries#BLOCK}.
 */
public final class TemporaryBlockRegistryModifier extends TemporaryRegistryModifier<Block, DefaultedMappedRegistry<Block>> {

    TemporaryBlockRegistryModifier() {
        super((DefaultedMappedRegistry<Block>) BuiltInRegistries.BLOCK);
    }

    @Override
    public void add(List<Pair<ResourceKey<Block>, Block>> resources) {
        super.add(resources);
        if (!resources.isEmpty()) {
            // Apply lazy values
            ApplyLazyBlockValues.apply(resources.stream().map(Pair::right));
        }
    }

    @Override
    public void add(ResourceKey<Block> resourceKey, Block resource) {
        super.add(resourceKey, resource);
        // Add render type
        @Nullable ChunkSectionLayer chunkSectionLayer = ((PropertiesExtensions) resource.properties()).spout$getChunkSectionLayer();
        if (chunkSectionLayer != null) {
            ItemBlockRenderTypesAccessor.getTypeByBlock().put(resource, chunkSectionLayer);
        }
    }

    @Override
    public void remove(ResourceKey<Block> resourceKey, Block resource) {
        super.remove(resourceKey, resource);
        // Remove render type
        ItemBlockRenderTypesAccessor.getTypeByBlock().remove(resource);
    }

}
