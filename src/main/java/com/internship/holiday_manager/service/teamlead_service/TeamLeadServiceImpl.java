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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor

public class TeamLeadServiceImpl implements TeamLeadService {

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
                    if (!holiday.getType().name().equals("TEAMLEAD"))
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
        Document document = new Document(PageSize.B2, 5, 5, 5, 5);
        ;
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();
        document.newPage();
        Paragraph documentParagraph = new Paragraph();
        Font titleParagraphFont = new Font(Font.FontFamily.HELVETICA, 30);
        Paragraph titleParagraph = new Paragraph("TEAM LEAD REPORT", titleParagraphFont);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(40f);
        documentParagraph.add(titleParagraph);
        Font tableHeadFont = new Font(Font.FontFamily.HELVETICA, 12);
        tableHeadFont.setColor(230, 132, 11);
        Font userDetailsParagraphFont = new Font(Font.FontFamily.HELVETICA, 20);
        float[] pointColumnWidthsRequestsTable = {250f, 250f, 250f, 250f, 250f, 350f};
        members.forEach(user -> {
                    if (!user.getType().name().equals("TEAMLEAD")) {
                        Paragraph userParagraph = new Paragraph();

                        Paragraph userDetailsParagraph = new Paragraph(user.getForname() + " " + user.getSurname() + " - holiday days: " + user.getNrHolidays(), userDetailsParagraphFont);
                        userDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
                        userDetailsParagraph.setIndentationLeft(140f);
                        userDetailsParagraph.setSpacingAfter(15f);
                        userParagraph.add(userDetailsParagraph);

                        PdfPTable requestsTable = new PdfPTable(pointColumnWidthsRequestsTable);
                        requestsTable.setHeaderRows(1);
                        PdfPCell c1 = new PdfPCell(new Phrase("Start Date", tableHeadFont));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c1.setBackgroundColor(new BaseColor(255, 227, 192));
                        c1.setPadding(10f);
                        requestsTable.addCell(c1);
                        c1 = new PdfPCell(new Phrase("End Date", tableHeadFont));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c1.setBackgroundColor(new BaseColor(255, 227, 192));
                        c1.setPadding(10f);
                        requestsTable.addCell(c1);
                        c1 = new PdfPCell(new Phrase("Substitute", tableHeadFont));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c1.setBackgroundColor(new BaseColor(255, 227, 192));
                        c1.setPadding(10f);
                        requestsTable.addCell(c1);
                        c1 = new PdfPCell(new Phrase("Type", tableHeadFont));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c1.setBackgroundColor(new BaseColor(255, 227, 192));
                        c1.setPadding(10f);
                        requestsTable.addCell(c1);
                        c1 = new PdfPCell(new Phrase("Status", tableHeadFont));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c1.setBackgroundColor(new BaseColor(255, 227, 192));
                        c1.setPadding(10f);
                        requestsTable.addCell(c1);
                        c1 = new PdfPCell(new Phrase("Details", tableHeadFont));
                        c1.setBackgroundColor(new BaseColor(255, 227, 192));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c1.setPadding(10f);
                        requestsTable.addCell(c1);


                        List<Holiday> holidays = new ArrayList<>();
                        holidays.addAll(this.holidayRepository.findByUserId(user.getId()));
                        int i = 0;
                        for (Holiday h : holidays) {
                            if (h.getStatus() != HolidayStatus.DENIED) {
                                i++;
                                PdfPCell startDateCell = new PdfPCell(Phrase.getInstance(h.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                                PdfPCell endDateCell = new PdfPCell(Phrase.getInstance(h.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

                                PdfPCell typeCell = new PdfPCell(Phrase.getInstance(h.getType().toString()));
                                PdfPCell statusCell = new PdfPCell(Phrase.getInstance(h.getStatus().toString()));


                                startDateCell.setPadding(7f);
                                endDateCell.setPadding(7f);
                                typeCell.setPadding(7f);
                                statusCell.setPadding(7f);

                                startDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                endDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                                requestsTable.addCell(startDateCell);
                                requestsTable.addCell(endDateCell);
                                if (h.getSubstitute() != null) {
                                    PdfPCell substitutCell = new PdfPCell(Phrase.getInstance(h.getSubstitute()));
                                    substitutCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    substitutCell.setPadding(7f);
                                    requestsTable.addCell(substitutCell);
                                } else {
                                    PdfPCell substitutCell = new PdfPCell(Phrase.getInstance("-"));
                                    substitutCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    substitutCell.setPadding(7f);
                                    requestsTable.addCell(substitutCell);
                                }
                                requestsTable.addCell(typeCell);
                                requestsTable.addCell(statusCell);

                                if (h.getDetails() != null) {
                                    PdfPCell detailsCell = new PdfPCell(Phrase.getInstance(h.getDetails()));
                                    detailsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    requestsTable.addCell(detailsCell);
                                } else {
                                    PdfPCell detailsCell = new PdfPCell(Phrase.getInstance("-"));
                                    detailsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    requestsTable.addCell(detailsCell);
                                }
                            }
                        }
                        if (i == 0) {
                            PdfPCell startDateCell = new PdfPCell(Phrase.getInstance("-"));
                            PdfPCell endDateCell = new PdfPCell(Phrase.getInstance("-"));
                            PdfPCell substituteCell = new PdfPCell(Phrase.getInstance("-"));
                            PdfPCell typeCell = new PdfPCell(Phrase.getInstance("-"));
                            PdfPCell statusCell = new PdfPCell(Phrase.getInstance("-"));
                            PdfPCell detailsCell = new PdfPCell(Phrase.getInstance("-"));

                            detailsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            requestsTable.addCell(detailsCell);
                            startDateCell.setPadding(7f);
                            endDateCell.setPadding(7f);
                            substituteCell.setPadding(7f);
                            typeCell.setPadding(7f);
                            statusCell.setPadding(7f);

                            startDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            endDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            substituteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                            requestsTable.addCell(startDateCell);
                            requestsTable.addCell(endDateCell);
                            requestsTable.addCell(substituteCell);
                            requestsTable.addCell(typeCell);
                            requestsTable.addCell(statusCell);


                        }

                        try {
                            userParagraph.add(requestsTable);
                            document.add(new Paragraph(" "));
                            documentParagraph.add(userParagraph);
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        document.add(documentParagraph);
        //document.add(membersTable);


        document.close();
        return byteArrayOutputStream.toByteArray();
    }
}