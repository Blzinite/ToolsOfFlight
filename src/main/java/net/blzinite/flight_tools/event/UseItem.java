package net.blzinite.flight_tools.event;

import net.blzinite.flight_tools.FlightToolsMod;
import net.blzinite.flight_tools.data.BoostHolder;
import net.blzinite.flight_tools.data.BoostItem;
import net.blzinite.flight_tools.entity.FlyingTool;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class UseItem {
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getVehicle() instanceof FlyingTool tool) {
            ItemStack holdingStack = event.getItemStack();
            ItemStack flyingStack = tool.getItem();
            if (!holdingStack.isEmpty() && flyingStack.has(FlightToolsMod.BOOST_HOLDER)) {
                BoostHolder boostHolder = flyingStack.get(FlightToolsMod.BOOST_HOLDER);
                for (BoostItem boostItem : boostHolder.boostItems()) {
                    if (boostItem.item().equals(holdingStack.getItem())) {
                        tool.setBoost(boostItem.boost());
                        if (!player.getAbilities().instabuild) holdingStack.shrink(1);
                        player.swing(event.getHand());
                        return;
                    }
                }
            }
        }
    }
}
