package net.blzinite.flight_tools.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record BoostItem(Item item, int boost) {
    public static final Codec<BoostItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(BoostItem::item),
            Codec.INT.fieldOf("boost").forGetter(BoostItem::boost)
    ).apply(instance, BoostItem::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoostItem> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.registry(Registries.ITEM),
                    BoostItem::item,

                    ByteBufCodecs.VAR_INT,
                    BoostItem::boost,

                    BoostItem::new
            );
}
