#  GrievanceHub – Public Complaint Management System

GrievanceHub is a web-based platform developed using **Java Spring Boot**, **Thymeleaf**, and **MySQL**, allowing users to submit complaints with location and optional image uploads. Admins can review and take action on the complaints.

---

##  Features

✅ User Registration & Login  
✅ Complaint Submission with Title, Description, Location & Image  
✅ View All Complaints Submitted by Logged-in User  
✅ Admin Dashboard (future enhancement)  
✅ Image Upload Stored in `/static/uploads` Directory  
✅ Image Preview on Complaint List  
✅ Download Image Option  
✅ Secure with Spring Security  

---

##  Tech Stack

- **Backend:** Spring Boot (Java), Spring Security  
- **Frontend:** HTML + Thymeleaf  
- **Database:** MySQL  
- **File Upload:** MultipartFile, stored in `/static/uploads`  
- **Templating Engine:** Thymeleaf  
- **Maven Project**

---

##  Project Structure

grievancehub/
│
├── src/main/java/com/grievancehub/
│ ├── controller/ComplaintController.java
│ ├── service/ComplaintService.java
│ ├── entity/Complaint.java, User.java
│ ├── repository/ComplaintRepository.java, UserRepository.java
│ └── config/WebMvcConfig.java
│
├── src/main/resources/
│ ├── static/uploads/ # Uploaded images stored here
│ └── templates/
│ ├── create-complaint.html # Form to submit complaint
│ ├── my-complaints.html # List of user's complaints
│ ├── login.html # User login
│ └── register.html # User registration
│
├── application.properties
└── pom.xml

---

##  Image Handling

- Images are uploaded using `MultipartFile` and stored inside `src/main/resources/static/uploads/`.
- The `imagePath` is saved in the database.
- Images are shown on the complaint list using:
  ```html
  <img th:src="@{${complaint.imagePath}}" width="100"/>
<a th:href="@{${complaint.imagePath}}" download>Download</a>

--

## How to Run
1.Clone the repo:
git clone https:[//github.com/yourusername/grievancehub.git](https://github.com/pradeepkumar823/GrievanceHub/tree/main)
cd grievancehub

-
2.Set up MySQL DB and update application.properties:
spring.datasource.url=jdbc:mysql://localhost:3306/grievancehub
spring.datasource.username=root
spring.datasource.password=yourpassword

-
3.Run the application:
mvn spring-boot:run

-
4.Open browser:
http://localhost:8080/



















