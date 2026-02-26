package dev.molkovor.productservice.api;

import dev.molkovor.productservice.domain.CacheMode;
import dev.molkovor.productservice.domain.db.ProductEntity;
import dev.molkovor.productservice.domain.service.DbProductService;
import dev.molkovor.productservice.domain.service.ManualCachingProductService;
import dev.molkovor.productservice.domain.service.ProductService;
import dev.molkovor.productservice.domain.service.SpringAnnotationCachingProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final DbProductService dbProductService;
    private final ManualCachingProductService manualCachingProductService;
    private final SpringAnnotationCachingProductService springAnnotationCachingProductService;
    private final ProductDtoMapper productDtoMapper;


    @PostMapping
    public ResponseEntity<ProductDto> createProduct(
            @RequestBody ProductCreateRequest createRequest,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode) {
        log.info("Creating new product, cacheMode={}", cacheMode);

        ProductService service = resolveProductService(cacheMode);
        ProductEntity productEntity = service.create(createRequest);
        ProductDto productDto = productDtoMapper.toDto(productEntity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(
            @PathVariable("id") Long id,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode) {
        log.info("Getting product by id={} cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);
        ProductEntity productEntity = service.getById(id);
        ProductDto productDto = productDtoMapper.toDto(productEntity);

        return ResponseEntity.ok(productDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable("id") Long id,
            @RequestBody ProductUpdateRequest updateRequest,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode) {
        log.info("Updating product with id={}, cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);
        ProductEntity productEntity = service.update(id, updateRequest);
        ProductDto productDto = productDtoMapper.toDto(productEntity);

        return ResponseEntity.ok(productDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") Long id,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode) {
        log.info("Deleting product with id={}, cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);
        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        log.info("Retrieving all products");
        List<ProductEntity> productEntities = dbProductService.getAll();

        return ResponseEntity
                .ok()
                .body(productEntities.stream()
                        .map(productDtoMapper::toDto)
                        .collect(Collectors.toList()));
    }
    private ProductService resolveProductService(CacheMode cacheMode) {
        return switch (cacheMode) {
            case NONE_CACHE -> dbProductService;
            case MANUAL -> manualCachingProductService;
            case SPRING -> springAnnotationCachingProductService;
        };
    }
}
