# Cloud-Native Enterprise ERP System

A containerized, scalable ERP platform built with a microservices-inspired architecture. The system provides integrated modules for Sales, Purchasing, Inventory, and Administrative management, deployed using modern DevOps practices on AWS.

## Live Demo
**URL:** [https://dw6bn4yx1l0j2.cloudfront.net/Registration.html](https://dw6bn4yx1l0j2.cloudfront.net/Registration.html)  

## Tech Stack
- **Backend:** Java 17, Spring Boot 3.3, Spring Security (JWT + RBAC), JPA/Hibernate
- **Frontend:** React (Buildless JSX), Vanilla CSS, Babel Standalone
- **Database:** Amazon RDS (PostgreSQL)
- **Infrastructure:** AWS ECS (Fargate), ECR, ALB, CloudFront, S3
- **CI/CD:** GitHub Actions

## Architecture
- **Micro-Modules:** Separate ERP-Main and Admin services containerized via Docker.
- **Routing:** AWS CloudFront acts as a global entry point, routing traffic to an Application Load Balancer (ALB) and static UI assets.
- **Security:** Stateless JWT-based authentication with fine-grained Role-Based Access Control (RBAC).

## Getting Started
1. Clone the repository.
2. Configure environment variables (DB_URL, DB_USERNAME, DB_PASSWORD).
3. Build using Maven: `./mvnw clean package`.
4. Run locally: `./mvnw spring-boot:run`.

## CI/CD Pipeline
Automatic deployments are triggered on every push to the Dev branch:
1. **Build:** Maven compilation & Docker image generation.
2. **Push:** Images stored in Amazon ECR.
3. **Deploy:** ECS Task Definitions updated for zero-downtime rolling deployments via AWS Fargate.

