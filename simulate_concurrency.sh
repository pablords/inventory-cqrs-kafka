#!/bin/bash

amounts=(10 20 30)
pids=()

for amount in "${amounts[@]}"; do
  {
    response=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/v1/products/a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d/add" \
      -H "Content-Type: application/json" \
      -d "{\"amount\": $amount}")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    if [[ "$http_code" == "200" || "$http_code" == "201" ]]; then
      echo -e "\033[1;32m[SUCCESS]\033[0m Request add amount=$amount: Estoque alterado com sucesso. Resposta: $body"
    else
      echo -e "\033[1;31m[ERROR]\033[0m Request add amount=$amount: Falha ao alterar estoque. Status: $http_code. Resposta: $body"
    fi
  } &
  pids+=($!)
done

for pid in "${pids[@]}"; do
  wait $pid
done

echo -e "\nConsulta do modelo de leitura após concorrência:"
curl -s -X GET "http://localhost:8081/api/v1/products/a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d" \
     -H "Content-Type: application/json" | jq .

# echo -e "\nCriando novo pedido:"
# curl -s -X POST "http://localhost:8080/api/v1/orders" \
#      -H "Content-Type: application/json" \
#      -d "{\"productId\": \"a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d\", \"quantity\": 5}" | jq .