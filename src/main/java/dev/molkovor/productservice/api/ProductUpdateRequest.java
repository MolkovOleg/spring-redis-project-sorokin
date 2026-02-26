package dev.molkovor.productservice.api;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        BigDecimal price,
        String description
) {
}
