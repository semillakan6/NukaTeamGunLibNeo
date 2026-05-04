package com.nukateam.ntgl.modules.data;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;

/**
 * Holds synced booleans separately for logical server vs logical client storage so integrated-server + optimistic
 * client updates cannot cross-contaminate (shared JVM previously merged pendingSync into {@link DataKeyManager} broadcasts).
 */
public class DataKey {
    private final HashMap<Integer, DataEntry> serverData = new HashMap<>();
    private final HashMap<Integer, DataEntry> clientData = new HashMap<>();

    private final boolean defaultValue;

    public DataKey(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    private HashMap<Integer, DataEntry> mapFor(LivingEntity entity) {
        return entity.level().isClientSide() ? clientData : serverData;
    }

    public boolean getValue(LivingEntity entity) {
        var entry = mapFor(entity).get(entity.getId());
        if (entry != null) {
            return entry.getValue();
        }
        return defaultValue;
    }

    public void setValue(LivingEntity entity, boolean value) {
        put(mapFor(entity), entity.getId(), value);
    }

    /** Apply values from {@link com.nukateam.ntgl.modules.data.message.S2CMessageUpdateEntityData} on the physical client only. */
    public void setSyncedClientValue(int entityId, boolean value) {
        put(clientData, entityId, value);
    }

    private void put(HashMap<Integer, DataEntry> map, int id, boolean value) {
        if (map.containsKey(id)) {
            map.get(id).setValue(value);
        } else {
            var dataEntry = new DataEntry();
            dataEntry.setValue(value);
            map.put(id, dataEntry);
        }
    }

    /** Pending outbound sync entries (logical server only). */
    public HashMap<Integer, DataEntry> getServerData() {
        return serverData;
    }
}
