package net.blzinite.flight_tools.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record BoostHolder(int initalBoost, List<BoostItem> boostItems) {
    public static final Codec<BoostHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("inital_boost").forGetter(BoostHolder::initalBoost),
            BoostItem.CODEC.listOf().fieldOf("boost_items").forGetter(BoostHolder::boostItems)
    ).apply(instance, BoostHolder::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoostHolder> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    BoostHolder::initalBoost,

                    BoostItem.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    BoostHolder::boostItems,

                    BoostHolder::new
            );
}
