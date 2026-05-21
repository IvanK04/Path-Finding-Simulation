package com.example.backend.dto;

import com.example.backend.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlgorithmEvent {
    private String type;
    private int x;
    private int y;
}
