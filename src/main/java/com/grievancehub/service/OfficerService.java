package com.grievancehub.service;

import com.grievancehub.entity.Officer;
import com.grievancehub.repository.OfficerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficerService {

    private final OfficerRepository officerRepository;

    @Autowired
    public OfficerService(OfficerRepository officerRepository) {
        this.officerRepository = officerRepository;
    }

    public String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    public boolean emailsConceptuallyMatch(String email1, String email2) {
        if (email1 == null || email2 == null) return false;
        
        email1 = email1.trim().toLowerCase();
        email2 = email2.trim().toLowerCase();
        
        if (email1.equals(email2)) return true;
        
        // Gmail dot-equivalence check
        boolean isGmail1 = email1.endsWith("@gmail.com") || email1.endsWith("@googlemail.com");
        boolean isGmail2 = email2.endsWith("@gmail.com") || email2.endsWith("@googlemail.com");
        
        if (isGmail1 && isGmail2) {
            String local1 = email1.split("@")[0].replace(".", "");
            String local2 = email2.split("@")[0].replace(".", "");
            return local1.equals(local2);
        }
        
        return false;
    }

    public Officer findByEmail(String email) {
        if (email == null) return null;
        String normalizedSearch = normalizeEmail(email);
        
        // First try to find by exact match
        List<Officer> exactMatch = officerRepository.findByEmail(normalizedSearch);
        if (!exactMatch.isEmpty()) return exactMatch.get(0);
        
        // If not found, check for Gmail dot-equivalent matches
        if (normalizedSearch.endsWith("@gmail.com") || normalizedSearch.endsWith("@googlemail.com")) {
            List<Officer> allOfficers = officerRepository.findAll();
            for (Officer o : allOfficers) {
                if (emailsConceptuallyMatch(o.getEmail(), normalizedSearch)) {
                    return o;
                }
            }
        }
        
        return null;
    }

    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }

    public void save(Officer officer) {
        officer.setEmail(normalizeEmail(officer.getEmail()));
        if (officer.getRole() == null || officer.getRole().isEmpty()) {
            officer.setRole("ROLE_OFFICER");
        }
        officerRepository.save(officer);
    }

    public List<Officer> getAllOfficers() {
        return officerRepository.findAll();
    }

    public void deleteOfficerById(Long id) {
        officerRepository.deleteById(java.util.Objects.requireNonNull(id));
    }
}
