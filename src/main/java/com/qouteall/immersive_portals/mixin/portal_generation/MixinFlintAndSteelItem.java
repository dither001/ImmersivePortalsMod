package com.qouteall.immersive_portals.mixin.portal_generation;

import com.qouteall.hiding_in_the_bushes.O_O;
import com.qouteall.immersive_portals.ModMain;
import com.qouteall.immersive_portals.portal.BreakableMirror;
import com.qouteall.immersive_portals.portal.CustomizablePortalGeneration;
import com.qouteall.immersive_portals.portal.nether_portal.NetherPortalGeneration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public class MixinFlintAndSteelItem {
    @Inject(
        method = "canIgnite",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onCanIgnite(
        BlockState block,
        IWorld world,
        BlockPos pos,
        CallbackInfoReturnable<Boolean> cir
    ) {
        for (Direction direction : Direction.values()) {
            if (O_O.isObsidian(world.getBlockState(pos.offset(direction)))) {
                if (block.isAir()) {
                    cir.setReturnValue(true);
                    cir.cancel();
                }
            }
        }
    }
    
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void onUseFlintAndSteel(
        ItemUsageContext context,
        CallbackInfoReturnable<ActionResult> cir
    ) {
        World world = context.getWorld();
        if (!world.isClient()) {
            BlockPos targetPos = context.getBlockPos();
            Direction side = context.getSide();
            BlockPos firePos = targetPos.offset(side);
            BlockState targetBlockState = world.getBlockState(targetPos);
            Block targetBlock = targetBlockState.getBlock();
            if (BreakableMirror.isGlass(world, targetPos)) {
                BreakableMirror mirror = BreakableMirror.createMirror(
                    ((ServerWorld) world), targetPos, side
                );
                cir.setReturnValue(ActionResult.SUCCESS);
            }
            else if (targetBlock == ModMain.portalHelperBlock) {
                boolean result = NetherPortalGeneration.activatePortalHelper(
                    ((ServerWorld) world),
                    firePos
                );
            }
            else {
                CustomizablePortalGeneration.onFireLit(
                    ((ServerWorld) world),
                    firePos,
                    targetBlock
                );
            }
        }
    }
}
