package dev.sam.capi;

import java.util.Map;

public interface MetaGateway {
    boolean configured();
    MetaGatewayResponse send(Map<String, Object> event);
}
