package org.isfce.pdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.isfce.pdb.model.Installation;
import org.isfce.pdb.model.Plan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLPlanDao implements IPlanDao {

    private static String SQL_GET_FROM_ID = """
            SELECT ID_PLA, NOM_PLA, FKINSTALLATION_PLA
            FROM TPLAN WHERE ID_PLA = ?
            """;

    private static String SQL_GET_LISTE_FROM_INSTALLATION = """
            SELECT ID_PLA, NOM_PLA, FKINSTALLATION_PLA
            FROM TPLAN WHERE FKINSTALLATION_PLA = ?
            """;

    private static String SQL_INSERT = """
            INSERT INTO TPLAN (NOM_PLA, FKINSTALLATION_PLA)
            VALUES (?, ?)
            """;

    private DAOFactory factory;
    private Connection connexion;

    public SQLPlanDao(DAOFactory factory) {
        this.factory = factory;
        this.connexion = factory.getConnection();
    }

    @Override
    public Optional<Plan> getFromID(Integer id) {
        Plan obj = null;
        try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_FROM_ID)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                obj = new Plan(
                        rs.getInt("ID_PLA"),
                        rs.getString("NOM_PLA"),
                        null
                );
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return Optional.ofNullable(obj);
    }

    @Override
    public List<Plan> getListePlanFromInstallation(int installation) {
        List<Plan> liste = new ArrayList<>();
        try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_LISTE_FROM_INSTALLATION)) {
            ps.setInt(1, installation);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Plan obj = new Plan(
                        rs.getInt("ID_PLA"),
                        rs.getString("NOM_PLA"),
                        null
                );
                liste.add(obj);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return liste;
    }

    @Override
    public Plan insert(Plan obj) throws Exception {
        try (PreparedStatement ps = connexion.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, obj.getNom());
            ps.setInt(2, obj.getInstallation().getId());
            int nb = ps.executeUpdate();
            if (nb == 1) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    obj.setId(rs.getInt(1));
                    if (!connexion.getAutoCommit())
                        connexion.commit();
                }
            }
        } catch (SQLException e) {
            log.error("Insertion plan non validée: " + e);
            if (!connexion.getAutoCommit())
                connexion.rollback();
            factory.dispatchException(e, "[INS] PLAN");
        }
        return obj;
    }
}