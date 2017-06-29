package me.desht.modularrouters.proxy;

import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.fx.FXSparkle;
import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void init() {
        super.init();

        registerBlockColors();
    }

    private static boolean noclipEnabled = false;
    private static boolean corruptSparkle = false;

    @Override
    public void setSparkleFXNoClip(boolean noclip) {
        noclipEnabled = noclip;
    }

    @Override
    public void setSparkleFXCorrupt(boolean corrupt) {
        corruptSparkle = corrupt;
    }

    @Override
    public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m, boolean fake) {
        if (!doParticle(world) && !fake)
            return;

        FXSparkle sparkle = new FXSparkle(world, x, y, z, size, r, g, b, m);
        sparkle.fake = sparkle.noClip = fake;
        if (noclipEnabled)
            sparkle.noClip = true;
        if (corruptSparkle)
            sparkle.corrupt = true;
        Minecraft.getMinecraft().effectRenderer.addEffect(sparkle);
    }

    private boolean doParticle(World world) {
        if(!world.isRemote)
            return false;

//        if(!ConfigHandler.useVanillaParticleLimiter)
//            return true;

        float chance = 1F;
        if(Minecraft.getMinecraft().gameSettings.particleSetting == 1)
            chance = 0.6F;
        else if(Minecraft.getMinecraft().gameSettings.particleSetting == 2)
            chance = 0.2F;

        return chance == 1F || Math.random() < chance;
    }

    @Override
    public World theClientWorld() {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public IThreadListener threadListener() {
        return Minecraft.getMinecraft();
    }

    @Override
    public TileEntityItemRouter getOpenItemRouter() {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
            return ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).router;
        } else {
            return null;
        }
    }

    private void registerBlockColors() {
        // this ensures camouflage properly mimics colourable blocks like grass blocks
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
            TileEntity te = MiscUtil.getTileEntitySafely(worldIn, pos);
            if (te instanceof ICamouflageable) {
                return Minecraft.getMinecraft().getBlockColors().colorMultiplier(((ICamouflageable) te).getCamouflage(), te.getWorld(), pos, tintIndex);
            } else {
                return -1;
            }
        }, RegistrarMR.ITEM_ROUTER, RegistrarMR.TEMPLATE_FRAME);
    }
}
