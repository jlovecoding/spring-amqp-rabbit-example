package com.example.producer.model;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult implements Serializable {

    private String localTeam;
    private String visitorTeam;
    private String result;
}