package com.grievancehub.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.grievancehub.entity.Complaint;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] generateComplaintPdf(Complaint complaint) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        
        document.open();
        
        // Add Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, java.awt.Color.DARK_GRAY);
        Paragraph title = new Paragraph("GrievanceHub Official Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        document.add(new Paragraph("\n\n"));
        
        // Add Content
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.BLACK);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12, java.awt.Color.DARK_GRAY);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        String date = complaint.getCreatedAt() != null ? complaint.getCreatedAt().format(formatter) : "N/A";

        String citizenName = complaint.getUser() != null ? complaint.getUser().getName() : "Unknown Citizen";
        String citizenEmail = complaint.getUser() != null ? complaint.getUser().getEmail() : "N/A";

        // Meta data section
        document.add(new Paragraph("Complaint ID: #" + complaint.getId(), headerFont));
        document.add(new Paragraph("Current Status: " + complaint.getStatus(), contentFont));
        document.add(new Paragraph("Date Submitted: " + date, contentFont));
        document.add(new Paragraph("Citizen Name: " + citizenName, contentFont));
        document.add(new Paragraph("Citizen Email: " + citizenEmail, contentFont));
        document.add(new Paragraph("Department: " + complaint.getDepartment(), contentFont));
        
        document.add(new Paragraph("\n"));
        
        // Detailed textual sections
        document.add(new Paragraph("Problem Title:", headerFont));
        document.add(new Paragraph(complaint.getTitle(), contentFont));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Full Description:", headerFont));
        document.add(new Paragraph(complaint.getDescription(), contentFont));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Official Admin Response:", headerFont));
        String response = (complaint.getAdminReply() != null && !complaint.getAdminReply().isEmpty()) 
                            ? complaint.getAdminReply() : "Pending review...";
        document.add(new Paragraph(response, contentFont));
        
        document.close();
        
        return out.toByteArray();
    }

    public byte[] generateRtiPdf(Complaint complaint) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();
        
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, java.awt.Color.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, java.awt.Color.BLACK);

        String citizenName = complaint.getUser() != null ? complaint.getUser().getName() : "Unknown Citizen";
        String citizenEmail = complaint.getUser() != null ? complaint.getUser().getEmail() : "N/A";
        
        Paragraph title = new Paragraph("APPLICATION UNDER RIGHT TO INFORMATION ACT, 2005", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));
        
        document.add(new Paragraph("To,", normalFont));
        document.add(new Paragraph("The Public Information Officer (PIO)", boldFont));
        document.add(new Paragraph("Department of " + (complaint.getDepartment() != null ? complaint.getDepartment() : "Public Grievances") + ",", boldFont));
        document.add(new Paragraph(complaint.getCity() + ", " + complaint.getState(), normalFont));
        document.add(new Paragraph("\n"));
        
        document.add(new Paragraph("Subject: Request for information regarding Grievance Tracking ID " + complaint.getTrackingId(), boldFont));
        document.add(new Paragraph("\n"));
        
        document.add(new Paragraph("Respected Sir/Madam,", normalFont));
        document.add(new Paragraph("I, " + citizenName + ", hereby request the following information under the Right to Information Act, 2005, pertaining to my grievance filed on the GrievanceHub portal.", normalFont));
        document.add(new Paragraph("\n"));
        
        document.add(new Paragraph("Particulars of Grievance:", boldFont));
        document.add(new Paragraph("1. Grievance Tracking ID: " + complaint.getTrackingId(), normalFont));
        document.add(new Paragraph("2. Date of Filing: " + (complaint.getCreatedAt() != null ? complaint.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "N/A"), normalFont));
        document.add(new Paragraph("3. Subject Matter: " + complaint.getTitle(), normalFont));
        document.add(new Paragraph("4. Description: " + complaint.getDescription(), normalFont));
        document.add(new Paragraph("\n"));
        
        document.add(new Paragraph("Information Requested:", boldFont));
        document.add(new Paragraph("- Please provide certified copies of the action taken report and the daily progress made on the above-mentioned grievance.", normalFont));
        document.add(new Paragraph("- Please provide the name and designation of the officials responsible for the delay in resolving the grievance.", normalFont));
        document.add(new Paragraph("\n"));
        
        document.add(new Paragraph("Applicant Details:", boldFont));
        document.add(new Paragraph("Name: " + citizenName, normalFont));
        document.add(new Paragraph("Email: " + citizenEmail, normalFont));
        if (complaint.getUser() != null && complaint.getUser().getMobileNumber() != null) {
            document.add(new Paragraph("Mobile: " + complaint.getUser().getMobileNumber(), normalFont));
        }
        document.add(new Paragraph("\n"));
        
        document.add(new Paragraph("Date: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")), normalFont));
        document.add(new Paragraph("Place: " + complaint.getCity(), normalFont));
        document.add(new Paragraph("\n\n"));
        document.add(new Paragraph("(Signature of Applicant)", normalFont));
        
        document.close();
        return out.toByteArray();
    }
}
