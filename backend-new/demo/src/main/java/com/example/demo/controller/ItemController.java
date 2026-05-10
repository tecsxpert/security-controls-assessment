package com.example.demo.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @GetMapping("/{id}")
    public Map<String, Object> getItem(@PathVariable Long id) {
        return Map.of(
            "id", id,
            "name", "Sample Item",
            "status", "ACTIVE"
        );
    }

    @DeleteMapping("/{id}")
    public String deleteItem(@PathVariable Long id) {
        return "Deleted item " + id;
    }
}