package com.internship.holiday_manager.service.teamlead_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.HolidayType;
import com.internship.holiday_manager.mapper.HolidayMapper;
import com.internship.holiday_manager.repository.HolidayRepository;
import com.internship.holiday_manager.repository.TeamRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
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

    @Override
    public ByteArrayOutputStream getPDF(Long teamId) throws DocumentException {
      List<HolidayDto> holidays= getTeamRequests(teamId);
        Document document = new Document();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, stream);



        document.open();

        document.close();



return stream;
    }

}
