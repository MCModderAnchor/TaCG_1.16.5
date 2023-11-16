package com.tac.guns.client.render.model;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GunComponent implements Comparable<GunComponent>{
    public final String key;
    public final String namespace;
    public String group;

    private static final HashMap<String, HashMap<String, GunComponent>> componentMap = new HashMap<>();

    public GunComponent(@Nullable String key){
        this(key, key);
    }

    public GunComponent(@Nullable String key, @Nullable String group){
        this(null, key, group);
    }

    public GunComponent(@Nullable String namespace, @Nullable String key, @Nullable String group){
        this.key = key;
        this.namespace = namespace;
        this.group = group;
        componentMap.compute(namespace, (k, map)->{
            if(map == null){
                map = new HashMap<>();
            }
            map.put(key, this);
            return map;
        });
    }

    public static GunComponent getComponent(@Nullable String namespace, @Nullable String key){
        Map<String, GunComponent> map = componentMap.get(namespace);
        if(map != null){
            return map.get(key);
        }
        return null;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof GunComponent component){
            return
                    Objects.equals(this.key, component.key) &&
                    Objects.equals(this.namespace, component.namespace);
        }
        else
            return false;
    }

    @Override
    public int hashCode(){
        return (namespace == null ? 0 : namespace.hashCode()) * 31 + (key == null ? 0 : key.hashCode());
    }

    @Override
    public int compareTo(@Nonnull GunComponent o) {
        int r = namespace.compareTo(o.namespace);
        if(r != 0) return r;
        return key.compareTo(o.key);
    }

    @Nullable
    public ResourceLocation getModelLocation(String mainLocation){
        return ResourceLocation.tryParse(mainLocation+(this.key == null ? "" : "_" + this.key));
    }
    @Nullable
    public ResourceLocation getModelLocation(ResourceLocation mainLocation){
        return ResourceLocation.tryParse(mainLocation+(this.key == null ? "" : "_" + this.key));
    }
}
