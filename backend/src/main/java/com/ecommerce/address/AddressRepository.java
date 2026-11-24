package com.ecommerce.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByDefaultAddressDescCreatedAtDesc(Long userId);

    /**
     * Busca SEMPRE filtrando pelo dono. Se o endereço for de outro usuário,
     * o resultado é vazio (404) — não revelamos que o id existe.
     */
    Optional<Address> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserId(Long userId);

    Optional<Address> findFirstByUserIdAndIdNotOrderByCreatedAtDesc(Long userId, Long id);

    /**
     * UPDATE em lote. flushAutomatically envia ao banco as mudanças pendentes antes;
     * clearAutomatically limpa o cache do Hibernate depois, para não ler dados velhos.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Address a SET a.defaultAddress = false WHERE a.user.id = :userId AND a.defaultAddress = true")
    void clearDefaultForUser(@Param("userId") Long userId);
}
