package com.ecommerce.storage;

import com.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalStorageServiceTest {

    /** @TempDir: o JUnit cria uma pasta temporária e apaga ao final do teste. */
    @TempDir
    Path tempDir;

    private LocalStorageService storage;

    @BeforeEach
    void setUp() {
        storage = new LocalStorageService(tempDir.toString());
    }

    @Test
    void shouldStoreLoadAndDelete() throws Exception {
        String key = storage.store(new byte[]{1, 2, 3}, "png");

        assertTrue(key.endsWith(".png"));
        assertArrayEquals(new byte[]{1, 2, 3}, storage.load(key).getContentAsByteArray());

        storage.delete(key);

        assertFalse(Files.exists(tempDir.resolve(key)));
    }

    @Test
    void shouldRejectPathTraversalKeys() {
        assertThrows(ResourceNotFoundException.class, () -> storage.load("../../etc/passwd"));
        assertThrows(ResourceNotFoundException.class, () -> storage.load("..\\secret.png"));
        assertThrows(ResourceNotFoundException.class, () -> storage.load(null));
    }
}
