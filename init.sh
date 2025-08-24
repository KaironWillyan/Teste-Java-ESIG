#!/bin/bash
# Script para inicializar o ambiente de desenvolvimento

echo "--- Verificando se o Docker está a correr..."
if ! docker info > /dev/null 2>&1; then
  echo "!!! Docker não parece estar a correr. Por favor, inicie o Docker e tente novamente."
  exit 1
fi

echo "--- A subir o contentor do PostgreSQL com Docker Compose..."
docker-compose up -d

echo ""
echo "--- Ambiente iniciado com sucesso! ---"
echo "O banco de dados PostgreSQL está a correr no localhost, na porta 5432."
echo "As tabelas foram criadas e os dados iniciais foram inseridos."
echo "Agora, pode compilar e executar a sua aplicação Java."