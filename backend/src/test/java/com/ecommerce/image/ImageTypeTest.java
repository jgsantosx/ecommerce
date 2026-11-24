package com.ecommerce.image;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ImageTypeTest {

    static final byte[] PNG_HEADER = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};

    @Test
    void shouldDetectByMagicBytes() {
        assertEquals(ImageType.PNG, ImageType.detect(PNG_HEADER).orElseThrow());
        assertEquals(ImageType.JPEG, ImageType.detect(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0}).orElseThrow());

        byte[] webp = "RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1);
        assertEquals(ImageType.WEBP, ImageType.detect(webp).orElseThrow());
    }

    @Test
    void shouldRejectNonImagesEvenIfNamedLikeImages() {
        byte[] script = "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);
        byte[] exe = {'M', 'Z', 0, 0};

        assertTrue(ImageType.detect(script).isEmpty());
        assertTrue(ImageType.detect(exe).isEmpty());
        assertTrue(ImageType.detect(new byte[0]).isEmpty());
    }
}
