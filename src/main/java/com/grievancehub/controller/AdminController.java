package com.grievancehub.controller;

import com.grievancehub.entity.Complaint;
import com.grievancehub.entity.User;
import com.grievancehub.entity.Officer;
import com.grievancehub.service.ComplaintService;
import com.grievancehub.service.UserService;
import com.grievancehub.service.OfficerService;
import com.grievancehub.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserService userService;

    @Autowired
    private OfficerService officerService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // ── Helper: AccountDto for rendering users/officers in unified list ───────
    public static class AccountDto {
        public Long id;
        public String type;
        public String name;
        public String email;
        public String mobileNumber;
        public String role;
        public String department;
        public Boolean enabled;

        public AccountDto(Long id, String type, String name, String email, String mobileNumber, String role, String department, Boolean enabled) {
            this.id = id; this.type = type; this.name = name; this.email = email;
            this.mobileNumber = mobileNumber; this.role = role; this.department = department; this.enabled = enabled;
        }
    }

    // ── Helper: ensure caller is authorized ──────────────────────────────────────
    private boolean isSuperAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private boolean isOfficer(Authentication auth) {
        return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_OFFICER"));
    }

    private boolean isAuthorized(Authentication auth) {
        return isSuperAdmin(auth) || isOfficer(auth);
    }

    // ── Dashboard ────────────────────────────────────────────────────────────
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model, Authentication authentication,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String department,
                             @RequestParam(required = false) String keyword) {
        if (!isAuthorized(authentication)) return "redirect:/home";

        List<Complaint> all;

        if (isSuperAdmin(authentication)) {
            all = complaintService.getAllComplaints();
        } else {
            Officer currentUser = officerService.findByEmail(authentication.getName());
            if (currentUser != null) {
                all = complaintService.getComplaintsByDepartment(currentUser.getDepartment());
                department = currentUser.getDepartment(); // Override any attempted filter injection
            } else {
                return "redirect:/home"; // Safeguard if user is missing
            }
        }

        final String effectiveDept = department;

        // ── Filtering ──
        List<Complaint> filtered = all.stream()
                .filter(c -> status == null || status.isBlank() || c.getStatus().equalsIgnoreCase(status))
                .filter(c -> effectiveDept == null || effectiveDept.isBlank() || (c.getDepartment() != null && c.getDepartment().equalsIgnoreCase(effectiveDept)))
                .filter(c -> keyword == null || keyword.isBlank() ||
                        (c.getTitle() != null && c.getTitle().toLowerCase().contains(keyword.toLowerCase())) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(keyword.toLowerCase())) ||
                        (c.getTrackingId() != null && c.getTrackingId().toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());

        // ── Stats (always from full list) ──
        long pendingCount  = all.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getStatus())   || "Pending".equalsIgnoreCase(c.getStatus())).count();
        long progressCount = all.stream().filter(c -> "IN_PROGRESS".equalsIgnoreCase(c.getStatus()) || "In Progress".equalsIgnoreCase(c.getStatus())).count();
        long resolvedCount = all.stream().filter(c -> "RESOLVED".equalsIgnoreCase(c.getStatus())  || "Resolved".equalsIgnoreCase(c.getStatus())).count();
        long rejectedCount = all.stream().filter(c -> "REJECTED".equalsIgnoreCase(c.getStatus())  || "Rejected".equalsIgnoreCase(c.getStatus())).count();

        Map<String, Long> deptStats = all.stream()
                .filter(c -> c.getDepartment() != null && !c.getDepartment().trim().isEmpty())
                .collect(Collectors.groupingBy(Complaint::getDepartment, Collectors.counting()));

        List<String> departments = all.stream()
                .map(Complaint::getDepartment)
                .filter(d -> d != null && !d.isBlank())
                .distinct().sorted().collect(Collectors.toList());

        model.addAttribute("pendingCount",  pendingCount);
        model.addAttribute("progressCount", progressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("deptStats",     deptStats);
        model.addAttribute("totalCount",    all.size());
        model.addAttribute("departments",   departments);
        model.addAttribute("complaints",    filtered);
        model.addAttribute("filterStatus",  status);
        model.addAttribute("filterDept",    department);
        model.addAttribute("filterKeyword", keyword);
        return "admin-dashboard";
    }

    // ── Update Complaint Status / Reply ──────────────────────────────────────
    @PostMapping("/complaint/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               @RequestParam(required = false) String adminReply,
                               Authentication authentication) {
        if (!isAuthorized(authentication)) return "redirect:/home";
        if (isOfficer(authentication)) {
            Complaint c = complaintService.getComplaintById(id);
            Officer currentUser = officerService.findByEmail(authentication.getName());
            if (c.getDepartment() == null || !c.getDepartment().equalsIgnoreCase(currentUser.getDepartment())) {
                return "redirect:/admin/dashboard";
            }
        }
        String actingOfficial = authentication.getName();
        complaintService.updateComplaintStatus(id, status, adminReply, actingOfficial);
        return "redirect:/admin/complaint/" + id;
    }

    // ── View Single Complaint Detail ─────────────────────────────────────────
    @GetMapping("/complaint/{id}")
    public String complaintDetail(@PathVariable Long id, Model model, Authentication authentication) {
        if (!isAuthorized(authentication)) return "redirect:/home";
        Complaint c = complaintService.getComplaintById(id);
        
        if (isOfficer(authentication)) {
            Officer currentUser = officerService.findByEmail(authentication.getName());
            if (c.getDepartment() == null || !c.getDepartment().equalsIgnoreCase(currentUser.getDepartment())) {
                return "redirect:/admin/dashboard";
            }
        }

        model.addAttribute("complaint", c);
        model.addAttribute("audits", complaintService.getAudits(id));
        return "admin-complaint-detail";
    }

    // ── Generate Complaint PDF ──────────────────────────────────────────────
    @GetMapping("/complaint/{id}/pdf")
    public ResponseEntity<byte[]> downloadComplaintPdf(@PathVariable Long id, Authentication authentication) {
        if (!isAuthorized(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            Complaint c = complaintService.getComplaintById(id);
            if (isOfficer(authentication)) {
                Officer currentUser = officerService.findByEmail(authentication.getName());
                if (c.getDepartment() == null || !c.getDepartment().equalsIgnoreCase(currentUser.getDepartment())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            byte[] pdfBytes = pdfService.generateComplaintPdf(c);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "GrievanceHub_Report_" + c.getTrackingId() + ".pdf");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── Delete Complaint ──────────────────────────────────────────────────────
    @PostMapping("/complaint/{id}/delete")
    public String deleteComplaint(@PathVariable Long id, Authentication authentication) {
        if (!isSuperAdmin(authentication)) return "redirect:/home";
        complaintService.deleteComplaint(id);
        return "redirect:/admin/dashboard";
    }

    // ── User Management ───────────────────────────────────────────────────────
    @GetMapping("/users")
    public String manageUsers(Model model, Authentication authentication) {
        if (!isSuperAdmin(authentication)) return "redirect:/home";
        
        List<AccountDto> usersList = new ArrayList<>();
        List<AccountDto> officersList = new ArrayList<>();
        
        // Add all regular users (excluding super admin)
        List<User> users = userService.getAllUsers().stream()
                .filter(u -> !"ADMIN".equals(u.getRole()))
                .collect(Collectors.toList());
                
        for (User u : users) {
            usersList.add(new AccountDto(u.getId(), "USER", u.getName(), u.getEmail(), u.getMobileNumber(), u.getRole(), u.getDepartment(), u.getEnabled()));
        }

        // Add all officers
        for (Officer o : officerService.getAllOfficers()) {
            officersList.add(new AccountDto(o.getId(), "OFFICER", o.getName(), o.getEmail(), o.getMobileNumber(), o.getRole(), o.getDepartment(), o.getEnabled()));
        }
                
        model.addAttribute("users", usersList);
        model.addAttribute("officers", officersList);
        model.addAttribute("totalUsers", usersList.size() + officersList.size());
        return "admin-users";
    }

    // ── Delete User / Officer ─────────────────────────────────────────────────
    @PostMapping("/user/{id}/delete")
    public String deleteUser(@PathVariable Long id, Authentication authentication) {
        if (!isSuperAdmin(authentication)) return "redirect:/home";
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }
    
    @PostMapping("/officer/{id}/delete")
    public String deleteOfficer(@PathVariable Long id, Authentication authentication) {
        if (!isSuperAdmin(authentication)) return "redirect:/home";
        officerService.deleteOfficerById(id);
        return "redirect:/admin/users";
    }

    // ── Create Nodal Officer ──────────────────────────────────────────────────
    @PostMapping("/user/create-officer")
    public String createOfficer(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String department,
                                @RequestParam String mobileNumber,
                                Authentication authentication) {
        if (!isSuperAdmin(authentication)) return "redirect:/home";
        
        if (userService.emailExists(email) || officerService.emailExists(email)) {
            return "redirect:/admin/users?error=Email+already+registered";
        }
        
        Officer officer = new Officer();
        officer.setName(name);
        officer.setEmail(email);
        officer.setPassword(passwordEncoder.encode(password));
        officer.setRole("ROLE_OFFICER");
        officer.setDepartment(department);
        officer.setMobileNumber(mobileNumber);
        
        officerService.save(officer);
        
        return "redirect:/admin/users?success=Officer+account+provisioned";
    }

    // ── Admin Profile ─────────────────────────────────────────────────────────
    @GetMapping("/profile")
    public String adminProfile(Model model, Authentication authentication) {
        if (!isAuthorized(authentication)) return "redirect:/home";
        
        if (isSuperAdmin(authentication)) {
            User admin = userService.findByEmail(authentication.getName());
            model.addAttribute("admin", admin);
        } else {
            Officer admin = officerService.findByEmail(authentication.getName());
            model.addAttribute("admin", admin);
        }
        return "admin-profile";
    }

    // ── Live Interactive Heatmap ──────────────────────────────────────────────
    @GetMapping("/map")
    public String adminMap(Authentication authentication) {
        if (!isAuthorized(authentication)) return "redirect:/home";
        return "admin-map";
    }

    @GetMapping("/api/map-data")
    @ResponseBody
    public List<Map<String, Object>> getMapData(Authentication authentication) {
        if (!isAuthorized(authentication)) return List.of();
        
        List<Complaint> all;
        if (isSuperAdmin(authentication)) {
            all = complaintService.getAllComplaints();
        } else {
            Officer currentUser = officerService.findByEmail(authentication.getName());
            if (currentUser != null) {
                all = complaintService.getComplaintsByDepartment(currentUser.getDepartment());
            } else {
                return List.of();
            }
        }
        
        return all.stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .map(c -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", c.getId());
                    m.put("trackingId", c.getTrackingId());
                    m.put("title", c.getTitle());
                    m.put("department", c.getDepartment() != null ? c.getDepartment() : "General");
                    m.put("status", c.getStatus());
                    m.put("priority", c.getPriority() != null ? c.getPriority() : "Low");
                    m.put("latitude", c.getLatitude());
                    m.put("longitude", c.getLongitude());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // ── High Speed CSV Data Pipeline ──────────────────────────────────────────
    @GetMapping("/export/csv")
    public void exportToCsv(HttpServletResponse response, Authentication authentication) throws Exception {
        if (!isAuthorized(authentication)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized Access.");
            return;
        }

        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"GrievanceHub_Export_Logs.csv\"");

        try (java.io.PrintWriter writer = response.getWriter()) {
        
        // Native Master Standard Headers
        writer.println("Tracking ID,Reported Date,Target Department,Citizen Title,Priority Classification,Current Status,City,State,Total Community Upvotes");

        List<Complaint> all;
        if (isSuperAdmin(authentication)) {
            all = complaintService.getAllComplaints();
        } else {
            Officer currentUser = officerService.findByEmail(authentication.getName());
            if (currentUser != null) {
                all = complaintService.getComplaintsByDepartment(currentUser.getDepartment());
            } else {
                return;
            }
        }

        for (Complaint c : all) {
            String date = c.getCreatedAt() != null ? c.getCreatedAt().toString() : "N/A";
            // Scrub standard carriage returns out of CSV files to prevent corruption
            String title = c.getTitle() != null ? c.getTitle().replace(",", " ").replace("\n", " ").replace("\r", " ") : "N/A";
            String dept = c.getDepartment() != null ? c.getDepartment() : "General";
            String priority = c.getPriority() != null ? c.getPriority() : "Normal";
            String status = c.getStatus() != null ? c.getStatus() : "Unknown";
            String city = c.getCity() != null ? c.getCity().replace(",", " ") : "N/A";
            String state = c.getState() != null ? c.getState().replace(",", " ") : "N/A";

            // Print natively
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%d\n",
                    c.getTrackingId(), date, dept, title, priority, status, city, state, c.getUpvoteCount());
        }
        }
    }
}
