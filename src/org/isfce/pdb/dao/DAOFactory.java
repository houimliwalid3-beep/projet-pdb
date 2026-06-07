package org.isfce.pdb.dao;

import java.sql.Connection;
import org.isfce.pdb.exceptions.InstallationException;

public abstract class DAOFactory {

    public enum TypePersistance {
        FIREBIRD, H2, POSTGRESQL
    }

    // DAO que doit fournir chaque fabrique concrète
    public abstract ITypePieceDao getTypePieceDAO();
    public abstract IPieceDao getPieceDAO();
    public abstract IPlanDao getPlanDAO();
    public abstract ISvgDao getSvgDAO();
    public abstract IAppareilDao getAppareilDAO();
    public abstract IElementDao getElementDAO();
    public abstract ILocalisationDao getLocalisationDAO();
    public abstract IInstallationDao getInstallationDAO();

    // Méthode statique qui génère des fabriques concrètes
    public static DAOFactory getDAOFactory(TypePersistance typeP, Connection connect) {
        switch (typeP) {
        case FIREBIRD:
            return new FBDAOFactory(connect);
        case H2:
            return null;
        case POSTGRESQL:
            return null;
        default:
            return null;
        }
    }

    // Retourne la connexion SQL
    public abstract Connection getConnection();

    // Transforme une exception SQL en exception applicative
    protected abstract void dispatchException(Exception e, String detail) throws InstallationException;
}