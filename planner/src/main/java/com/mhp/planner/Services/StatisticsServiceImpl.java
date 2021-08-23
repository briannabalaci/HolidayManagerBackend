package com.mhp.planner.Services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;
import com.mhp.planner.Entities.Event;
import com.mhp.planner.Entities.Invite;
import com.mhp.planner.Entities.InviteQuestionResponse;
import com.mhp.planner.Entities.Question;
import com.mhp.planner.Repository.EventRepository;
import com.mhp.planner.Repository.InviteQuestionRepository;
import com.mhp.planner.Repository.InvitesRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService{

    private final EventRepository eventRepository;
    private final InvitesRepository invitesRepository;

    //fonts
    private static final Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 24,
            Font.BOLD);
    private static final Font bigFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
            Font.NORMAL);
    private static final Font bigBold = new Font(Font.FontFamily.TIMES_ROMAN, 16,
            Font.BOLD);
    private static final Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 14,
            Font.NORMAL);
    private static final Font normalBold = new Font(Font.FontFamily.TIMES_ROMAN, 14,
            Font.BOLD);
    private static final Font normalItalic = new Font(Font.FontFamily.TIMES_ROMAN, 14,
            Font.ITALIC);
    private static final Font smallFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL);
    private static final Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);

    @Override
    public ByteArrayInputStream generatePDF(Long id) {
        //find event
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if(optionalEvent.isEmpty()) {
            //nasol
            return null; //should throw some kind of an error
        }
        else {
            Event event = optionalEvent.get();
            Document pdf = new Document();
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PdfWriter.getInstance(pdf, out);
                pdf.open();

                addHeaderPage(pdf, event);
                add3Spaces(pdf, event);
                addBasicDetails(pdf, event);
                add3Spaces(pdf, event);

                addTableForAccepted(pdf, event);
                addTableForDeclinedOrNotResponded(pdf, event, "declined");
                addTableForDeclinedOrNotResponded(pdf, event, "Not Accepted");

                pdf.close();

                return new ByteArrayInputStream(out.toByteArray());
            } catch (DocumentException | IOException e) {
                log.debug("Can't generate pdf");
                e.printStackTrace();
            }
            return null;
        }
    }

    void addHeaderPage(Document pdf, Event event) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        //add logo to the left
        Image img = Image.getInstance(String.valueOf(new ClassPathResource("images/mhp.png").getFile()));
        PdfPCell c1 = new PdfPCell(img, true);
        c1.setFixedHeight(70);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setPadding(0);
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        //add additional text to the right
        Paragraph p = new Paragraph();
        p.add(new Paragraph("Rocket Team\n", smallBold));
        p.add("Str. Onisifor Ghibu, Nr.20A\n");
        p.add("Cluj-Napoca, România\n");

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String strDate = dateFormat.format(new Date());

        p.add(strDate);
        c1 = new PdfPCell(p);
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c1.setPadding(0);
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        pdf.add(table);
    }

    void add3Spaces(Document pdf, Event event) throws DocumentException {
        Paragraph preface = new Paragraph();
        addEmptyLines(preface, 3);

        preface.setAlignment(Element.ALIGN_CENTER);
        pdf.add(preface);
    }

    void addBasicDetails(Document pdf, Event event) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        Paragraph basicDetails = new Paragraph();

        basicDetails.add(new Paragraph("Event Statistics Report", titleFont));
        basicDetails.add(new Phrase("\nAuthor: ", normalBold));
        basicDetails.add(new Phrase(event.getOrganizer().getFullName() +"\n", normalItalic));
        basicDetails.add(new Paragraph("\n\n\n"));
        addCustomParagraph(basicDetails, "Title: ", event.getTitle());
        addCustomParagraph(basicDetails, "Location: ", event.getLocation());
        addCustomParagraph(basicDetails, "Date: ", event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")));
        addCustomParagraph(basicDetails, "Dress code: ", event.getDressCode());

        PdfPCell c1 = new PdfPCell(basicDetails);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setPadding(0);
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        //add cover img to the right
        String onlyImg = event.getCover_image().replace("data:image/jpeg;base64,", "");
        onlyImg = onlyImg.replace("data:image/png;base64,", "");
        byte[] decodedImg = Base64.getDecoder().decode(onlyImg);
        Image img = Image.getInstance(decodedImg);
        c1 = new PdfPCell(img, true);
        c1.setFixedHeight(150);
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c1.setPadding(0);
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        pdf.add(table);
    }

    void addTableForAccepted(Document pdf, Event event) throws DocumentException {
        String status = "accepted";
        List<Invite> invites = event.getInvitesByStatus(status);

        Paragraph statusParagraph = new Paragraph("", normalFont);
        addCustomParagraph(statusParagraph, "Status: ", invites.size() + " " + status +"\n");    //should display how many accepted
        pdf.add(statusParagraph);

        PdfPTable table = new PdfPTable(1 + event.getQuestions().size());
        table.setWidthPercentage(100);

        PdfPCell c1 = new PdfPCell(new Phrase("Name", smallBold));
        c1.setPadding(3);
        table.addCell(c1);

        //add column for each question
        for(Question q : event.getQuestions()) {
            c1 = new PdfPCell(new Phrase(q.getText(), smallBold));
            c1.setPadding(3);
            table.addCell(c1);
        }

        //add row for each invite
        //for now add all to check the result


        for(Invite invite: invites) {
            c1 = new PdfPCell(new Phrase(invite.getUserInvited().getFullName(), smallFont));
            c1.setPadding(3);
            table.addCell(c1);
            List<InviteQuestionResponse> iqrs = invite.getInviteQuestionResponses();
            iqrs.sort((q1, q2) ->
                    Long.compare(q2.getId(), q1.getId()));
            for(InviteQuestionResponse iqr : iqrs) {
                System.out.println("iqr id: " + iqr.getId() + " " + iqr.getQuestion().getText() + " has answer " + iqr.getAnswer().getText());
                c1 = new PdfPCell(new Phrase(iqr.getAnswer().getText(), smallFont));
                c1.setPadding(3);
                table.addCell(c1);
            }
        }

        pdf.add(table);
    }

    void addTableForDeclinedOrNotResponded(Document pdf, Event event, String status) throws DocumentException {
        List<Invite> invites = event.getInvitesByStatus(status);

        Paragraph statusParagraph = new Paragraph("", normalFont);
        addCustomParagraph(statusParagraph, "\nStatus: ", invites.size() + " " + status +"\n");    //should display how many accepted
        pdf.add(statusParagraph);

        PdfPTable table = new PdfPTable(1);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(40);

        PdfPCell c1 = new PdfPCell(new Phrase("Name", smallBold));
        c1.setPadding(3);
        table.addCell(c1);

        for(Invite invite: invites) {
            c1 = new PdfPCell(new Phrase(invite.getUserInvited().getFullName(), smallFont));
            c1.setPadding(3);
            table.addCell(c1);
        }

        pdf.add(table);
    }

    void addCustomParagraph(Paragraph pharagraph, String str1, String str2) {
        Paragraph titleParagraph = new Paragraph("", normalFont);
        titleParagraph.add(new Phrase(str1, normalBold));
        titleParagraph.add(new Phrase(str2 + "\n", normalFont));
        pharagraph.add(titleParagraph);
    }

    private static void addEmptyLines(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
