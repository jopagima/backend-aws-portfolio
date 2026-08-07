# 🚀 AWS Serverless Portfolio: Decoupled Event-Driven Backend

This repository represents the core of my **Roadmap to Senior Backend & Cloud Architect**. It documents a professional transition from enterprise Java/Spring foundations to **Serverless Native** distributed systems on AWS, using **AWS CDK** as the Infrastructure as Code (IaC) engine.

## 🎯 Project Goals

The objective is to demonstrate high-level architectural patterns through a practical microservices ecosystem:

*   **Serverless First:** Zero infrastructure management using AWS Lambda and Amazon API Gateway for cost-efficient, automatic scaling.
*   **Decoupled Resilience (Event-Driven):** Implementing **Amazon SQS** as a buffer to separate API ingestion from heavy persistence logic, ensuring high availability and fault tolerance [9, 396, Historial].
*   **SOLID Infrastructure:** Utilizing **Custom Constructs** to encapsulate cloud resources, ensuring the codebase remains modular, reusable, and versionable.
*   **Senior-Level Observability:** Full distributed tracing with **AWS X-Ray** and structured logging in **CloudWatch**, moving beyond basic monitoring into proactive auditing.

## 🏗️ System Architecture (Event-Driven)

The system follows a fully decoupled, asynchronous flow:

1.  **Entry Point:** **Amazon API Gateway** receives RESTful requests, validated against **Amazon Cognito** JWT tokens.
2.  **Producer (Status Service):** An AWS Lambda (**Java 21**) extracts user identity and immediately pushes an event to an **SQS Queue**. This minimizes API latency by not waiting for DB writes [92, Historial].
3.  **The Resilient Bridge:** **Amazon SQS** stores messages securely, decoupling the Producer from the Consumer and providing a Dead Letter Queue (DLQ) for error handling [396, Historial].
4.  **Consumer (Worker Service):** A second Lambda function is triggered by SQS events, processing messages in batches for cost optimization.
5.  **Persistence Layer:** Data is persisted in **Amazon DynamoDB**, a high-performance NoSQL store.

## 🔍 Observability & Distributed Tracing

As a **Senior Architect** requirement, the system is instrumented for deep visibility:

*   **Active Tracing:** Enabled across the entire stack via **AWS X-Ray**, providing a visual **Service Map** of the request journey.
*   **Indexed Annotations:** The `WorkerService` implements custom X-Ray annotations (e.g., `PortfolioUserId`). This allows for sub-second filtering of specific user traces within the AWS Console [Historial].
*   **Structured Logging:** Correlated logs between the Producer and Consumer via **CloudWatch Logs Insights**, enabling complex queries to debug Jackson parsing or IAM permission issues.

## ⚙️ Continuous Delivery (CI/CD Pipeline)

As of **Day 14**, infrastructure and application changes are no longer deployed manually. The project is driven by a **self-mutating CDK Pipeline** (`PipelineConstruct`), fully defined as code:

*   **Source Stage:** The pipeline tracks the `master` branch on GitHub, authenticating via a token stored in **AWS Secrets Manager** (never hardcoded).
*   **Synth Stage:** A dedicated **AWS CodeBuild** project (Amazon Linux 2023, Corretto 21 + Node.js 20) builds both microservices' Fat JARs with Maven and then runs `cdk synth` to produce the CloudFormation templates.
*   **Self-Mutation:** Because the pipeline is defined via CDK, it can safely update its own stages whenever `PipeLineStack` changes, without manual intervention in the AWS Console.
*   **Explicit Runtime Pinning:** Build environment versions (Java, Node.js, CDK CLI) are pinned explicitly rather than relying on image defaults, avoiding silent breakage from upstream image updates.

## 📈 Roadmap Progress

Currently completing the **CI/CD Automation Phase (Day 14)** [Roadmap]:

- [x] **Base Configuration:** CDK Bootstrapping, CLI profiles, and security setup.
- [x] **Status Microservice:** Implementation of the first Lambda Producer.
- [x] **Security Integration:** Cognito User Pools and API Gateway Authorizers.
- [x] **Asynchronous Flow:** SQS integration and Worker Lambda implementation.
- [x] **Data Persistence:** DynamoDB schema design and Repository pattern implementation.
- [x] **Advanced Observability:** X-Ray SDK instrumentation and custom indexed annotations.
- [x] **CI/CD Automation:** Automated pipelines for multi-account deployments.

## 🛠️ Development & Deployment

### Backend Modules (Java 21)
Build the standalone artifacts (Fat JARs) using Maven:
```bash
# Build Status Producer
cd services/status-lambda && mvn clean package

# Build Access Worker
cd services/worker-lambda && mvn clean package
```

### Infrastructure (AWS CDK v2)
Manage the cloud stack from the root directory:
*   `cdk synth`: Synthesize the CloudFormation template for local validation.
*   `cdk deploy`: Deploy the complete stack, including SQS triggers and IAM Least Privilege roles.
*   `cdk diff`: Inspect changes between local code and the deployed environment.

---

### 🧠 Architect's Note: Design Trade-offs
By choosing an **Asynchronous Persistence** pattern, we prioritize **Availability** and **Low Latency** for the end-user. Even if DynamoDB experiences a rare transient failure, the user's interaction is never blocked; the message remains safe in SQS for automatic retry, ensuring **Eventual Consistency** without compromising performance.
