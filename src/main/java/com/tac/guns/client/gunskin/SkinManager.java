package com.tac.guns.client.gunskin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tac.guns.GunMod;
import com.tac.guns.Reference;
import com.tac.guns.item.TransitionalTypes.TimelessGunItem;
import com.tac.guns.util.GunModifierHelper;
import javafx.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class SkinManager {
    private static Map<String, Map<ResourceLocation, GunSkin>> skins = new HashMap<>();
    private static final Map<String, DefaultSkin> defaultSkins = new HashMap<>();

    public static void reload() {
        skins = new HashMap<>();
        init();
    }

    public static void cleanCache() {
        for (GunSkin skin : defaultSkins.values()) {
            skin.cleanCache();
        }
        for (Map<ResourceLocation, GunSkin> map : skins.values()) {
            for (GunSkin skin : map.values()) {
                skin.cleanCache();
            }
        }
    }

    private static void init() {
        //get skin configs from all namespace
        Set<String> nameSpaces = Minecraft.getInstance().getResourceManager().getResourceNamespaces();
        for (String nameSpace : nameSpaces) {
            ResourceLocation loc = new ResourceLocation(nameSpace, "models/gunskin/skin.json");
            try {
                List<IResource> all = Minecraft.getInstance().getResourceManager().getAllResources(loc);
                for (IResource resource : all) {
                    loadSkinList(resource);
                }
            } catch (IOException e) {
                GunMod.LOGGER.warn("Failed to load skins from {} {}", loc, e);
            }
        }
    }

    private static void loadSkinList(IResource resource) throws IOException {
        JsonObject json;
        InputStream stream = resource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        json = new JsonParser().parse(reader).getAsJsonObject();
        String nameSpace = resource.getLocation().getNamespace();

        for (Map.Entry<String, JsonElement> e : json.entrySet()) {
            //gun
            String gun = e.getKey();
            SkinLoader loader = SkinLoader.getSkinLoader(gun);
            if (loader == null) continue;
            JsonObject skins = e.getValue().getAsJsonObject();

            for (Map.Entry<String, JsonElement> skin : skins.entrySet()) {
                // skin
                try {
                    String skinName = skin.getKey();
                    JsonObject skinObject = skin.getValue().getAsJsonObject();

                    String skinType = skinObject.get("type").getAsString();
                    ResourceLocation skinLoc = ResourceLocation.tryCreate(nameSpace + ":" + skinName);

                    if (skinLoc == null) {
                        GunMod.LOGGER.warn("Failed to load skins of {} named {}: invalid name.", gun, skinName);
                        continue;
                    } else if (!defaultSkins.containsKey(gun)) {
                        GunMod.LOGGER.warn("Failed to load skins of {} named {}: default skin no loaded.", gun, skinName);
                        continue;
                    }

                    if ("custom".equals(skinType)) {
                        JsonObject modelObject = skinObject.get("models").getAsJsonObject();

                        Map<String, String> components = new HashMap<>();

                        for (Map.Entry<String, JsonElement> c : modelObject.entrySet()) {
                            components.put(c.getKey(), c.getValue().getAsString());
                        }

                        if (registerCustomSkin(loader, skinLoc, components)) {
                            GunMod.LOGGER.info("Loaded custom gun skin of {} named {}", gun, skinName);
                        }

                    } else if ("texture".equals(skinType)) {
                        JsonObject modelObject = skinObject.get("textures").getAsJsonObject();

                        List<Pair<String, ResourceLocation>> textures = new ArrayList<>();

                        for (Map.Entry<String, JsonElement> c : modelObject.entrySet()) {
                            ResourceLocation tl = ResourceLocation.tryCreate(c.getValue().getAsString());
                            if (tl != null) {
                                textures.add(new Pair<>(c.getKey(), tl));
                            }
                        }

                        if (registerTextureOnlySkin(loader, skinLoc, textures)) {
                            GunMod.LOGGER.info("Loaded texture-only gun skin of {} named {}", gun, skinName);
                        }
                    } else {
                        GunMod.LOGGER.warn("Failed to load skins of {} named {}: unknown type.", gun, skinName);
                    }

                } catch (Exception e2) {
                    GunMod.LOGGER.warn("Failed to load skins from {} {}.", resource.getLocation(), e2);
                }
            }
        }
        reader.close();
        stream.close();
    }

    public static void loadDefaultSkins() {
        for (SkinLoader loader : SkinLoader.values()) {
            DefaultSkin skin = loader.loadDefaultSkin();
            defaultSkins.put(loader.getGun(), skin);
        }
    }

    private static boolean registerCustomSkin(SkinLoader loader, ResourceLocation skinLocation, Map<String, String> models) {
        GunSkin skin = loader.loadCustomSkin(skinLocation, models);

        if (skin != null) {
            skins.putIfAbsent(loader.getGun(), new HashMap<>());
            skins.get(loader.getGun()).put(skinLocation, skin);
            return true;
        } else return false;
    }

    private static boolean registerTextureOnlySkin(SkinLoader loader, ResourceLocation skinLocation, List<Pair<String, ResourceLocation>> textures) {
        GunSkin skin = loader.loadTextureOnlySkin(skinLocation, textures);

        if (skin != null) {
            skins.putIfAbsent(loader.getGun(), new HashMap<>());
            skins.get(loader.getGun()).put(skinLocation, skin);
            return true;
        } else return false;
    }

    public static GunSkin getSkin(String gun, ResourceLocation skinLocation) {
        if (skinLocation != null && skins.containsKey(gun)) return skins.get(gun).get(skinLocation);
        else return null;
    }

    private static GunSkin getAttachedSkin(ItemStack weapon) {
        if (weapon.getItem() instanceof TimelessGunItem) {
            String gun = weapon.getItem().toString();
            String[] skinList = {
                    "ak47.ak47_GOLDEN",
                    "ak47.ak47_SILVER",

                    "all.BEIGE",
                    "all.BLACK",
                    "all.BLUE",
                    "all.GREEN",
                    "all.JADE",
                    "all.ORANGE",
                    "all.PINK",
                    "all.SAND",
                    "all.WHITE"
            };

            for (String s : skinList) {
                String[] currentSkin = s.split("\\.");
                if (currentSkin.length < 2) return null;
                String resourceName = "tac:" + currentSkin[1].toLowerCase();
                if (currentSkin[0].equals("all")) {
                    if (currentSkin[1].equals(GunModifierHelper.getAdditionalSkin(weapon)))
                        return getSkin(gun, new ResourceLocation(resourceName));
                } else {
                    if (currentSkin[1].equals(GunModifierHelper.getAdditionalSkin(weapon)) && gun.equals(currentSkin[0]))
                        return getSkin(gun, new ResourceLocation(resourceName));
                }
            }
        }
        return null;
    }

    public static GunSkin getSkin(ItemStack stack, String gun) {
        GunSkin skin = null;
        if (stack.getTag() != null) {
            if (stack.getTag().contains("Skin", Constants.NBT.TAG_STRING)) {
                String skinLoc = stack.getTag().getString("Skin");
                ResourceLocation loc;
                if (skinLoc.contains(":")) {
                    loc = ResourceLocation.tryCreate(skinLoc);
                } else {
                    loc = new ResourceLocation(Reference.MOD_ID, skinLoc);
                }
                skin = getSkin(gun, loc);
            }
        }
        if (skin == null) {
            skin = getAttachedSkin(stack);
        }
        if (skin == null && defaultSkins.containsKey(gun)) {
            return defaultSkins.get(gun);
        }
        return skin;
    }

    public static DefaultSkin getDefaultSkin(String gun) {
        return defaultSkins.get(gun);
    }
}
