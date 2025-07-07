package com.colby.roommate.controller;

import com.colby.roommate.model.Purchase;
import com.colby.roommate.repo.PurchaseRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {
  private final PurchaseRepository repo;
  public PurchaseController(PurchaseRepository repo) { this.repo = repo; }

  @GetMapping List<Purchase> all()       { return repo.findAll(); }
  @PostMapping Purchase create(@RequestBody Purchase p) { return repo.save(p); }
  @PutMapping("/{id}") Purchase update(
      @PathVariable Long id, @RequestBody Purchase p) {
    p.setId(id);
    return repo.save(p);
  }
  @DeleteMapping("/{id}") void delete(@PathVariable Long id) {
    repo.deleteById(id);
  }
}
