# Desafio Pessoa Desenvolvedora Java — ESIG

Aplicação web em **Java/JSF (PrimeFaces)** para **gestão de pessoas** e **consolidação de salários**. Inclui autenticação com controle de sessão única, perfis de acesso (Admin) e **relatórios em PDF** (respeitando filtros e ordenação).

## Visão geral (como funciona)

* **Camada web:** JSF + PrimeFaces (páginas `.xhtml`), navegação com PrettyFaces.
* **Regras de negócio:** serviços de domínio (cálculo e consolidação de salários).
* **Persistência:** JPA/Hibernate sobre **PostgreSQL**.
* **Segurança:**

  * Login com **BCrypt**.
  * **Sessão única** por usuário (logout da sessão anterior ao novo login).
  * **Filtro** protege rotas e áreas **apenas para Admin**.
* **Relatórios:** geração de **PDF** a partir da lista atual da tela.

## Funcionalidades

* CRUD de **Funcionários** (com desativação/soft delete).
* CRUD de **Usuários** (com permissão Admin).
* **Listagens paginadas** com filtros.
* **Relatórios em PDF** para Usuarios, Pessoas e Salarios.
* **Autenticação** + **controle de acesso**.

## Pré-requisitos

* **Java 11+**
* **Maven 3.x**
* **Docker** (para subir o banco rapidamente)
* **PostgreSQL 15** (via Docker)
* Servidor de aplicação **Tomcat 9+** (Wildfly/Jboss, Glassfish, ou similar)

> As credenciais e URL do banco são definidas no `persistence.xml`. Ajuste host/porta/banco/usuário/senha conforme seu ambiente.

---

## Execução — Maven

### 1) Suba o banco com Docker

Na raiz do projeto:

```bash
./init.sh
```

Esse script:

1. Sobe um container **PostgreSQL**
2. Cria o banco `esig`
3. Executa `init-db/init.sql` (DDL + dados iniciais)

### 2) Build do projeto

```bash
mvn clean install
```

Gera `target/esig-teste.war`.

### 3) Deploy no Tomcat

* Copie o `.war` para `TOMCAT_HOME/webapps`, ou
* No **Eclipse**, adicione o projeto ao servidor na view **Servers** e **Start/Debug**.

### 4) Acesse

```
http://localhost:8080/esig-teste/
```

> O contexto `/esig-teste` pode variar conforme o nome do `.war` ou a config do servidor.

**Login padrão**

* **Email:** `admin@esig.com.br`
* **Senha:** `admin`

---

## Executando pelo Eclipse

1. **Importar**: `File > Import > Maven > Existing Maven Projects`
2. **Atualizar deps**: botão direito no projeto → `Maven > Update Project... (Force Update)`
3. **Configurar servidor**: view **Servers** → adicione um **Tomcat 9+**
4. **Deploy**: `Add and Remove...` → mova o projeto para **Configured**
5. **Start/Debug** o servidor

---

## Relatórios (PDF)

* O botão **Gerar PDF** usa os **filtros** da tela.
* É possível **baixar** o PDF ou **abrir em outra aba** (ajustando `Content-Disposition` para `attachment` ou `inline`, conforme sua preferência).

---

## Estrutura

```
src/main/java
  br.com.esig.domain       // modelos de domínio
  br.com.esig.application  // serviços (regras de negócio)
  br.com.esig.managedbeans // beans JSF (view)
  br.com.esig.infra        // filtros, listeners, utilitários, relatórios

src/main/webapp
  *.xhtml                  // páginas JSF/PrimeFaces
  WEB-INF/web.xml          // mapeamentos
  pretty-config.xml        // rotas amigáveis
```

---

## Dicas & Problemas comuns

* **Porta 5432 ocupada**: pare outros Postgres locais ou mude a porta no `init.sh`/compose.
* **Credenciais do banco**: se o login falhar, ajuste `persistence.xml` (URL/usuário/senha).
* **Context path**: se a URL não abrir, confirme o nome do `.war` ou o contexto configurado.
* **Permissões Admin**: áreas restritas exigem usuário com `isAdmin=true`.


## Licença

Projeto para fins de avaliação técnica (ESIG). Use e adapte conforme necessário.

