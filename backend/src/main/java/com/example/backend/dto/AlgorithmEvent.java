package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlgorithmEvent {
    private String type;
    private int x;
    private int y;
}
