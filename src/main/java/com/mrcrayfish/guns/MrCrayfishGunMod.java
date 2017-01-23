package com.mrcrayfish.guns;

import com.mrcrayfish.guns.entity.EntityBullet;
import com.mrcrayfish.guns.event.GuiOverlayEvent;
import com.mrcrayfish.guns.init.ModCrafting;
import com.mrcrayfish.guns.init.ModGuns;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.proxy.IProxy;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MC_VERSION)
public class MrCrayfishGunMod 
{
	@SidedProxy(clientSide = Reference.PROXY_CLIENT, serverSide = Reference.PROXY_SERVER)
	public static IProxy proxy;
	
	public static final CreativeTabs GUN_TAB = new TabGun();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ModGuns.init();
		ModGuns.register();
		ModSounds.register();
		ModCrafting.init();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init();
		
		EntityRegistry.registerModEntity(new ResourceLocation("cgm:bullet"), EntityBullet.class, "cfmBullet", 0, this, 64, 80, true);
		
		MinecraftForge.EVENT_BUS.register(new GuiOverlayEvent());
	}
}