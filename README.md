#  GrievanceHub – Public Complaint Management System

GrievanceHub is a web-based platform developed using **Java Spring Boot**, **Thymeleaf**, and **MySQL**, allowing users to submit complaints with location and optional image uploads. Admins can review and take action on the complaints.

---

Features

✅ User Registration & Login (Form-based Authentication)
✅ Login with Google (OAuth 2.0)
✅ Secure Authentication using Spring Security
✅ Complaint Submission with Title, Description, Location & Image
✅ View Complaints Submitted by Logged-in User
✅ Image Upload and Storage in /static/uploads
✅ Image Preview and Download Option
✅ Role-based Access (User / Admin – extendable)
✅ Admin Dashboard (Future Enhancement)

Tech Stack
Backend: Java, Spring Boot, Spring Security, OAuth 2.0
Frontend: HTML, CSS, Thymeleaf
Database: MySQL

Authentication:
Form Login (Username & Password)
Google OAuth 2.0 Login

File Upload: MultipartFile

Build Tool: Maven
---

##  Project Structure

grievancehub/
│
├── src/main/java/com/grievancehub/
│   ├── controller/
│   │   └── ComplaintController.java
│   ├── service/
│   │   └── ComplaintService.java
│   ├── entity/
│   │   ├── Complaint.java
│   │   └── User.java
│   ├── repository/
│   │   ├── ComplaintRepository.java
│   │   └── UserRepository.java
│   └── config/
│       ├── SecurityConfig.java
│       └── WebMvcConfig.java
│
├── src/main/resources/
│   ├── static/
│   │   └── uploads/        # Uploaded images
│   └── templates/
│       ├── create-complaint.html
│       ├── my-complaints.html
│       ├── login.html
│       └── register.html
│
├── application.properties
└── pom.xml


---
Authentication Flow
Form-Based Login
Users can register with username, email, and password.
Passwords are securely encrypted using BCrypt.
Access is controlled using Spring Security.

Google OAuth 2.0 Login
Users can log in using their Google account.
OAuth authentication is handled by Spring Security.
On first login, user details (name, email) are saved in the database.
Existing users are automatically recognized on future logins.

--

##  Image Handling
- Images are uploaded using `MultipartFile` and stored inside `src/main/resources/static/uploads/`.
- The `imagePath` is saved in the database.
- Images are shown on the complaint list using:
  ```html
  <img th:src="@{${complaint.imagePath}}" width="100"/>
<a th:href="@{${complaint.imagePath}}" download>Download</a>

---

## How to Run
1.Clone the repo:
git clone https:[//github.com/pradeepkumar823/grievancehub.git](https://github.com/pradeepkumar823/GrievanceHub.git)
cd grievancehub

-
2.Set up MySQL DB and update application.properties:
spring.datasource.url=jdbc:mysql://localhost:3306/grievancehub
spring.datasource.username=root
spring.datasource.password=yourpassword

-
3. Configure Google OAuth
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email

-
4.Run the application:
mvn spring-boot:run

-
5.Open browser:
http://localhost:8081/
