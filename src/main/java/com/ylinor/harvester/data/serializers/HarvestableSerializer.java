package com.ylinor.harvester.data.serializers;

import com.google.common.reflect.TypeToken;
import com.ylinor.harvester.data.beans.HarvestableBean;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.List;

public class HarvestableSerializer implements TypeSerializer<HarvestableBean> {

    @Override
    public HarvestableBean deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        String blocType = value.getNode("type").getString();
        int respawnMin = value.getNode("respawnmin").getInt();
        int respawnMax = value.getNode("respawnmax").getInt();
        return new HarvestableBean(blocType, respawnMin, respawnMax);
    }

    @Override
    public void serialize(TypeToken<?> type, HarvestableBean obj, ConfigurationNode value) throws ObjectMappingException {

    }
}