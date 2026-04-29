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
        if (email == null)
            return null;
        email = email.trim().toLowerCase();
        if (email.endsWith("@gmail.com") || email.endsWith("@googlemail.com")) {
            String[] parts = email.split("@");
            String localPart = parts[0].replace(".", "");
            return localPart + "@" + parts[1];
        }
        return email;
    }

    public Officer findByEmail(String email) {
        List<Officer> officers = officerRepository.findByEmail(normalizeEmail(email));
        return officers.isEmpty() ? null : officers.get(0);
    }

    public boolean emailExists(String email) {
        List<Officer> officers = officerRepository.findByEmail(normalizeEmail(email));
        return !officers.isEmpty();
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
