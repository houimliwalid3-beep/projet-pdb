package org.isfce.pdb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Plan {
    private Integer id;
    private String nom;
    private Installation installation;

    public String getFichier() {
        return nom.toLowerCase().replace(" ", "") + ".png";
    }
}