package net.blzinite.flight_tools.event;

import net.blzinite.flight_tools.FlightToolsMod;
import net.blzinite.flight_tools.entity.FlyingToolRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber
public class RegisterRenderers {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FlightToolsMod.FLYING_TOOL.get(), FlyingToolRenderer::new);
    }
}
