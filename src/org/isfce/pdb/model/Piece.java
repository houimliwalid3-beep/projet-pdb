package org.isfce.pdb.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@EqualsAndHashCode
@Getter
@Setter
public class Piece implements Cloneable {
    private Integer id;
    private String nom;
    private String description;
    private BigDecimal etage;
    private TypePiece typePiece;
    private final Integer installation;
    private Plan plan;
}