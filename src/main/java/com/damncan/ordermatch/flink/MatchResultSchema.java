package com.damncan.ordermatch.flink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;

import java.io.IOException;

/**
 * This class defines serializer and deserializer for flink to consume/produce data from/into kafka.
 *
 * @author Ian Zhong (damncan)
 * @since 18 September 2022
 */
public class MatchResultSchema implements DeserializationSchema<ObjectNode>, SerializationSchema<ObjectNode> {

    private ObjectMapper mapper;

    @Override
    public ObjectNode deserialize(byte[] message) throws IOException {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        ObjectNode node = mapper.createObjectNode();
        if (message != null) {
            node.set("value", mapper.readValue(message, JsonNode.class));
        }
        return node;
    }

    @Override
    public byte[] serialize(ObjectNode myMessage) {
        return myMessage.toString().getBytes();
    }

    @Override
    public TypeInformation<ObjectNode> getProducedType() {
        return TypeExtractor.getForClass(ObjectNode.class);
    }

    @Override
    public boolean isEndOfStream(ObjectNode myMessage) {
        return false;
    }
}
