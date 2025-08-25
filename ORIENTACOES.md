# Orientações — Desafio Pessoa Desenvolvedora Java (ESIG)

## O que foi implementado
- Consolidação de salários (créditos somam, débitos subtraem) e preenchimento da tabela `pessoa_salario_consolidado`.
- Tela JSF com listagem paginada, filtros e **recalcular salários**.
- Relatórios **JasperReports** (PDF) respeitando filtros/ordenação.
- Autenticação com **BCrypt**, controle de sessão única e rotas **Admin-only**.
- CRUDs de Pessoa e Usuário (soft delete para Pessoa).
- Testes (JUnit + Mockito + JaCoCo) cobrindo serviços, relatórios e auth.

## Como executar localmente
1. **Banco via Docker**
   ```bash
   ./init.sh
```
2. **Build e testes**
```
mvn clean verify
```
Gera target/esig-teste.war e relatório JaCoCo em target/site/jacoco.
3. **Deploy**
Tomcat: copie o .war para webapps (ou use a view Servers do Eclipse).
Acesse: http://localhost:8080/esig-teste/
4. **Acesso**

**Email**: admin@esig.com.br

**Senha**: admin

O README traz visão geral, stack, e como rodar. Este arquivo resume implementações e passos.

## Stack
Java 11+, Maven, JSF/PrimeFaces, JPA/Hibernate, PostgreSQL (Docker), JasperReports, JUnit/Mockito, GitHub Actions.