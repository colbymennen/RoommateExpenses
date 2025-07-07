package com.colby.roommate.repo;

import com.colby.roommate.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase,Long> { }
