package org.isfce.pdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.isfce.pdb.model.Appareil;
import org.isfce.pdb.model.Appareil.Classe;
import org.isfce.pdb.model.Svg;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLAppareilDao implements IAppareilDao {

    private static String SQL_GET_FROM_ID = """
            SELECT CODE_APP, NOM_APP, FKSVG_APP, CLASSE_APP
            FROM TAPPAREIL WHERE CODE_APP = ?
            """;

    private DAOFactory factory;
    private Connection connexion;

    public SQLAppareilDao(DAOFactory factory) {
        this.factory = factory;
        this.connexion = factory.getConnection();
    }

    @Override
    public Optional<Appareil> getFromID(String id) {
        Appareil obj = null;
        try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_FROM_ID)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String codeSvg = rs.getString("FKSVG_APP");
                Svg svg = factory.getSvgDAO().getFromID(codeSvg).orElse(null);
                obj = new Appareil(
                        rs.getString("CODE_APP").trim(),
                        rs.getString("NOM_APP"),
                        svg,
                        Classe.valueOf(rs.getString("CLASSE_APP").trim())
                );
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return Optional.ofNullable(obj);
    }
}