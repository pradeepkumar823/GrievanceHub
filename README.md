# 🏛️ GrievanceHub – Public Complaint Management System

GrievanceHub is a full-stack web application built with **Java Spring Boot**, **Thymeleaf**, and **MySQL** that enables citizens to submit public complaints with location tracking and image uploads. Admins and officers can review, assign, and resolve complaints through a dedicated command dashboard.

---

## ✅ Features

### 👤 User
- Register & Login with email/password
- Login with **Google OAuth 2.0**
- Submit complaints with title, description, category, location & image
- Track status of submitted complaints
- Download complaint as **PDF**
- View profile and complaint history
- Multi-language support (**English / Hindi**)

### 🛡️ Admin
- Admin Command Center Dashboard
- View, filter, and manage all complaints
- Assign complaints to Officers
- Update complaint status (Pending → In Progress → Resolved)
- Manage users and officers
- Live complaint map view
- SLA-based auto-escalation

---

## 🛠️ Tech Stack

| Layer        | Technology                                      |
|--------------|-------------------------------------------------|
| Backend      | Java 17, Spring Boot, Spring Security, OAuth 2.0 |
| Frontend     | HTML5, CSS3, Thymeleaf, JavaScript              |
| Database     | MySQL 8                                         |
| Email        | Spring Mail (SMTP via Gmail)                    |
| PDF          | iText / OpenPDF                                 |
| Build Tool   | Maven                                           |
| Real-time    | WebSocket (Spring)                              |

---

## 📁 Project Structure

```
GrievanceHub/
├── src/
│   ├── main/
│   │   ├── java/com/grievancehub/
│   │   │   ├── config/           # Security, OAuth, WebMVC, WebSocket configs
│   │   │   ├── controller/       # Admin, Auth, Complaint, Home controllers
│   │   │   ├── entity/           # User, Complaint, Officer, ComplaintAudit
│   │   │   ├── repository/       # JPA Repositories
│   │   │   └── service/          # Business logic & services
│   │   └── resources/
│   │       ├── static/           # Images, uploads
│   │       ├── templates/        # Thymeleaf HTML templates
│   │       ├── application.properties.example  ← copy this!
│   │       ├── messages.properties             # Default language
│   │       ├── messages_en.properties          # English
│   │       └── messages_hi.properties          # Hindi
├── .gitignore
├── pom.xml
└── README.md
```

---

## ⚙️ Local Setup

### 1. Clone the Repository

```bash
git clone https://github.com/pradeepkumar823/GrievanceHub.git
cd GrievanceHub
```

### 2. Create `application.properties`

> ⚠️ `application.properties` is NOT committed (it's in `.gitignore`). You must create it locally.

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Then fill in your actual values in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/grievancehub
spring.datasource.username=root
spring.datasource.password=YOUR_DB_PASSWORD

# Google OAuth2 (from Google Cloud Console)
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=email,profile

# Admin credentials
app.admin.email=admin@gmail.com
app.admin.password=YOUR_ADMIN_PASSWORD

# Gmail SMTP (use a Google App Password)
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_GOOGLE_APP_PASSWORD
```

### 3. Create MySQL Database

```sql
CREATE DATABASE grievancehub;
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

### 5. Open in Browser

```
http://localhost:8081/
```

---

## 🔐 Authentication

### Form Login
- Users register with name, email, and password
- Passwords encrypted with **BCrypt**
- Roles: `USER`, `ADMIN`, `OFFICER`

### Google OAuth 2.0
- One-click login via Google
- On first login, user is auto-saved to the database
- Existing users are recognized on future logins

---

## 🌍 Multi-Language Support

The app supports **English** and **Hindi** via Spring's `MessageSource`.

Switch language via the URL parameter:
```
http://localhost:8081/?lang=hi   # Hindi
http://localhost:8081/?lang=en   # English
```

---

## 📸 Image Handling

- Uploaded using `MultipartFile`
- Stored in `src/main/resources/static/uploads/`
- Displayed inline on complaint detail pages
- Downloadable as attachment

---

## 📬 Contact

> Built by **Pradeep Kumar** | [GitHub](https://github.com/pradeepkumar823)
