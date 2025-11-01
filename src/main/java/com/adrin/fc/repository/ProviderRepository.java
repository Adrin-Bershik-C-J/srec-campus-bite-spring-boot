package com.adrin.fc.repository;

import com.adrin.fc.entity.Provider;
import com.adrin.fc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findByUser(User user);
    
    List<Provider> findByActiveTrue();
}
