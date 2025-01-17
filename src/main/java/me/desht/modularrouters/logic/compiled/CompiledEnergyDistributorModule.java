package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CompiledEnergyDistributorModule extends CompiledModule {
    public CompiledEnergyDistributorModule(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        List<ModuleTarget> inRange = getTargets().stream()
                .filter(target -> target.isSameWorld(router.getLevel()) && router.getBlockPos().distSqr(target.gPos.pos()) <= getRangeSquared())
                .toList();
        if (inRange.isEmpty()) return false;

        int total = router.getCapability(CapabilityEnergy.ENERGY).map(routerHandler -> {
            int toSend = routerHandler.getEnergyStored() / inRange.size();
            int total1 = 0;
            boolean doBeam = router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2;
            for (ModuleTarget target : inRange) {
                total1 += target.getEnergyHandler().map(handler -> {
                    int toExtract = routerHandler.extractEnergy(toSend, true);
                    int sent = handler.receiveEnergy(toExtract, false);
                    routerHandler.extractEnergy(sent, false);
                    if (sent > 0 && doBeam) {
                        router.addItemBeam(new BeamData(router.getTickRate(), target.gPos.pos(), 0xE04040));
                    }
                    return sent;
                }).orElse(0);
            }
            return total1;
        }).orElse(0);

        return total > 0;
    }

    @Override
    public List<ModuleTarget> setupTargets(ModularRouterBlockEntity router, ItemStack stack) {
        return router == null ? Collections.emptyList() : ImmutableList.copyOf(TargetedModule.getTargets(stack, !router.getLevel().isClientSide));
    }
}
