package dev.sam.capi;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseEventRepository extends JpaRepository<PurchaseEvent, Long> {
    List<PurchaseEvent> findTop20ByOrderByLastAttemptAtDesc();
}
