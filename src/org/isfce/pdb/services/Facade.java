package org.isfce.pdb.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Flow.Subscriber;

import org.isfce.pdb.dao.DAOFactory;
import org.isfce.pdb.dao.SQLLocalisationDao;
import org.isfce.pdb.dao.SQLPieceDao;
import org.isfce.pdb.exceptions.InstallationException;
import org.isfce.pdb.model.Adresse;
import org.isfce.pdb.model.Appareil;
import org.isfce.pdb.model.Element;
import org.isfce.pdb.model.Installation;
import org.isfce.pdb.model.Localisation;
import org.isfce.pdb.model.Piece;
import org.isfce.pdb.model.Plan;
import org.isfce.pdb.model.Svg;
import org.isfce.pdb.model.TypePiece;
import org.isfce.pdb.services.ListMessages.Classe;
import org.isfce.pdb.services.ListMessages.Evenement;
import org.isfce.pdb.view.bundle.I18N;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Facade {
    private DAOFactory factory;
    private Installation installation;
    private PublisherEvent publisher;

    private List<Plan> plans = new ArrayList<Plan>();
    private List<Piece> pieces = new ArrayList<Piece>();
    private List<Element> elements = new ArrayList<Element>();

    @Getter
    private Properties properties = new Properties();

    public Facade(DAOFactory factory) {
        this.factory = factory;
        publisher = new PublisherEvent();
        chargerProperties();
    }

    public void chargeInstallation(int id) throws InstallationException {
        if (id == 1 || id == 3) {
            installation = Installation.builder()
                    .adresse(new Adresse("Rue J buedts", 1040, "Etterbeek"))
                    .date(LocalDate.of(2026, 5, 12))
                    .id(id)
                    .Installateur("moi")
                    .proprietaire("truc")
                    .build();
            plans.clear();
            pieces.clear();
            elements.clear();
            plans.add(new Plan(1, "Rez", installation));
            plans.add(new Plan(2, "Etage1", installation));
            pieces.add(Piece.builder().id(8).description("Salle à manger et salon").etage(BigDecimal.ZERO)
                    .installation(id).nom("Séjour").typePiece(new TypePiece("LIVING", "Living", false))
                    .plan(plans.get(0)).build());
            pieces.add(Piece.builder().id(101).description("Cuisine").etage(BigDecimal.ZERO).installation(id)
                    .nom("Cuisine").typePiece(new TypePiece("CUISINE", "Cuisine", true)).plan(plans.get(0)).build());
            pieces.add(Piece.builder().id(102).description("Chambre 1").etage(BigDecimal.ONE).installation(id)
                    .nom("Chambre1").typePiece(new TypePiece("CHB", "Chambre", false)).plan(plans.get(1)).build());
            pieces.add(Piece.builder().id(103).description("Chambre 2").etage(BigDecimal.ONE).installation(id)
                    .nom("Chambre2").typePiece(new TypePiece("CHB", "Chambre", false)).plan(plans.get(1)).build());
            Svg priseSvg = new Svg("PC11",
                    "m0 0 0-.1 2.7 0 0-1.6.2 0 0 1.6c0-1.1.9-2 2-2l0-1 .2 0 0 1.2-.2 0c-1 0-1.8.9-1.8 1.8l0 .2c0 1.2.8 1.8 1.8 1.8l.2 0 0 1.2-.2 0 0-1c-1 0-2-.6-2-2l0 1.6-.2 0 0-1.6-2.7 0 0-.1m3.1 0",
                    0.0, -3.1, 5.1, 6.2);
            Appareil appPrise = new Appareil("PC11", "Prise Classique terre/Enf", priseSvg, Appareil.Classe.PRISE);
            Element el1 = new Element(1, appPrise, 1, "A1", "Prise cuisine", 0,
                    new Localisation(pieces.get(0), 0.0, 0.0, 0.0, false));
            Element el2 = new Element(14, appPrise, 2, "C5", "Prise double", 0,
                    new Localisation(pieces.get(0), 0.0, 0.0, 0.0, false));
            elements.add(el1);
            elements.add(el2);
        } else
            throw new InstallationException(I18N.getString("err.install.inconnue"));
    }

    public Installation getCurrentInstallation() throws InstallationException {
        if (installation == null)
            throw new InstallationException(I18N.getString("err.noInstall"));
        return installation;
    }

    public List<TypePiece> getTypePiece() {
        return factory.getTypePieceDAO().getListe(null);
    }

    public void insertPiece(Piece piece) throws InstallationException {
        try {
            factory.getPieceDAO().insert(piece);
            Map<Classe, Evenement> messages = new HashMap<>();
            messages.put(Classe.PIECE, new Evenement(TypeOperation.INSERT, piece));
            publisher.submit(new ListMessages(messages));
        } catch (Exception e) {
            if (e instanceof InstallationException exception)
                throw exception;
        }
    }

    public List<Piece> getListePieces() {
        if (installation != null)
            return factory.getPieceDAO().getListeFromInstallation(installation.getId());
        else
            return List.of();
    }

    public boolean deletePiece(Piece piece) throws InstallationException {
        boolean ok = false;
        try {
            ok = factory.getPieceDAO().delete(piece);
            Map<Classe, Evenement> messages = new HashMap<>();
            messages.put(Classe.PIECE, new Evenement(TypeOperation.DELETE, piece));
            publisher.submit(new ListMessages(messages));
        } catch (Exception e) {
            if (e instanceof InstallationException exc)
                throw exc;
        }
        return ok;
    }

    public boolean updatePiece(Piece piece) throws InstallationException {
        boolean ok = false;
        try {
            factory.getPieceDAO().update(piece);
            Map<Classe, Evenement> messages = new HashMap<>();
            messages.put(Classe.PIECE, new Evenement(TypeOperation.UPDATE, piece));
            publisher.submit(new ListMessages(messages));
        } catch (Exception e) {
            if (e instanceof InstallationException exc)
                throw exc;
        }
        return ok;
    }

    public Optional<Piece> getPiece(Integer id) {
        return factory.getPieceDAO().getFromID(id);
    }

    public List<Plan> getListePlans() {
        if (installation != null)
            return factory.getPlanDAO().getListePlanFromInstallation(installation.getId());
        else
            return plans;
    }

    public List<Piece> getPiecesPlan(Plan plan) {
        if (plan.getId() != null)
            return ((SQLPieceDao) factory.getPieceDAO()).getListeFromPlan(plan.getId());
        else
            return pieces.stream().filter(p -> plan.equals(p.getPlan())).toList();
    }

    public List<Element> getElementPiece(Piece piece) {
        if (piece != null && piece.getId() != null) {
            SQLLocalisationDao locDao = (SQLLocalisationDao) factory.getLocalisationDAO();
            return factory.getElementDAO().getListeFromInstallation(installation.getId())
                    .stream()
                    .filter(e -> {
                        try {
                            return locDao.getFromID(e.getId(), piece.getId()).isPresent();
                        } catch (Exception ex) {
                            return false;
                        }
                    })
                    .toList();
        }
        return elements.stream()
                .filter(e -> piece.equals(e.getLocalisation() == null ? null : e.getLocalisation().getPiece()))
                .toList();
    }

    public List<Element> getElements() {
        if (installation != null)
            return factory.getElementDAO().getListeFromInstallation(installation.getId());
        else
            return elements;
    }

    public List<Installation> getListeInstallations() {
        return factory.getInstallationDAO().getListe();
    }

    public void insertPlan(Plan plan) throws Exception {
        factory.getPlanDAO().insert(plan);
        plans.add(plan);
    }

    public void saveLocalisation(int elementId, Piece piece) throws Exception {
        log.info("saveLocalisation: elementId=" + elementId + " pieceId=" + piece.getId());
        SQLLocalisationDao locDao = (SQLLocalisationDao) factory.getLocalisationDAO();
        Localisation loc = new Localisation(piece, 0.0, 0.0, 0.0, false);
        Optional<Localisation> existing = locDao.getFromID(elementId, piece.getId());
        if (existing.isPresent()) {
            locDao.update(elementId, loc);
        } else {
            locDao.insert(elementId, loc);
        }
    }

    public void savePosition(int elementId, double x, double y) throws Exception {
        SQLLocalisationDao locDao = (SQLLocalisationDao) factory.getLocalisationDAO();
        locDao.updatePosition(elementId, x, y);
    }

    public void addObserver(Subscriber<ListMessages> obs) {
        publisher.addObserver(obs);
    }

    private void chargerProperties() {
        String fichier = "./ressources/installation.properties";
        try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
            properties.load(br);
        } catch (IOException e) {
            log.error("Problème de chargement du fichier " + fichier + " : " + e.getMessage());
        }
    }
    
    public double[] getPosition(int elementId) {
        SQLLocalisationDao locDao = (SQLLocalisationDao) factory.getLocalisationDAO();
        return locDao.getPosition(elementId);
    }
}