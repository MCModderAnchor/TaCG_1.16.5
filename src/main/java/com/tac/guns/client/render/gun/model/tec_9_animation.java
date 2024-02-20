package com.tac.guns.client.render.gun.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.tac.guns.client.gunskin.GunSkin;
import com.tac.guns.client.gunskin.SkinManager;
import com.tac.guns.client.handler.ShootingHandler;
import com.tac.guns.client.render.animation.TEC9AnimationController;
import com.tac.guns.client.render.animation.module.AnimationMeta;
import com.tac.guns.client.render.animation.module.GunAnimationController;
import com.tac.guns.client.render.animation.module.PlayerHandAnimation;
import com.tac.guns.client.render.gun.SkinAnimationModel;
import com.tac.guns.common.Gun;
import com.tac.guns.item.GunItem;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import static com.tac.guns.client.gunskin.ModelComponent.*;

/*
 * Because the revolver has a rotating chamber, we need to render it in a
 * different way than normal items. In this case we are overriding the model.
 */

/**
 * Author: Timeless Development, and associates.
 */
public class tec_9_animation extends SkinAnimationModel {
    @Override
    public void render(float partialTicks, ItemCameraTransforms.TransformType transformType, ItemStack stack, ItemStack parent, LivingEntity entity, MatrixStack matrices, IRenderTypeBuffer renderBuffer, int light, int overlay) {
        TEC9AnimationController controller = TEC9AnimationController.getInstance();
        GunSkin skin = SkinManager.getSkin(stack);

        matrices.push();
        controller.applySpecialModelTransform(getModelComponent(skin, BODY), TEC9AnimationController.INDEX_BODY, transformType, matrices);
        renderComponent(stack, matrices, renderBuffer, light, overlay, skin, BODY);
        matrices.pop();

        matrices.push();
        controller.applySpecialModelTransform(getModelComponent(skin, BODY), TEC9AnimationController.INDEX_MAG, transformType, matrices);
        renderMag(stack, matrices, renderBuffer, light, overlay, skin);
        matrices.pop();

        //Always push
        matrices.push();
        controller.applySpecialModelTransform(getModelComponent(skin, BODY), TEC9AnimationController.INDEX_BOLT, transformType, matrices);
        Gun gun = ((GunItem) stack.getItem()).getGun();
        int gunRate = (int) Math.min(ShootingHandler.calcShootTickGap(gun.getGeneral().getRate()), 4);
        int rateBias = (int) (ShootingHandler.calcShootTickGap(gun.getGeneral().getRate()) - gunRate);
        float cooldownOg = (ShootingHandler.get().getshootMsGap() - rateBias) / gunRate < 0 ? 1 : MathHelper.clamp((ShootingHandler.get().getshootMsGap() - rateBias) / gunRate, 0, 1);
        if (transformType.isFirstPerson()) {
            AnimationMeta reloadEmpty = controller.getAnimationFromLabel(GunAnimationController.AnimationLabel.RELOAD_EMPTY);
            boolean shouldOffset = reloadEmpty != null && reloadEmpty.equals(controller.getPreviousAnimation()) && controller.isAnimationRunning();
            if (!shouldOffset && !Gun.hasAmmo(stack)) {
                matrices.translate(0, 0, -0.375);
            } else {
                matrices.translate(0, 0, -0.375 + Math.pow(cooldownOg - 0.5, 2));
            }
        }
        renderComponent(stack, matrices, renderBuffer, light, overlay, skin, BOLT);
        //Always pop
        matrices.pop();

        matrices.push();
        controller.applySpecialModelTransform(getModelComponent(skin, BODY), TEC9AnimationController.INDEX_BULLET, transformType, matrices);
        renderComponent(stack, matrices, renderBuffer, light, overlay, skin, BULLET);
        matrices.pop();

        PlayerHandAnimation.render(controller, transformType, matrices, renderBuffer, light);
    }
}
