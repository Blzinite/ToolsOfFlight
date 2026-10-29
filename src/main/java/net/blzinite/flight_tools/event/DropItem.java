package net.blzinite.flight_tools.event;

import net.blzinite.flight_tools.FlightToolsMod;
import net.blzinite.flight_tools.entity.FlyingTool;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber
public class DropItem {
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {

        if (event.getLevel() instanceof ServerLevel server) {
            if (event.getEntity() instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();
                if (stack.has(FlightToolsMod.BOOST_HOLDER) && itemEntity.getOwner() instanceof Player player) {
                    FlyingTool flyingTool = new FlyingTool(event.getLevel(), stack, player);
                    flyingTool.moveTo(itemEntity.position());
                    if (server.addFreshEntity(flyingTool)) {
                        event.setCanceled(true);
                    }
                }
            } else if (event.getEntity() instanceof FlyingTool flyingTool) {
                Player rider = flyingTool.getRider();
                if (rider != null) {
                    rider.startRiding(flyingTool);
                }
            }
        }
    }
}
