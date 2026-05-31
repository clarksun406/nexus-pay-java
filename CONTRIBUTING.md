# Contributing to NexusPay

## Development Setup

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL 16+
- Docker (optional)

### Getting Started

```bash
# Clone
git clone https://github.com/nexuspay/nexus-pay-java.git
cd nexus-pay-java

# Backend
mvn clean install
cd nexuspay-web && mvn spring-boot:run

# Frontend
cd frontend-dashboard && npm install && npm run dev
```

---

## Project Structure

```
nexuspay-java/
鈹溾攢鈹€ nexuspay-common/      # Common utilities
鈹溾攢鈹€ nexuspay-domain/      # Domain entities
鈹溾攢鈹€ nexuspay-repository/  # Data access
鈹溾攢鈹€ nexuspay-service/     # Business logic
鈹溾攢鈹€ nexuspay-web/         # REST API
鈹溾攢鈹€ frontend-dashboard/             # Merchant dashboard
鈹溾攢鈹€ frontend-admin/       # Admin portal
鈹溾攢鈹€ frontend-nexuspay-js/          # Element SDK
鈹斺攢鈹€ docs/                 # Documentation
```

---

## Coding Standards

### Java
- Follow Google Java Style
- Use Lombok for boilerplate
- Write unit tests for services
- Use meaningful variable names

### TypeScript/Vue
- Use Composition API
- Follow Vue 3 style guide
- Use TypeScript strict mode

---

## Commit Messages

Format: `type(scope): description`

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance

Example:
```
feat(payment): add Apple Pay support
fix(routing): fix weighted selection bug
docs(api): update API reference
```

---

## Pull Request Process

1. Create feature branch from `main`
2. Make changes with tests
3. Run tests: `mvn test`
4. Update documentation if needed
5. Submit PR with description

---

## Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Frontend tests
cd frontend-dashboard && npm run test
```

---

## Need Help?

Open an issue or start a discussion on GitHub.


