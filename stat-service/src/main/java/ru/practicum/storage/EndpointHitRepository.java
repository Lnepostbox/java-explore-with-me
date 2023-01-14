package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EndpointHit;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    List<EndpointHit> findAllByTimestampBetweenAndUriIn(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "SELECT DISTINCT ON(ip) * FROM endpoint_hit  WHERE timestamp BETWEEN ?1 AND ?2 ", nativeQuery = true)
    List<EndpointHit> findAllByUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT DISTINCT ON(ip) * FROM endpoint_hit  WHERE timestamp BETWEEN ?1 AND ?2 AND uri IN(?3)",
            nativeQuery = true)
    List<EndpointHit> findAllByUrisAndUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    List<EndpointHit> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT DISTINCT e.uri, COALESCE(r.count_hited, 0) AS count_hited " +
            "FROM endpoint_hit e " +
            "LEFT JOIN (SELECT uri, COUNT(*) AS count_hited FROM endpoint_hit GROUP BY uri) r ON r.uri = e.uri " +
            " WHERE e.uri IN ?1",
            nativeQuery = true)
    List<Object[]> getCountHitByUriList(List<String> uris);

}
