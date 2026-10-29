package net.blzinite.flight_tools;

import net.blzinite.flight_tools.data.BoostHolder;
import net.blzinite.flight_tools.data.BoostItem;
import net.blzinite.flight_tools.entity.FlyingTool;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

@Mod(FlightToolsMod.MODID)
public class FlightToolsMod {
    public static final String MODID = "flight_tools";


    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<FlyingTool>> FLYING_TOOL = registerEntity("flying_tool",
                    EntityType.Builder.<FlyingTool>of(FlyingTool::new, MobCategory.MISC)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .sized(0.5F, 0.5F)
    );


    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BoostHolder>> BOOST_HOLDER = COMPONENTS.registerComponentType(
            "boost_holder", builder -> builder.persistent(BoostHolder.CODEC).networkSynchronized(BoostHolder.STREAM_CODEC)
    );


    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<Item> BROOM = ITEMS.register("broom",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(BOOST_HOLDER,
                            new BoostHolder(30, List.of(new BoostItem(Items.BLAZE_POWDER, 30)))
                    )
    ));

    public FlightToolsMod(IEventBus modEventBus, ModContainer modContainer) {
        ENTITY_TYPE.register(modEventBus);
        COMPONENTS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder) {
        return ENTITY_TYPE.register(name, () -> builder.build(name));
    }
}
