package dev.gustavoteixeira.api.votingsession.controller.agenda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gustavoteixeira.api.votingsession.dto.request.VoteRequestDTO;
import dev.gustavoteixeira.api.votingsession.exception.AgendaClosedException;
import dev.gustavoteixeira.api.votingsession.exception.AgendaNotFoundException;
import dev.gustavoteixeira.api.votingsession.exception.AssociateIsNotAbleToVoteException;
import dev.gustavoteixeira.api.votingsession.exception.VoteAlreadyExistsException;
import dev.gustavoteixeira.api.votingsession.service.AgendaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class VoteAgendaTest {

    public static final String AGENDA_ID = "608d817df3117478ca0f7432";
    public static final String NONEXISTENT_AGENDA_ID = "608ded0cc66aaf5bd61759de";
    public static final String ASSOCIATE_IDENTIFIER = "38347541027";
    public static final String INVALID_ASSOCIATE_IDENTIFIER = "0123456789";
    public static final String POSITIVE_CHOICE = "Sim";
    public static final String NEGATIVE_CHOICE = "Não";
    public static final String INVALID_CHOICE = "Talvez";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AgendaService agendaService;

    @Test
    void voteAgendaWithValidRequestShouldReturnOK() throws Exception {
        VoteRequestDTO requestBody = VoteRequestDTO.builder()
                .associate(ASSOCIATE_IDENTIFIER)
                .choice(POSITIVE_CHOICE).build();

        doNothing().when(agendaService).voteAgenda(eq(AGENDA_ID), any(VoteRequestDTO.class));

        mvc.perform(post("/agenda/".concat(AGENDA_ID).concat("/vote"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapToJson(requestBody)))
                .andExpect(status().isOk());
    }

    @Test
    void voteAgendaWithNonexistentAgendaRequestShouldReturnNotFound() throws Exception {
        VoteRequestDTO requestBody = VoteRequestDTO.builder()
                .associate(ASSOCIATE_IDENTIFIER)
                .choice(POSITIVE_CHOICE).build();

        doThrow(AgendaNotFoundException.class)
                .when(agendaService).voteAgenda(eq(NONEXISTENT_AGENDA_ID), any(VoteRequestDTO.class));

        mvc.perform(post("/agenda/".concat(NONEXISTENT_AGENDA_ID).concat("/vote"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapToJson(requestBody)))
                .andExpect(status().isNotFound());
    }

    @Test
    void voteAgendaWithClosedAgendaRequestShouldReturnNotAcceptable() throws Exception {
        VoteRequestDTO requestBody = VoteRequestDTO.builder()
                .associate(ASSOCIATE_IDENTIFIER)
                .choice(NEGATIVE_CHOICE).build();

        doThrow(AgendaClosedException.class)
                .when(agendaService).voteAgenda(eq(NONEXISTENT_AGENDA_ID), any(VoteRequestDTO.class));

        mvc.perform(post("/agenda/".concat(NONEXISTENT_AGENDA_ID).concat("/vote"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapToJson(requestBody)))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void voteAgendaWithVoteAlreadyRegisteredRequestShouldReturnNotAcceptable() throws Exception {
        VoteRequestDTO requestBody = VoteRequestDTO.builder()
                .associate(ASSOCIATE_IDENTIFIER)
                .choice(NEGATIVE_CHOICE).build();

        doThrow(VoteAlreadyExistsException.class)
                .when(agendaService).voteAgenda(eq(AGENDA_ID), any(VoteRequestDTO.class));

        mvc.perform(post("/agenda/".concat(AGENDA_ID).concat("/vote"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapToJson(requestBody)))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void voteAgendaWithInvalidChoiceRequestShouldReturnBadRequest() throws Exception {
        VoteRequestDTO requestBody = VoteRequestDTO.builder()
                .associate(ASSOCIATE_IDENTIFIER)
                .choice(INVALID_CHOICE).build();

        mvc.perform(post("/agenda/".concat(AGENDA_ID).concat("/vote"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapToJson(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void voteAgendaWithInvalidAssociateIdentifierShouldReturnBadRequest() throws Exception {
        VoteRequestDTO requestBody = VoteRequestDTO.builder()
                .associate(INVALID_ASSOCIATE_IDENTIFIER)
                .choice(POSITIVE_CHOICE).build();

        mvc.perform(post("/agenda/".concat(AGENDA_ID).concat("/vote"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapToJson(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void voteAgendaWithAssociateNotAbleToVoteShouldReturnForbidden() throws Exception {
        VoteRequestDTO requestBody = VoteRequestDTO.builder()
                .associate(ASSOCIATE_IDENTIFIER)
                .choice(NEGATIVE_CHOICE).build();

        doThrow(AssociateIsNotAbleToVoteException.class)
                .when(agendaService).voteAgenda(eq(AGENDA_ID), any(VoteRequestDTO.class));

        mvc.perform(post("/agenda/".concat(AGENDA_ID).concat("/vote"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapToJson(requestBody)))
                .andExpect(status().isForbidden());
    }

    protected String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }

}
