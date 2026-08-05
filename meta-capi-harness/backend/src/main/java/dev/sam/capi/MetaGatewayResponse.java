package dev.sam.capi;

public record MetaGatewayResponse(boolean accepted, int httpStatus, String summary) {
}
