package com.ecommerce.image;

import java.util.Arrays;
import java.util.Optional;

/**
 * Tipos de imagem aceitos, identificados pelos "magic bytes" — os primeiros
 * bytes do arquivo, que indicam seu formato real.
 *
 * Por que não confiar no Content-Type ou na extensão enviados? Porque o cliente
 * controla os dois: alguém poderia enviar um executável chamado "foto.png".
 */
public enum ImageType {

    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String contentType;
    private final String extension;

    ImageType(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public static Optional<ImageType> detect(byte[] bytes) {
        return Arrays.stream(values())
                .filter(type -> type.matches(bytes))
                .findFirst();
    }

    private boolean matches(byte[] b) {
        return switch (this) {
            case JPEG -> startsWith(b, 0, 0xFF, 0xD8, 0xFF);
            case PNG -> startsWith(b, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A);
            case WEBP -> startsWith(b, 0, 'R', 'I', 'F', 'F') && startsWith(b, 8, 'W', 'E', 'B', 'P');
        };
    }

    private static boolean startsWith(byte[] bytes, int offset, int... expected) {
        if (bytes.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((bytes[offset + i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }
}
