package dev.molkovor.productservice.domain.service;

import dev.molkovor.productservice.api.ProductCreateRequest;
import dev.molkovor.productservice.api.ProductUpdateRequest;
import dev.molkovor.productservice.domain.db.ProductEntity;
import dev.molkovor.productservice.domain.db.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpringAnnotationCachingProductService implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductEntity create(ProductCreateRequest createRequest) {
        log.info("Saving product to DB with name={}", createRequest.name());
        ProductEntity product = ProductEntity.builder()
                .name(createRequest.name())
                .price(createRequest.price())
                .description(createRequest.description())
                .build();
        return productRepository.save(product);
    }

    @CacheEvict(
            value = "product",
            key = "#id",
            beforeInvocation = true
    )
    @Override
    public ProductEntity update(Long id, ProductUpdateRequest updateRequest) {
        log.info("Updating product in DB with id={}", id);
        ProductEntity productToUpdate = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with id=%s not found".formatted(id)));

        if (updateRequest.price() != null) {
            productToUpdate.setPrice(updateRequest.price());
        }
        if (updateRequest.description() != null) {
            productToUpdate.setDescription(updateRequest.description());
        }

        return productRepository.save(productToUpdate);
    }

    @Cacheable(
            value = "product",
            key = "#id"
    )
    @Override
    public ProductEntity getById(Long id) {
        log.info("Getting product from DB with id={}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with id=%s not found".formatted(id)));
    }

    @CacheEvict(
            value = "product",
            key = "#id"
    )
    @Override
    public void delete(Long id) {
        log.info("Deleting product from DB with id={}", id);
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product with id=%s not found".formatted(id));
        }
        productRepository.deleteById(id);
    }

    @Override
    public List<ProductEntity> getAll() {
        log.info("Retrieving all products from DB");
        return productRepository.findAll();
    }

}
