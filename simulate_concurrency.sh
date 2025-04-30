#!/bin/bash

# Atualiza o estoque do mesmo produto em paralelo
curl -X POST "http://localhost:8080/api/v1/products/a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d/add" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "amount=10" &

curl -X POST "http://localhost:8080/api/v1/products/a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d/add" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "amount=20" &

curl -X POST "http://localhost:8080/api/v1/products/a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d/add" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "amount=30" &

curl -X GET "http://localhost:8081/api/v1/products/a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d" \
     -H "Content-Type: application/x-www-form-urlencoded" &&

curl -X GET "http://localhost:8081/api/v1/products/a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d" \
     -H "Content-Type: application/x-www-form-urlencoded"