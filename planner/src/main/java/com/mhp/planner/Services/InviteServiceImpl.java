package com.mhp.planner.Services;

import com.mhp.planner.Dtos.InviteQuestionResponseDto;
import com.mhp.planner.Dtos.InvitesDto;
import com.mhp.planner.Entities.Invite;
import com.mhp.planner.Entities.InviteQuestionResponse;
import com.mhp.planner.Mappers.InviteQuestionResponseMapper;
import com.mhp.planner.Mappers.InvitesMapper;
import com.mhp.planner.Repository.InviteQuestionRepository;
import com.mhp.planner.Repository.InvitesRepository;
import com.mhp.planner.Repository.UserRepository;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteServiceImpl implements InviteService {
    private final InvitesRepository invitesRepository;
    private final InvitesMapper invitesMapper;
    private final InviteQuestionResponseMapper inviteQuestionResponseMapper;
    private final UserRepository userRepository;
    private final InviteQuestionRepository inviteQuestionRepository;


    @Override
    @Transactional
    public InvitesDto updateInvite(InvitesDto invitesDto) throws NotFoundException {
        Optional<Invite> inviteOptional = invitesRepository.findById(invitesDto.getId());

        if (inviteOptional.isEmpty()) {
            throw new NotFoundException("User with id " + invitesDto.getId() + " not found!");
        } else {
            Invite invite = inviteOptional.get();
            System.out.println(invitesDto);

            invite.setStatus(invitesDto.getStatus());

            //set invite question response

            if(invitesDto.getInviteQuestionResponses().isEmpty()) {

                for(var response: invite.getInviteQuestionResponses())
                {
                    inviteQuestionRepository.deleteById(response.getId());
                }
                invite.getInviteQuestionResponses().clear();
            }
            else
            {
                invite.getInviteQuestionResponses().clear();
                invite.getInviteQuestionResponses().addAll(inviteQuestionResponseMapper.dto2entities(invitesDto.getInviteQuestionResponses()));
            }
            System.out.println(invite);
            Invite updatedEntity = invitesRepository.save(invite);

            return invitesMapper.entity2dto(updatedEntity);
        }
    }

    @Override
    public List<InviteQuestionResponseDto> getQuestionResponseById(Long id) throws NotFoundException {

        Optional<Invite> inviteOptional = invitesRepository.findById(id);

        if (inviteOptional.isEmpty()) {
            throw new NotFoundException("User with id " + id + " not found!");
        }
        else
        {
            List<InviteQuestionResponse> inviteQuestionResponses = inviteOptional.get().getInviteQuestionResponses();

            return inviteQuestionResponseMapper.entities2dtos(inviteQuestionResponses);
        }
    }


}
