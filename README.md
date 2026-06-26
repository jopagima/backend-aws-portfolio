Here is the comprehensive **README.md** for your project, written in professional English with a focus on your transition toward a **Senior Cloud Architect** role.

---

# 🚀 AWS Serverless Portfolio Backend

This repository serves as the core of my **Roadmap to Senior Backend & Cloud Architect**. The primary objective is to demonstrate a successful transition from a solid foundation in enterprise development (Java/Spring) to highly scalable, **Serverless Native** architectures on AWS using **AWS CDK** as the Infrastructure as Code (IaC) engine [3, Previous Conversation].

## 🎯 Project Goals

This development is not just a functional application but a practical demonstration of modern architectural standards. The project aims to build a microservices ecosystem that solves real-world problems by applying:

*   **Serverless First:** Minimizing infrastructure management by leveraging AWS Lambda and Amazon API Gateway for automatic, cost-efficient scaling.
*   **Infrastructure as Code (IaC):** Using **AWS CDK v2** to define the entire infrastructure through Java code, ensuring repeatable and versionable deployments [585, Previous Conversation].
*   **SOLID Principles & Clean Architecture:** Applying enterprise design patterns, such as **Custom Constructs**, to ensure the system is modular, maintainable, and easy to test [Previous Conversation].
*   **Senior-Level Observability:** Native integration with **AWS X-Ray** and **CloudWatch** to obtain end-to-end traceability in distributed systems [404, 413, Previous Conversation].

## 🏗️ System Architecture (Overview)

The application follows a decoupled microservices model:
1.  **Entry Point:** REST API managed by **Amazon API Gateway**.
2.  **Compute:** Business logic executed in **AWS Lambda** using **Java 21**.
3.  **SOLID Infrastructure:** Resources are encapsulated into **Custom Constructs** (`StatusLambdaConstruct`, `PortfolioApiConstruct`) to decouple the API from the Lambda and improve reusability [Previous Conversation].
4.  **Clean Backend:** A strict separation between the **Handler** (AWS event entry) and the **Service Layer** (pure business logic) allows for **Unit Testing with JUnit 5** without cloud dependencies [Previous Conversation].

## 📂 Repository Structure

```text
.
├── src/main/java/com/myorg/
│   ├── BackendAwsPortfolioApp.java    # CDK Application entry point
│   ├── BackendAwsPortfolioStack.java  # Main infrastructure orchestrator
│   └── constructs/                    # SOLID building blocks (Layer 3 Constructs)
├── services/                          # Microservices Logic (Backend)
│   └── status-lambda/                 # System status microservice
│       ├── src/main/java/             # Java 21: Handler + Service Layer
│       └── pom.xml                    # Maven dependencies
├── cdk.json                           # CDK Toolkit configuration
└── pom.xml                            # Root Maven project (Infrastructure)
```

## 📈 Development Progress (Roadmap)

Currently, we are completing **Day 5** of a continuous evolution plan [3, Previous Conversation]:

*   [x] **Base Configuration:** Development environment setup, CLI, security profiles, and CDK bootstrapping [Previous Conversation].
*   [x] **Status Microservice:** Creation and deployment of the first Lambda function in Java 21 [Previous Conversation].
*   [x] **Public Exposure:** Configuration of API Gateway with proxy integration and CORS management [Previous Conversation].
*   [x] **SOLID Refactoring:** Migration to **Custom Constructs** and separation of logic into an independent **Service** layer [Previous Conversation].
*   [ ] **Observability:** Activation of **AWS X-Ray** for distributed tracing [421, Previous Conversation].
*   [ ] **Persistence (Next Step):** Implementation of **Amazon DynamoDB** for high-performance NoSQL data storage [551, Previous Conversation].

## 🛠️ Development Commands

**For the Backend (Java):**
Generate the artifact (Fat JAR) before deployment:
```bash
cd services/status-lambda && mvn clean package
```

**For the Infrastructure (CDK):**
Run these from the project root:
*   `cdk synth`: Synthesize the CloudFormation template for local validation.
*   `cdk deploy --profile portfolio`: Deploy the entire infrastructure to your AWS account.
*   `cdk diff`: Compare the local state with the deployed cloud resources.

---
*This project demonstrates the technical capability to transform Java Enterprise knowledge into modern, scalable cloud solutions.* [Previous Conversation].
