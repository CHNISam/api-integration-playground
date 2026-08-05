package dev.sam.webhook;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, Long> {
    List<DeliveryAttempt> findTop50ByOrderByReceivedAtDesc();
}
