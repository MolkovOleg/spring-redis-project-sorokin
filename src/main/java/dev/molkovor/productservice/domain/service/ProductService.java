package dev.molkovor.productservice.domain.service;

import dev.molkovor.productservice.api.ProductCreateRequest;
import dev.molkovor.productservice.api.ProductUpdateRequest;
import dev.molkovor.productservice.domain.db.ProductEntity;

import java.util.List;

public interface ProductService {

    ProductEntity create(ProductCreateRequest createRequest);

    ProductEntity update(Long id, ProductUpdateRequest updateRequest);

    ProductEntity getById(Long id);

    void delete(Long id);

    List<ProductEntity> getAll();
}
