package com.adrin.fc.repository;

import com.adrin.fc.entity.MenuItem;
import com.adrin.fc.entity.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findByProvider(Provider provider, Pageable pageable);
}
