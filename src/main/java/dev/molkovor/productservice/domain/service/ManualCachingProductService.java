package dev.molkovor.productservice.domain.service;

import dev.molkovor.productservice.api.ProductCreateRequest;
import dev.molkovor.productservice.api.ProductUpdateRequest;
import dev.molkovor.productservice.domain.db.ProductEntity;
import dev.molkovor.productservice.domain.db.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManualCachingProductService implements ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, ProductEntity> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "product:";
    private static final long CACHE_TTL_MINUTES = 1;

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

    @Override
    public ProductEntity update(Long id, ProductUpdateRequest updateRequest) {

        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
        log.info("Cache invalidated for updated product: id={}", id);

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

    @Override
    public ProductEntity getById(Long id) {
        log.info("Getting product with id={}", id);
        String cacheKey = CACHE_KEY_PREFIX + id;

        ProductEntity entityFromCache = redisTemplate.opsForValue().get(cacheKey);

        if (entityFromCache != null) {
            log.info("Found product with id={} in cache", id);
            return entityFromCache;
        }

        log.info("Not found product with id={} in cache", id);
        ProductEntity entityFromDb = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with id=%s not found".formatted(id)));

        redisTemplate.opsForValue()
                .set(cacheKey, entityFromDb, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("Product with id={} was cached", id);

        return entityFromDb;
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting product from DB with id={}", id);
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product with id=%s not found".formatted(id));
        }

        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
        log.info("Cache invalidated for deleted product: id={}", id);

        productRepository.deleteById(id);
    }

    @Override
    public List<ProductEntity> getAll() {
        log.info("Retrieving all products from DB");
        return productRepository.findAll();
    }
}
