package org.isfce.pdb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Localisation {
    private Piece piece;
    private double x;
    private double y;
    private double angle;
    private boolean place;
}