package org.isfce.pdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.isfce.pdb.model.Adresse;
import org.isfce.pdb.model.Installation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLInstallationDao implements IInstallationDao {

    private static String SQL_GET_FROM_ID = """
            SELECT NUM_INS, DATE_INS, ADRESSE_INS, CP_INS, VILLE_INS, INSTALLATEUR_INS, PROPRIETAIRE_INS
            FROM TINSTALLATION WHERE NUM_INS = ?
            """;

    private static String SQL_GET_LISTE = """
            SELECT NUM_INS, DATE_INS, ADRESSE_INS, CP_INS, VILLE_INS, INSTALLATEUR_INS, PROPRIETAIRE_INS
            FROM TINSTALLATION ORDER BY DATE_INS DESC
            """;

    private static String SQL_UPDATE = """
            UPDATE TINSTALLATION SET DATE_INS = ?, INSTALLATEUR_INS = ?, PROPRIETAIRE_INS = ?
            WHERE NUM_INS = ?
            """;

    private DAOFactory factory;
    private Connection connexion;

    public SQLInstallationDao(DAOFactory factory) {
        this.factory = factory;
        this.connexion = factory.getConnection();
    }

    private Installation buildInstallation(ResultSet rs) throws SQLException {
        Adresse adresse = new Adresse(
                rs.getString("ADRESSE_INS"),
                rs.getInt("CP_INS"),
                rs.getString("VILLE_INS")
        );
        return Installation.builder()
                .id(rs.getInt("NUM_INS"))
                .date(rs.getDate("DATE_INS").toLocalDate())
                .adresse(adresse)
                .Installateur(rs.getString("INSTALLATEUR_INS"))
                .proprietaire(rs.getString("PROPRIETAIRE_INS"))
                .build();
    }

    @Override
    public Optional<Installation> getFromID(Integer id) {
        Installation obj = null;
        try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_FROM_ID)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                obj = buildInstallation(rs);
                log.debug("Installation chargée: " + obj);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return Optional.ofNullable(obj);
    }

    @Override
    public List<Installation> getListe() {
        List<Installation> liste = new ArrayList<>();
        try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_LISTE)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(buildInstallation(rs));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return liste;
    }

    @Override
    public boolean update(Installation obj) throws Exception {
        boolean ok = false;
        try (PreparedStatement ps = connexion.prepareStatement(SQL_UPDATE)) {
            ps.setDate(1, java.sql.Date.valueOf(obj.getDate()));
            ps.setString(2, obj.getInstallateur());
            ps.setString(3, obj.getProprietaire());
            ps.setInt(4, obj.getId());
            int nb = ps.executeUpdate();
            if (nb == 1) {
                if (!connexion.getAutoCommit())
                    connexion.commit();
                ok = true;
            }
        } catch (SQLException e) {
            log.error("Mise à jour installation non validée: " + e);
            if (!connexion.getAutoCommit())
                connexion.rollback();
            factory.dispatchException(e, "[UPD] INSTALLATION");
        }
        return ok;
    }
}