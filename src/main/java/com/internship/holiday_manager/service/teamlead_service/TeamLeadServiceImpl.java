package com.internship.holiday_manager.service.teamlead_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.User;
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

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.MalformedURLException;
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
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, byteArrayOutputStream);  // Do this BEFORE document.open()

        document.open();
        document.newPage();

        PdfPTable table = new PdfPTable(3);

        // t.setBorderColor(BaseColor.GRAY);
        // t.setPadding(4);
        // t.setSpacing(4);
        // t.setBorderWidth(1);

        PdfPCell c1 = new PdfPCell(new Phrase("Surname"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Forname"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Email"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        table.setHeaderRows(1);
        table.addCell("1.0");
        table.addCell("1.1");
        table.addCell("1.2");
        table.addCell("2.1");
        table.addCell("2.2");
        table.addCell("2.3");

        document.add(new Paragraph("Hello World!"));
        document.add(table);




        document.close();
        return byteArrayOutputStream.toByteArray();

    }

}
