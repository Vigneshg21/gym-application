package com.codexgym.gym.repository;

import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.enums.InvoiceStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findAllByStatusInOrderByDueDateAsc(Collection<InvoiceStatus> statuses);

    List<Invoice> findAllByStatusInAndDueDateLessThanEqualOrderByDueDateAsc(
            Collection<InvoiceStatus> statuses,
            LocalDate dueDate
    );

    long countByStatus(InvoiceStatus status);
}

