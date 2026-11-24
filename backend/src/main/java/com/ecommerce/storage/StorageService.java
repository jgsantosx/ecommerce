package com.ecommerce.storage;

import org.springframework.core.io.Resource;

/**
 * Abstração de armazenamento de arquivos.
 *
 * Hoje: LocalStorageService (disco). Amanhã: S3StorageService, por exemplo.
 * Quem usa (ProductImageService) não sabe — nem precisa saber — onde o arquivo fica.
 */
public interface StorageService {

    /**
     * Grava o conteúdo e devolve uma "chave" opaca que identifica o arquivo.
     * A chave é gerada por nós, nunca a partir do nome enviado pelo usuário.
     */
    String store(byte[] content, String extension);

    Resource load(String key);

    void delete(String key);
}
