package com.example.producer.controller;

import com.example.producer.model.MatchResult;
import com.example.producer.service.MatchResultService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProducerController.class)
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class ProducerControllerTest {

    @MockBean
    private MatchResultService matchResultService;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(matchResultService.createMatchResult(any(MatchResult.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    }

    @Test
    public void shouldCreateMatchResult() throws Exception {
        //Given
        MatchResult matchResult = MatchResult.builder().localTeam("A").visitorTeam("B").result("1-3").build();

        //When && Then
        mockMvc.perform(post("/matchResult")
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(matchResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localTeam").value("A"))
                .andExpect(jsonPath("$.visitorTeam").value("B"))
                .andExpect(jsonPath("$.result").value("1-3"));
        verify(matchResultService, times(1)).createMatchResult(any(MatchResult.class));
    }

}