.PHONY: all clean compile test package install run help

all: install

clean:
	@echo "Limpando o projeto..."
	mvn clean

compile:
	@echo "Compilando o código fonte..."
	mvn compile

test:
	@echo "Executando testes..."
	mvn test

package:
	@echo "Gerando o pacote da aplicação..."
	mvn package

install: clean package
	@echo "Instalando o projeto..."
	mvn install

run:
	@echo "Iniciando a aplicação com Docker Compose..."
	docker-compose up -d --build

# Ajuda
help:
	@echo "Comandos disponíveis:"
	@echo "  make all       - Executa clean e install (padrão)"
	@echo "  make clean     - Limpa o diretório target"
	@echo "  make compile   - Compila o projeto"
	@echo "  make test      - Executa os testes"
	@echo "  make package   - Gera o pacote .war"
	@echo "  make install   - Instala o projeto no repositório local"
	@echo "  make run       - Sobe a aplicação com Docker Compose"