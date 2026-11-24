package com.ecommerce.storage;

import com.ecommerce.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    /** Só aceitamos chaves no formato que nós mesmos geramos: <uuid>.<ext>. */
    private static final Pattern KEY_PATTERN =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.[a-z]{3,4}");

    private final Path baseDir;

    public LocalStorageService(@Value("${app.storage.local.base-dir}") String baseDir) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar o diretório de upload: " + this.baseDir, e);
        }
    }

    @Override
    public String store(byte[] content, String extension) {
        String key = UUID.randomUUID() + "." + extension;

        try {
            Files.write(resolve(key), content);
            return key;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar arquivo", e);
        }
    }

    @Override
    public Resource load(String key) {
        Path path = resolve(key);

        if (!Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("Arquivo não encontrado");
        }

        return new PathResource(path);
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            // Um arquivo órfão no disco é menos grave que falhar a operação inteira.
            log.warn("Não foi possível apagar o arquivo {}", key, e);
        }
    }

    /**
     * Defesa contra "path traversal" (ex.: chave "../../etc/passwd"):
     * validamos o formato da chave e garantimos que o caminho final continua
     * dentro do diretório base.
     */
    private Path resolve(String key) {
        if (key == null || !KEY_PATTERN.matcher(key).matches()) {
            throw new ResourceNotFoundException("Arquivo não encontrado");
        }

        Path path = baseDir.resolve(key).normalize();

        if (!path.startsWith(baseDir)) {
            throw new ResourceNotFoundException("Arquivo não encontrado");
        }

        return path;
    }
}
