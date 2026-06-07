package org.isfce.pdb.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.isfce.pdb.dao.DAOFactory;
import org.isfce.pdb.dao.DAOFactory.TypePersistance;
import org.isfce.pdb.databases.connexion.ConnexionFromFile;
import org.isfce.pdb.databases.connexion.ConnexionSingleton;
import org.isfce.pdb.databases.uri.Databases;
import org.isfce.pdb.exceptions.InstallationException;
import org.isfce.pdb.services.Facade;
import org.isfce.pdb.view.bundle.I18N;
import org.isfce.pdb.view.element.VueListeElementsController;
import org.isfce.pdb.view.piece.VueListePiecesController;
import org.isfce.pdb.view.piece.VuePieceController;
import org.isfce.pdb.view.plan.VueImplantationController;
import org.isfce.pdb.view.plan.VuePlanController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainController extends Application {
    private Facade facade;
    private DAOFactory factory;
    private Stage mainStage;
    private Stage vueImplantation;
    private BooleanProperty installationChargee = new SimpleBooleanProperty(false);

    @Override
    public void start(Stage mainStage) {
        Locale.setDefault(Locale.FRENCH);
        this.mainStage = mainStage;

        factory = connexionToDatabase();
        facade = new Facade(factory);

        BorderPane cp = new BorderPane();

        VBox leftPane = new VBox(10);
        leftPane.setFillWidth(true);
        leftPane.setPadding(new Insets(10));

        Button bt1 = new Button(I18N.getString("bt.load"));
        leftPane.getChildren().add(bt1);
        bt1.setOnAction(this::actionChargeInstallation);
        bt1.setMaxWidth(Double.MAX_VALUE);

        Button bt2 = new Button(I18N.getString("bt.cree.piece"));
        leftPane.getChildren().add(bt2);
        bt2.setOnAction(this::actionCreePiece);
        bt2.setMaxWidth(Double.MAX_VALUE);
        bt2.disableProperty().bind(installationChargee.not());

        Button bt3 = new Button(I18N.getString("bt.liste.piece"));
        leftPane.getChildren().add(bt3);
        bt3.setOnAction(this::actionListePieces);
        bt3.setMaxWidth(Double.MAX_VALUE);
        bt3.disableProperty().bind(installationChargee.not());

        Button bt4 = new Button(I18N.getString("bt.implantation"));
        leftPane.getChildren().add(bt4);
        bt4.setOnAction(this::actionImplantation);
        bt4.setMaxWidth(Double.MAX_VALUE);
        bt4.disableProperty().bind(installationChargee.not());

        Button bt5 = new Button("Ajouter Plan");
        leftPane.getChildren().add(bt5);
        bt5.setOnAction(e -> showAddPlan());
        bt5.setMaxWidth(Double.MAX_VALUE);
        bt5.disableProperty().bind(installationChargee.not());

        Button bt6 = new Button(I18N.getString("bt.element"));
        leftPane.getChildren().add(bt6);
        bt6.setOnAction(e -> showListeElements());
        bt6.setMaxWidth(Double.MAX_VALUE);
        bt6.disableProperty().bind(installationChargee.not());

        cp.setLeft(leftPane);

        Scene scene = new Scene(cp, 500, 400);
        mainStage.setScene(scene);
        mainStage.setTitle("Projet PDB 2526");
        mainStage.show();
    }

    public void showAddPiece() {
        ResourceBundle bundle;
        Stage stage = new Stage();
        stage.initOwner(mainStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setX(100);
        stage.setY(50);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/piece/VuePiece.fxml"));

        try {
            bundle = I18N.getInstance().getGlobalBundle();
            loader.setResources(bundle);
            stage.setTitle(bundle.getString("piece.titre"));
        } catch (Exception e) {
            log.error("Imposible de charger le bundle pour la vue Piece" + e.getMessage());
            showErreur("Impossible de charger le bundle ");
            stage.setTitle("Vue Piece");
        }

        AnchorPane root;
        try {
            root = loader.load();
            VuePieceController ctrl = loader.getController();
            ctrl.setUp(this, stage);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            log.error("Imposible de charger la vue Piece");
            showErreur("Impossible de charger la vue Piece: " + e.getMessage());
        }
        stage = null;
    }

    private void showListePieces() {
        ResourceBundle bundle;
        Stage stage = new Stage();
        stage.initOwner(mainStage);
        stage.setX(100);
        stage.setY(50);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/piece/VueListePieces.fxml"));

        try {
            bundle = I18N.getInstance().getGlobalBundle();
            loader.setResources(bundle);
            stage.setTitle(bundle.getString("piece.liste.titre"));
        } catch (Exception e) {
            log.error("Imposible de charger le bundle pour la vue liste pieces" + e.getMessage());
            showErreur("Impossible de charger le bundle ");
            stage.setTitle("Vue Liste Pieces");
        }

        AnchorPane root;
        try {
            root = loader.load();
            VueListePiecesController ctrl = loader.getController();
            ctrl.setUp(this, stage);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            log.error("Imposible de charger la vue ListePieces");
            showErreur("Impossible de charger la vue ListePieces: " + e.getMessage());
        }
        stage = null;
    }

    private void showImplantation() {
        if (vueImplantation != null) {
            vueImplantation.show();
            return;
        }
        ResourceBundle bundle;
        Stage stage = new Stage();
        stage.initOwner(mainStage);
        stage.setX(0);
        stage.setY(0);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/plan/VueImplantation.fxml"));

        try {
            bundle = I18N.getInstance().getGlobalBundle();
            loader.setResources(bundle);
            stage.setTitle(bundle.getString("implantation.titre"));
        } catch (Exception e) {
            log.error("Imposible de charger le bundle pour la vue implantation" + e.getMessage());
            showErreur("Impossible de charger le bundle ");
            stage.setTitle("Vue Implantation");
        }

        BorderPane root;
        try {
            root = loader.load();
            VueImplantationController ctrl = loader.getController();
            ctrl.setUp(this, stage);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
            stage.setScene(scene);
            vueImplantation = stage;
            stage.show();
        } catch (Exception e) {
            log.error("Imposible de charger la vue Implantation: " + e.getMessage(), e);
            showErreur("Impossible de charger la vue Implantation: " + e.getMessage());
        }
        stage = null;
    }

    private void showAddPlan() {
        ResourceBundle bundle;
        Stage stage = new Stage();
        stage.initOwner(mainStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setX(100);
        stage.setY(50);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/plan/VuePlan.fxml"));

        try {
            bundle = I18N.getInstance().getGlobalBundle();
            loader.setResources(bundle);
            stage.setTitle("Ajouter un plan");
        } catch (Exception e) {
            log.error("Impossible de charger le bundle" + e.getMessage());
            stage.setTitle("Ajouter un plan");
        }

        AnchorPane root;
        try {
            root = loader.load();
            VuePlanController ctrl = loader.getController();
            ctrl.setUp(this, stage);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            resetVueImplantation();
        } catch (IOException e) {
            log.error("Impossible de charger la vue Plan");
            showErreur("Impossible de charger la vue Plan: " + e.getMessage());
        }
        stage = null;
    }

    private void showListeElements() {
        ResourceBundle bundle;
        Stage stage = new Stage();
        stage.initOwner(mainStage);
        stage.setX(100);
        stage.setY(50);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/element/VueListeElements.fxml"));

        try {
            bundle = I18N.getInstance().getGlobalBundle();
            loader.setResources(bundle);
            stage.setTitle("Éléments");
        } catch (Exception e) {
            stage.setTitle("Éléments");
        }

        AnchorPane root;
        try {
            root = loader.load();
            VueListeElementsController ctrl = loader.getController();
            ctrl.setUp(this, stage);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            log.error("Impossible de charger la vue Elements");
            showErreur("Impossible de charger la vue Elements: " + e.getMessage());
        }
        stage = null;
    }

    public boolean showConfirmation(String message) {
        Alert a = new Alert(AlertType.CONFIRMATION, message);
        Optional<ButtonType> result = a.showAndWait();
        return result.get() == ButtonType.OK;
    }

    public void showErreur(String message) {
        Alert a = new Alert(AlertType.ERROR, message);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Facade getFacade() {
        return facade;
    }

    public void resetVueImplantation() {
        if (vueImplantation != null) {
            vueImplantation.close();
            vueImplantation = null;
        }
    }

    private DAOFactory connexionToDatabase() {
        DAOFactory factory = null;
        try {
            ConnexionSingleton.setInfoConnexion(
                    new ConnexionFromFile("./ressources/connexionPDB2526.properties", Databases.FIREBIRD));
            Connection connect = ConnexionSingleton.getConnexion();
            log.info("Connexion établie");
            factory = DAOFactory.getDAOFactory(TypePersistance.FIREBIRD, connect);
        } catch (Exception e) {
            showErreur("Problème de connexion");
            Platform.exit();
        }
        return factory;
    }

    public void actionChargeInstallation(ActionEvent event) {
        TextInputDialog txtI = new TextInputDialog("1");
        txtI.setHeaderText(I18N.getString("inst.id"));
        txtI.showAndWait().ifPresent(s -> {
            try {
                Integer i = Integer.parseInt(s);
                facade.chargeInstallation(i);
                installationChargee.set(true);
            } catch (InstallationException e) {
                showErreur(e.getMessage());
            } catch (NumberFormatException e2) {
                showErreur("Doit être un entier");
            }
        });
    }

    public void actionCreePiece(ActionEvent event) {
        showAddPiece();
    }

    public void actionListePieces(ActionEvent event) {
        showListePieces();
    }

    public void actionImplantation(ActionEvent event) {
        showImplantation();
    }
}