package org.isfce.pdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.isfce.pdb.model.Localisation;
import org.isfce.pdb.model.Piece;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLLocalisationDao implements ILocalisationDao {

    private static String SQL_GET_FROM_ID = """
            SELECT FKELEMENT_LOC, FKPIECE_LOC, X_LOC, Y_LOC, A_LOC
            FROM TLOCALISATION WHERE FKELEMENT_LOC = ? AND FKPIECE_LOC = ?
            """;

    private static String SQL_INSERT = """
            INSERT INTO TLOCALISATION (FKELEMENT_LOC, FKPIECE_LOC, X_LOC, Y_LOC, A_LOC)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static String SQL_UPDATE = """
            UPDATE TLOCALISATION SET X_LOC = ?, Y_LOC = ?, A_LOC = ?
            WHERE FKELEMENT_LOC = ? AND FKPIECE_LOC = ?
            """;

    private static String SQL_UPDATE_POSITION = """
            UPDATE TLOCALISATION SET X_LOC = ?, Y_LOC = ?
            WHERE FKELEMENT_LOC = ?
            """;

    private static String SQL_GET_POSITION = """
            SELECT X_LOC, Y_LOC FROM TLOCALISATION
            WHERE FKELEMENT_LOC = ? AND X_LOC > 0
            """;

    private static String SQL_DELETE = """
            DELETE FROM TLOCALISATION WHERE FKELEMENT_LOC = ?
            """;

    private DAOFactory factory;
    private Connection connexion;

    public SQLLocalisationDao(DAOFactory factory) {
        this.factory = factory;
        this.connexion = factory.getConnection();
    }

    @Override
    public Optional<Localisation> getFromID(Integer id) {
        throw new UnsupportedOperationException("Utiliser getFromID(elementId, pieceId)");
    }

    public Optional<Localisation> getFromID(int elementId, int pieceId) {
        Localisation obj = null;
        try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_FROM_ID)) {
            ps.setInt(1, elementId);
            ps.setInt(2, pieceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Piece piece = factory.getPieceDAO().getFromID(pieceId).orElse(null);
                obj = Localisation.builder()
                        .piece(piece)
                        .x(rs.getDouble("X_LOC"))
                        .y(rs.getDouble("Y_LOC"))
                        .angle(rs.getDouble("A_LOC"))
                        .place(true)
                        .build();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return Optional.ofNullable(obj);
    }

    public Localisation insert(int elementId, Localisation obj) throws Exception {
        try (PreparedStatement ps = connexion.prepareStatement(SQL_INSERT)) {
            ps.setInt(1, elementId);
            ps.setInt(2, obj.getPiece().getId());
            ps.setDouble(3, obj.getX());
            ps.setDouble(4, obj.getY());
            ps.setDouble(5, obj.getAngle());
            int nb = ps.executeUpdate();
            if (nb == 1 && !connexion.getAutoCommit())
                connexion.commit();
        } catch (SQLException e) {
            log.error("Insertion localisation non validée: " + e);
            if (!connexion.getAutoCommit())
                connexion.rollback();
            factory.dispatchException(e, "[INS] LOCALISATION");
        }
        return obj;
    }

    public boolean update(int elementId, Localisation obj) throws Exception {
        boolean ok = false;
        try (PreparedStatement ps = connexion.prepareStatement(SQL_UPDATE)) {
            ps.setDouble(1, obj.getX());
            ps.setDouble(2, obj.getY());
            ps.setDouble(3, obj.getAngle());
            ps.setInt(4, elementId);
            ps.setInt(5, obj.getPiece().getId());
            int nb = ps.executeUpdate();
            if (nb == 1) {
                if (!connexion.getAutoCommit())
                    connexion.commit();
                ok = true;
            }
        } catch (SQLException e) {
            log.error("Mise à jour localisation non validée: " + e);
            if (!connexion.getAutoCommit())
                connexion.rollback();
            factory.dispatchException(e, "[UPD] LOCALISATION");
        }
        return ok;
    }

    public boolean updatePosition(int elementId, double x, double y) throws Exception {
        boolean ok = false;
        try (PreparedStatement ps = connexion.prepareStatement(SQL_UPDATE_POSITION)) {
            ps.setDouble(1, x);
            ps.setDouble(2, y);
            ps.setInt(3, elementId);
            int nb = ps.executeUpdate();
            if (nb >= 1) {
                if (!connexion.getAutoCommit())
                    connexion.commit();
                ok = true;
            }
        } catch (SQLException e) {
            log.error("Mise à jour position non validée: " + e);
            if (!connexion.getAutoCommit())
                connexion.rollback();
            factory.dispatchException(e, "[UPD] POSITION");
        }
        return ok;
    }

    public double[] getPosition(int elementId) {
        try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_POSITION)) {
            ps.setInt(1, elementId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new double[]{rs.getDouble("X_LOC"), rs.getDouble("Y_LOC")};
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public boolean delete(int elementId) throws Exception {
        boolean ok = false;
        try (PreparedStatement ps = connexion.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, elementId);
            int nb = ps.executeUpdate();
            if (nb == 1) {
                if (!connexion.getAutoCommit())
                    connexion.commit();
                ok = true;
            }
        } catch (SQLException e) {
            log.error("Suppression localisation non validée: " + e);
            if (!connexion.getAutoCommit())
                connexion.rollback();
            factory.dispatchException(e, "[DEL] LOCALISATION");
        }
        return ok;
    }
}