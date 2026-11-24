package com.ecommerce.address;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(
            AddressRepository addressRepository,
            UserRepository userRepository
    ) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> findAll(Long userId) {
        return addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId)
                .stream()
                .map(AddressResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse findById(Long userId, Long id) {
        return AddressResponse.fromEntity(getOwned(userId, id));
    }

    /**
     * Também usado pelo módulo de pedidos para obter o endereço de entrega.
     * Sem @Transactional próprio: roda dentro da transação de quem chama.
     */
    public Address getOwned(Long userId, Long id) {
        return addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço não encontrado: " + id
                ));
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest request) {
        // O primeiro endereço do usuário vira o principal automaticamente.
        boolean isFirst = !addressRepository.existsByUserId(userId);
        boolean makeDefault = isFirst || request.isDefaultAddress();

        if (makeDefault) {
            addressRepository.clearDefaultForUser(userId);
        }

        Address address = new Address(userRepository.getReferenceById(userId));
        address.apply(request);
        address.setDefaultAddress(makeDefault);

        return AddressResponse.fromEntity(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long userId, Long id, AddressRequest request) {
        Address address = getOwned(userId, id);
        address.apply(request);

        // Desmarcar o principal pelo update não é permitido (sempre deve existir um);
        // para trocar, marque outro endereço como principal.
        if (request.isDefaultAddress() && !address.isDefaultAddress()) {
            return setDefault(userId, id);
        }

        return AddressResponse.fromEntity(address);
    }

    @Transactional
    public AddressResponse setDefault(Long userId, Long id) {
        getOwned(userId, id);

        // Primeiro desmarcamos o atual (UPDATE em lote), depois marcamos o novo.
        // A ordem importa por causa do índice único parcial no banco.
        addressRepository.clearDefaultForUser(userId);

        Address address = getOwned(userId, id);
        address.setDefaultAddress(true);

        return AddressResponse.fromEntity(address);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Address address = getOwned(userId, id);
        boolean wasDefault = address.isDefaultAddress();

        addressRepository.delete(address);

        // Se o principal foi removido, promovemos o mais recente que sobrou.
        if (wasDefault) {
            addressRepository.flush();
            addressRepository.findFirstByUserIdAndIdNotOrderByCreatedAtDesc(userId, id)
                    .ifPresent(next -> next.setDefaultAddress(true));
        }
    }
}
