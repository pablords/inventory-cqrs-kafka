package com.pablords.query.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import com.pablords.query.model.ProductView;

public interface ProductViewRepository extends MongoRepository<ProductView, String> {
}