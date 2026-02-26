package dev.molkovor.productservice.api;

import dev.molkovor.productservice.domain.db.ProductEntity;
import dev.molkovor.productservice.domain.service.DbProductService;
import dev.molkovor.productservice.domain.service.ProductService;
import dev.molkovor.productservice.locking.RedisLockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products/lock")
public class ProductUpdateLockingController {

    private final RedisLockManager redisLockManager;
    private final DbProductService dbProductService;
    private final ProductDtoMapper productDtoMapper;


    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProductWithLock(
            @PathVariable("id") Long id,
            @RequestBody ProductUpdateRequest request,
            @RequestParam(defaultValue = "500") long timeout) {

        log.info("Updating product with locking: id={}", id);
        String lockKey = "product:" + id;

        String lockId = redisLockManager.tryLock(lockKey, Duration.ofMinutes(1));
        if (lockId == null) {
            throw new ResponseStatusException(
                    HttpStatus.LOCKED,
                    "Блокировка захвачена для объекта %s. Пробуйте позже".formatted(lockKey)
            );
        }

        try {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            ProductEntity productEntity = dbProductService.update(id, request);
            ProductDto productDto = productDtoMapper.toDto(productEntity);
            log.info("Product was updated successfully: id={}", id);
            return ResponseEntity.ok(productDto);
        } finally {
            redisLockManager.unlockLock(lockKey, lockId);
        }
    }

}
