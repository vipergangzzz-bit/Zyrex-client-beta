package com.zyrex.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zyrex.module.Module;
import com.zyrex.module.ModuleManager;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import com.zyrex.setting.Setting;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final File FILE = new File("config/zyrex.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load(ModuleManager mm) {
        if (!FILE.exists()) return;
        try {
            Reader reader = new FileReader(FILE);
            Type type = new TypeToken<Map<String, Map<String, Object>>>(){}.getType();
            Map<String, Map<String, Object>> data = GSON.fromJson(reader, type);
            reader.close();
            if (data == null) return;

            for (Module m : mm.modules) {
                Map<String, Object> modData = data.get(m.getName());
                if (modData == null) continue;

                Object enabled = modData.get("enabled");
                if (enabled instanceof Boolean) m.setEnabled((Boolean) enabled);

                Object key = modData.get("key");
                if (key instanceof Number) m.setKey(((Number) key).intValue());

                for (Setting s : m.getSettings()) {
                    Object val = modData.get(s.getName());
                    if (val == null) continue;
                    if (s instanceof NumberSetting) {
                        if (val instanceof Number) ((NumberSetting) s).setValue(((Number) val).doubleValue());
                    } else if (s instanceof ModeSetting) {
                        if (val instanceof String) ((ModeSetting) s).setValue((String) val);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save(ModuleManager mm) {
        try {
            FILE.getParentFile().mkdirs();
            Map<String, Map<String, Object>> data = new HashMap<String, Map<String, Object>>();

            for (Module m : mm.modules) {
                Map<String, Object> modData = new HashMap<String, Object>();
                modData.put("enabled", m.isEnabled());
                modData.put("key", m.getKey());
                for (Setting s : m.getSettings()) {
                    if (s instanceof NumberSetting) {
                        modData.put(s.getName(), ((NumberSetting) s).getValue());
                    } else if (s instanceof ModeSetting) {
                        modData.put(s.getName(), ((ModeSetting) s).getValue());
                    }
                }
                data.put(m.getName(), modData);
            }

            Writer writer = new FileWriter(FILE);
            GSON.toJson(data, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
