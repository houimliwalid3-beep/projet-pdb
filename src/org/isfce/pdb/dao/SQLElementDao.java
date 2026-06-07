package org.isfce.pdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.isfce.pdb.model.Appareil;
import org.isfce.pdb.model.Element;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLElementDao implements IElementDao {

    private static String SQL_GET_LISTE_FROM_INSTALLATION = """
            SELECT ID_ELE, FKAPPAREIL_ELE, QT_ELE, CODE_ELE, INFO_ELE, ORDRE_ELE
            FROM TELEMENT WHERE FKINSTALLATION_ELE = ?
            ORDER BY CODE_ELE, ORDRE_ELE
            """;

    private DAOFactory factory;
    private Connection connexion;

    public SQLElementDao(DAOFactory factory) {
        this.factory = factory;
        this.connexion = factory.getConnection();
    }

    @Override
    public List<Element> getListeFromInstallation(int installation) {
        List<Element> liste = new ArrayList<>();
        try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_LISTE_FROM_INSTALLATION)) {
            ps.setInt(1, installation);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String codeApp = rs.getString("FKAPPAREIL_ELE");
                Appareil appareil = factory.getAppareilDAO().getFromID(codeApp).orElse(null);
                Element obj = Element.builder()
                        .id(rs.getInt("ID_ELE"))
                        .appareil(appareil)
                        .qt(rs.getInt("QT_ELE"))
                        .code(rs.getString("CODE_ELE"))
                        .info(rs.getString("INFO_ELE"))
                        .ordre(rs.getInt("ORDRE_ELE"))
                        .build();
                liste.add(obj);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return liste;
    }
}