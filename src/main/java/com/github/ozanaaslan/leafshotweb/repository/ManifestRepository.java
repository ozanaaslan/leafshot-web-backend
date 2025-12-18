package com.github.ozanaaslan.leafshotweb.repository;

import com.github.ozanaaslan.leafshotweb.util.Manifest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManifestRepository extends JpaRepository<Manifest, String> {
    List<Manifest> findByDeletedFalseAndExpirationTimestampLessThan(long timestamp);
    List<Manifest> findByDeletedFalseAndEarmarkedForRemovalTrue();
}
