# 🏛️ GrievanceHub – Enterprise Public Complaint Management System

![GrievanceHub Banner](https://via.placeholder.com/1200x300.png?text=GrievanceHub+-+Empowering+Citizens,+Enabling+Action)

**GrievanceHub** is an enterprise-grade, production-ready web platform designed to seamlessly bridge the gap between citizens and civic authorities. Built with modern Java Spring Boot architectures, it allows the public to lodge infrastructural issues, track resolutions in real-time, and ensures government bodies are held accountable through automated Service Level Agreement (SLA) tracking and analytics dashboards.

---

## 🌟 Comprehensive Features

### 👤 Citizen Portal
*   **Frictionless Access:** Securely register and log in natively using BCrypt encrypted passwords, or use one-click **Google OAuth 2.0** integration.
*   **Rich Complaint Lodging:** Submit detailed grievances including titles, descriptions, dynamic geolocation (Latitude/Longitude), and photographic evidence using Spring Multipart support.
*   **Smart Department Triage (AI):** If a user is unsure of the department, an automated keyword-based triage logic routing mechanism scans the text and auto-assigns the complaint to the correct civic body (e.g., "garbage" maps to Sanitation).
*   **Real-time Tracking:** Citizens can monitor their complaint's lifecycle (Pending → In Progress → Resolved → Closed).
*   **Documentation:** Generate and download official PDF receipts and RTI (Right to Information) drafts for every lodged complaint via embedded iText/OpenPDF engines.
*   **Bilingual Interface:** Toggle seamlessly between English and Hindi using Spring `MessageSource` localization.

### 🛡️ Admin & Officer Command Center
*   **Analytical Dashboard:** A high-level visual dashboard displaying SLA breaches, departmental performance, and complaint volume dynamically rendered using Chart.js.
*   **Automated Escalations (SLA):** Complaints pending beyond predefined thresholds automatically trigger priority escalations and dispatch SMTP email alerts to administrators.
*   **Workflow Management:** Assign specific officers to cases, update statuses, and provide official administrative responses directly to citizens.
*   **Rich HTML Communications:** The system automatically dispatches premium, responsive HTML email notifications (via JavaMailSender & MimeMessage) for new submissions, status changes, and critical alerts.

---

## 🔑 Roles & Permissions (RBAC)

GrievanceHub enforces strict Role-Based Access Control via Spring Security.

| Role | Responsibilities | Core Access Level |
| :--- | :--- | :--- |
| **USER** | Citizens filing complaints. Can track their own data, upvote public issues, and download PDFs. | `/complaints/my`, `/profile`, `/complaints/new` |
| **OFFICER** | Civic officials assigned to specific departments. Can update statuses and add resolution remarks. | `/officer/dashboard`, `/officer/complaints` |
| **ADMIN** | System operators. Full visibility into all data, dashboards, user management, and manual officer assignment. | `/admin/dashboard`, `/admin/users`, `/admin/assign` |

---

## 🗄️ Database Schema & Entities

The application utilizes Spring Data JPA over a MySQL 8 backend.

1. **User Entity**: Stores citizen and administrator profiles. Fields include `email` (PK), `password`, `name`, `mobile_number`, `department`, `role`, and `oauth_provider` flags.
2. **Complaint Entity**: Stores the core ticket data. Fields include `id` (PK), `tracking_id` (Unique String), `title`, `description`, `status`, `department`, `priority`, `latitude`, `longitude`, `image_path`, and `user_id` (FK).
3. **ComplaintAudit Entity**: Maintains a strict timeline of when a complaint changed status, tracking timestamps, actor, and state changes for historical compliance.

---

## 🗺️ Core Application Routes (MVC)

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` or `/home` | Public landing page and overview |
| `GET / POST` | `/login` / `/register` | Authentication gateways |
| `GET / POST` | `/complaints/new` | Multi-part form for citizens to submit grievances |
| `GET` | `/complaints/explore` | Public transparency board for upvoting community issues |
| `GET` | `/admin/dashboard` | Secured admin analytics and SLA tracking board |
| `POST` | `/admin/complaints/{id}/status` | Endpoint for admins/officers to update complaint lifecycle |
| `GET` | `/complaints/{id}/pdf` | Generates a downloadable PDF receipt via iText |

---

## 🛠️ Technology Stack & Dependencies

| Layer | Technology |
| :--- | :--- |
| **Backend Framework** | Java 17, Spring Boot 3.5.x |
| **Security** | Spring Security 6, OAuth 2.0 Client |
| **Frontend Templates** | Thymeleaf, HTML5, Vanilla CSS3 (Glassmorphism UI) |
| **Database** | MySQL 8.0, Spring Data JPA, Hibernate |
| **Email Services** | Spring Mail (JavaMailSender) |
| **Document Generation**| OpenPDF / iText |
| **Build & Dependency** | Maven |

---

## 📁 Detailed System Architecture

```text
GrievanceHub/
├── src/main/java/com/grievancehub/
│   ├── config/          # Tomcat limits, OAuth, Security Chains, App Initializers
│   ├── controller/      # Web Request Handlers (Admin, Complaint, Auth, Public)
│   ├── entity/          # JPA Domain Models (User, Complaint, AuditLogs)
│   ├── repository/      # Database Access Interfaces (Spring Data JPA JpaRepository)
│   └── service/         # Core Business Logic (Email formatting, PDF drawing, AI Triage)
├── src/main/resources/
│   ├── static/          # CSS, JavaScript, and Uploaded Evidences (Images)
│   ├── templates/       # Thymeleaf HTML Views (Dashboards, Forms, Fragments)
│   ├── application.properties # Core system configuration
│   ├── messages_en.properties # English Localization
│   └── messages_hi.properties # Hindi Localization
└── pom.xml              # Maven dependencies
```

---

## 🚀 Installation & Local Setup

Follow these exhaustive steps to deploy GrievanceHub in your local development environment.

### Prerequisites
*   **Java Development Kit (JDK) 17** or higher
*   **Apache Maven 3.8+**
*   **MySQL Server 8.x** running locally on port `3306`

### 1. Clone the Repository
```bash
git clone https://github.com/pradeepkumar823/GrievanceHub.git
cd GrievanceHub
```

### 2. Configure the Database
Log into your local MySQL instance and create the database schema:
```sql
CREATE DATABASE grievancehub;
```

### 3. Environment Configuration (`application.properties`)
Create or edit the `src/main/resources/application.properties` file. You must provide your own credentials for the database, OAuth, and SMTP server.

```properties
# ---------------- DATABASE ----------------
spring.datasource.url=jdbc:mysql://localhost:3306/grievancehub
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# ---------------- SECURITY & OAUTH ----------------
# Obtain these from Google Cloud Console (APIs & Services -> Credentials)
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=email,profile

# ---------------- SYSTEM DEFAULTS ----------------
app.admin.email=admin@gmail.com
app.admin.password=admin123
server.port=8081

# Fix for large multipart forms (Complaint submissions)
server.tomcat.max-part-count=50

# ---------------- EMAIL CONFIG (SMTP) ----------------
# Use an App Password if using Gmail with 2FA enabled
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL_ADDRESS@gmail.com
spring.mail.password=YOUR_GOOGLE_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
```

### 4. Build and Run
Execute the application using the Spring Boot Maven plugin:
```bash
mvn clean install
mvn spring-boot:run
```

### 5. Access the Application
Open your web browser and navigate to:
👉 **[http://localhost:8081/](http://localhost:8081/)**

*(Note: The system automatically provisions a default administrator account upon first startup using the credentials specified in your properties file).*

---

## 🛡️ Security & Troubleshooting

### Multipart Upload Limits (`FileCountLimitExceededException`)
Modern Tomcat embedded servers restrict multipart requests to a maximum of 10 parts by default to prevent DoS attacks. Because the grievance submission form transmits complex data (CSRF tokens, geolocation, metadata, and files), this application overrides the strict limits natively via `server.tomcat.max-part-count=50` to ensure smooth form processing.

### Cross-Site Request Forgery (CSRF)
All POST requests (including profile updates, logout actions, and upvoting) are strictly protected by Spring Security's XOR CSRF tokens. Ensure that all custom HTML forms utilize the Thymeleaf `th:action` tag to inherit these tokens automatically.

---

## 📈 Future Roadmap
- [ ] Integration with Twilio for SMS status alerts.
- [ ] Automated image analysis to identify duplicate complaints based on visual hashes.
- [ ] Full REST API rollout for native Android/iOS mobile application support.

---

## 👨‍💻 Development & Contribution

GrievanceHub thrives on continuous improvement. If you wish to contribute:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

*Architected and maintained by **[Pradeep Kumar](https://github.com/pradeepkumar823)***.
