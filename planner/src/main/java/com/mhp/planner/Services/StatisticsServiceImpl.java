package com.mhp.planner.Services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;
import com.mhp.planner.Entities.*;
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
import java.util.*;
import java.util.List;

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
    public ByteArrayInputStream generatePDFByFilter(Long id, String filter) {
        //find event
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if(optionalEvent.isEmpty()) {
            return null;
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

                if(filter.equals("accepted") || filter.equals("all")) {
                    Paragraph p = new Paragraph("");
                    p = new Paragraph("Overview\n", normalBold);
                    pdf.add(p);

                    Map<Answers, Long> countOfAnswers = getCountOfAnswers(event.getInvitesByStatus("accepted"));
                    int i = 1;
                    for (Question question : event.getQuestions())
                        addTableForQuestion(pdf, question, i++, countOfAnswers);

                    p = new Paragraph("All responses\n", normalBold);
                    pdf.add(p);
                }

                if(filter.equals("accepted") || filter.equals("all"))
                    addTableForAccepted(pdf, event);

                if(filter.equals("declined") || filter.equals("all"))
                addTableForDeclinedOrNotResponded(pdf, event, "declined");

                if(filter.equals("pending") || filter.equals("all"))
                addTableForDeclinedOrNotResponded(pdf, event, "pending");

                pdf.close();

                return new ByteArrayInputStream(out.toByteArray());
            } catch (DocumentException | IOException e) {
                log.debug("Can't generate pdf");
                e.printStackTrace();
            }
            return null;
        }
    }

    private void addTableForQuestion(Document pdf, Question question, int questionNr, Map<Answers, Long> countOfAnswers) throws DocumentException {
        pdf.add(new Paragraph("Question" + questionNr + ": " + question.getText() + "\n\n", smallBold));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell c1 = new PdfPCell(new Phrase("Answer", smallBold));
        c1.setPadding(3);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Count", smallBold));
        c1.setPadding(3);
        table.addCell(c1);

        //add row for each answer
        for(Answers answer : question.getAnswerList()) {
            //add answer text
            c1 = new PdfPCell(new Phrase(answer.getText(), smallFont));
            c1.setPadding(3);
            table.addCell(c1);

            //add count
            c1 = new PdfPCell(new Phrase(countOfAnswers.getOrDefault(answer,0l).toString(), smallFont));
            c1.setPadding(3);
            table.addCell(c1);
        }

        pdf.add(table);
        pdf.add(new Paragraph("\n"));
    }

    private Map<Answers, Long> getCountOfAnswers(List<Invite> invites) {
        Map<Answers, Long> countOfAnswers = new HashMap<>();
        for(Invite invite:invites) {
            for(InviteQuestionResponse iqr : invite.getInviteQuestionResponses()) {
                countOfAnswers.put(iqr.getAnswer(), countOfAnswers.getOrDefault(iqr.getAnswer(), 0L) + 1);
            }
        }
        return countOfAnswers;
    }

    void addHeaderPage(Document pdf, Event event) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        //add logo to the left
//        Image img = Image.getInstance(String.valueOf(new ClassPathResource("images/mhp.png").getFile()));
        Image img = Image.getInstance(new URL("https://www.mhp.com/fileadmin/www.mhp.com/assets/og/mhp_opengraph.jpg"));
        PdfPCell c1 = new PdfPCell(img, true);
        c1.setFixedHeight(100);
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

        if(invites.size() == 0) return; //don't draw table headers if there is no data
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
        for(Invite invite: invites) {
            c1 = new PdfPCell(new Phrase(invite.getUserInvited().getFullName(), smallFont));
            c1.setPadding(3);
            table.addCell(c1);
            List<InviteQuestionResponse> iqrs = invite.getInviteQuestionResponses();
            iqrs.sort((q1, q2) ->
                    - Long.compare(q2.getId(), q1.getId()));
            for(InviteQuestionResponse iqr : iqrs) {
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

        if(invites.size() == 0) return; //don't draw table headers if there is no data
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
        Paragraph titleParagraph = new Paragraph("", smallFont);
        titleParagraph.add(new Phrase(str1, smallBold));
        titleParagraph.add(new Phrase(str2 + "\n", smallFont));
        pharagraph.add(titleParagraph);
    }

    private static void addEmptyLines(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
