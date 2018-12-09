package com.example.producer.service;

import com.example.producer.model.MatchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class MatchResultServiceTest {

    @InjectMocks
    private MatchResultService matchResultService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Before
    public void setUp() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    public void shouldCreateMatchResult() {
        //Given
        MatchResult matchResult = MatchResult.builder().localTeam("A").visitorTeam("B").result("1-3").build();

        //When && Then
        assertThat(matchResultService.createMatchResult(matchResult)).isEqualTo(matchResult);
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(),
                any(Object.class));

    }
}