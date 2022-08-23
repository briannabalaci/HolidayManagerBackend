package com.internship.holiday_manager.service.teamlead_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import com.internship.holiday_manager.mapper.HolidayMapper;
import com.internship.holiday_manager.repository.HolidayRepository;
import com.internship.holiday_manager.repository.TeamRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import com.itextpdf.text.pdf.parser.clipper.Path;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TeamLeadServiceImpl implements TeamLeadService{

    @Autowired
    private final HolidayRepository holidayRepository;

    @Autowired
    private final HolidayMapper holidayMapper;

    @Autowired
    private final TeamRepository teamRepository;

    @Override
    public List<HolidayDto> getRequests(Long teamLeaderId) {
        List<Holiday> entities = this.holidayRepository.findByUserId(teamLeaderId);

        return holidayMapper.entitiesToDtos(entities);
    }

    @Override
    public List<HolidayDto> getTeamRequests(Long teamId) {
        List<User> members = this.teamRepository.getById(teamId).getMembers();

        List<Holiday> holidays = new ArrayList<>();

        members.forEach(holiday -> {
                                    if(!holiday.getType().name().equals("TEAMLEAD"))
                                        holidays.addAll(this.holidayRepository.findByUserId(holiday.getId()));
                                    }
                        );

        // TODO: - still in doubts if i need this line or not
        // dtos.forEach(h -> { h.getUser().setTeamId(teamId);});

        return holidayMapper.entitiesToDtos(holidays);
    }
// List<HolidayDto> holidays= getTeamRequests(teamId);
    @Override
    public byte[] getPDF(Long teamId) throws DocumentException {
        List<User> members = this.teamRepository.getById(teamId).getMembers();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 15, 15, 5 , 5);;
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();
        document.newPage();
        float [] pointColumnWidthsMembersTable = {150f, 150f,350f,150f};
        PdfPTable membersTable = new PdfPTable(pointColumnWidthsMembersTable);


        PdfPCell c1 = new PdfPCell(new Phrase("Surname"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        membersTable.addCell(c1);

        c1 = new PdfPCell(new Phrase("Forname"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        membersTable.addCell(c1);

        c1 = new PdfPCell(new Phrase("Email"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        membersTable.addCell(c1);

        c1 = new PdfPCell(new Phrase("nrHolidays"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        membersTable.addCell(c1);
        membersTable.setHeaderRows(1);
        for(User u : members){
            PdfPCell surnameCell = new PdfPCell(Phrase.getInstance(u.getSurname()));
            surnameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            membersTable.addCell(surnameCell);

            PdfPCell fornameCell = new PdfPCell(Phrase.getInstance(u.getForname()));
            fornameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            membersTable.addCell(fornameCell);

            PdfPCell emailCell = new PdfPCell(Phrase.getInstance(u.getEmail()));
            emailCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            membersTable.addCell(emailCell);

            PdfPCell NrHolidays = new PdfPCell(Phrase.getInstance(u.getNrHolidays().toString()));
            NrHolidays.setHorizontalAlignment(Element.ALIGN_CENTER);
            membersTable.addCell(NrHolidays);

        }



        float [] pointColumnWidthsRequestsTable = {250f,250f, 250f,250f,250f,250f,350f};
        PdfPTable requestsTable = new PdfPTable(pointColumnWidthsRequestsTable);
        c1 = new PdfPCell(new Phrase("Member"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        requestsTable.addCell(c1);

        c1 = new PdfPCell(new Phrase("Start Date"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        requestsTable.addCell(c1);

        c1 = new PdfPCell(new Phrase("End Date"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        requestsTable.addCell(c1);

        c1 = new PdfPCell(new Phrase("Substitute"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        requestsTable.addCell(c1);

        c1 = new PdfPCell(new Phrase("Type"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        requestsTable.addCell(c1);

        c1 = new PdfPCell(new Phrase("Status"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        requestsTable.addCell(c1);

        c1 = new PdfPCell(new Phrase("Details"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        requestsTable.addCell(c1);

        List<Holiday> holidays = new ArrayList<>();

        members.forEach(holiday -> {
                    if(!holiday.getType().name().equals("TEAMLEAD"))
                        holidays.addAll(this.holidayRepository.findByUserId(holiday.getId()));
                }
        );
        for(Holiday h : holidays){
            PdfPCell memberCell = new PdfPCell(Phrase.getInstance(h.getUser().getForname()+" "+h.getUser().getSurname()));
            memberCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            requestsTable.addCell(memberCell);

            PdfPCell startDateCell = new PdfPCell(Phrase.getInstance(h.getStartDate().toString()));
            startDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            requestsTable.addCell(startDateCell);

            PdfPCell endDateCell = new PdfPCell(Phrase.getInstance(h.getEndDate().toString()));
            endDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            requestsTable.addCell(endDateCell);

            PdfPCell substituteCell = new PdfPCell(Phrase.getInstance(h.getSubstitute()));
            substituteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            requestsTable.addCell(substituteCell);

            PdfPCell typeCell = new PdfPCell(Phrase.getInstance(h.getType().toString()));
            typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            requestsTable.addCell(typeCell);

            PdfPCell statusCell = new PdfPCell(Phrase.getInstance(h.getStatus().toString()));
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            requestsTable.addCell(statusCell);

            PdfPCell detailsCell = new PdfPCell(Phrase.getInstance(h.getDetails().toString()));
            detailsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            requestsTable.addCell(detailsCell);
        }


     document.add(membersTable);
        document.add(new Paragraph(" "));
     document.add(requestsTable);






        document.close();
        return byteArrayOutputStream.toByteArray();

    }

}
