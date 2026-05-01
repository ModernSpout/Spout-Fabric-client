package spout.client.fabric.moredatadriven;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import spout.common.moredatadriven.minecraft.type.ApplyLazyItemValues;
import java.util.List;

/**
 * The {@link TemporaryRegistryModifier} for {@link BuiltInRegistries#ITEM}.
 */
public final class TemporaryItemRegistryModifier extends TemporaryRegistryModifier<Item, DefaultedMappedRegistry<Item>> {

    TemporaryItemRegistryModifier() {
        super((DefaultedMappedRegistry<Item>) BuiltInRegistries.ITEM);
    }

    @Override
    public void add(List<Pair<ResourceKey<Item>, Item>> resources) {
        super.add(resources);
        if (!resources.isEmpty()) {
            // Apply lazy values
            ApplyLazyItemValues.apply(resources.stream().map(Pair::right));
        }
    }

}
