package net.blzinite.flight_tools.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FlyingToolRenderer extends EntityRenderer<FlyingTool> {
    private final ItemRenderer itemRenderer;

    public FlyingToolRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FlyingTool entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ItemStack itemStack = entity.getItem();

        float yaw = entity.getViewYRot(partialTick);
        float pitch = entity.getViewXRot(partialTick);

        Quaternionf rotation = new Quaternionf()
                .rotateY((float) Math.toRadians(-yaw-90f))
                .rotateZ((float) Math.toRadians(-90f));


        Vector3f normal = new Vector3f(0, 0, 1).normalize();
        rotation.transform(normal);

        Quaternionf pitchRotation = new Quaternionf()
                .fromAxisAngleRad(
                        normal.x, normal.y, normal.z,
                        (float) Math.toRadians(-pitch)
                );

        rotation.premul(pitchRotation);

        // Calculate banking using the angle between facing and travel direction.
        Vec3 viewVector = entity.getViewVector(partialTick);
        Vec3 velocity = entity.getDeltaMovementLerped(partialTick);

        double velocityLengthSqr = velocity.horizontalDistanceSqr();
        double viewLengthSqr = viewVector.horizontalDistanceSqr();

        if (velocityLengthSqr > 1.0E-6 && viewLengthSqr > 1.0E-6) {
            double dot = (velocity.x * viewVector.x + velocity.z * viewVector.z) / Math.sqrt(velocityLengthSqr * viewLengthSqr);
            double cross = velocity.x * viewVector.z - velocity.z * viewVector.x;

            dot = Mth.clamp(dot, -1.0, 1.0);

            float bankAngle = Mth.clamp((float) (Math.signum(cross) * Math.acos(dot)), -0.6f, 0.6f);

            // Bank around the item's forward axis.
            Vector3f forward = new Vector3f(0, 1, 0);
            rotation.transform(forward);

            Quaternionf bankRotation = new Quaternionf()
                    .fromAxisAngleRad(
                            forward.x, forward.y, forward.z,
                            bankAngle
                    );

            rotation.premul(bankRotation);
        }

        poseStack.pushPose();
        poseStack.mulPose(rotation);

        this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), 0);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingTool entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}

