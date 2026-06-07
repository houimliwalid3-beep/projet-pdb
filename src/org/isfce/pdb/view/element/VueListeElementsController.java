package org.isfce.pdb.view.element;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.isfce.pdb.controller.MainController;
import org.isfce.pdb.model.Element;
import org.isfce.pdb.model.Piece;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VueListeElementsController implements Initializable {

    @FXML
    private TableView<Element> tblElements;

    @FXML
    private TableColumn<Element, String> colCode;

    @FXML
    private TableColumn<Element, String> colAppareil;

    @FXML
    private TableColumn<Element, Integer> colQt;

    @FXML
    private TableColumn<Element, Piece> colPiece;

    @FXML
    private Button btValider;

    @FXML
    private Button btQuitter;

    private Stage stage;
    private MainController ctrl;
    private ObservableList<Element> obsElements;
    private ObservableList<Piece> obsPieces;

    private Map<Integer, Piece> mapUpdate = new HashMap<>();

    @FXML
    void actionValider(ActionEvent event) {
        for (Map.Entry<Integer, Piece> entry : mapUpdate.entrySet()) {
            Integer elementId = entry.getKey();
            Piece piece = entry.getValue();
            try {
                ctrl.getFacade().saveLocalisation(elementId, piece);
            } catch (Exception e) {
                ctrl.showErreur("Erreur: " + e.getMessage());
            }
        }
        mapUpdate.clear();
        stage.close();
    }

    @FXML
    void actionQuitter(ActionEvent event) {
        stage.close();
    }

    public void setUp(MainController ctrl, Stage stage) {
        this.ctrl = ctrl;
        this.stage = stage;

        List<Piece> listePieces = ctrl.getFacade().getListePieces();
        obsPieces = FXCollections.observableArrayList(listePieces);

        colPiece.setCellFactory(_ -> new TableCell<>() {
            private final ComboBox<Piece> combo = new ComboBox<>();

            {
                combo.setItems(obsPieces);
                combo.setConverter(new StringConverter<Piece>() {
                    @Override
                    public String toString(Piece p) {
                        return p != null ? p.getNom() : "";
                    }
                    @Override
                    public Piece fromString(String s) {
                        return null;
                    }
                });
                combo.valueProperty().addListener((_, _, newPiece) -> {
                    if (newPiece != null && getIndex() >= 0) {
                        Element element = getTableView().getItems().get(getIndex());
                        mapUpdate.put(element.getId(), newPiece);
                    }
                });
            }

            @Override
            protected void updateItem(Piece item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(combo);
                }
            }
        });

        List<Element> listeElements = ctrl.getFacade().getElements();
        obsElements = FXCollections.observableArrayList(listeElements);
        tblElements.setItems(obsElements);

        stage.setOnCloseRequest(_ -> stage.close());
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        colCode.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getCode()));
        colAppareil.setCellValueFactory(p -> new SimpleStringProperty(
                p.getValue().getAppareil() != null ? p.getValue().getAppareil().getNom() : ""));
        colQt.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getQt()).asObject());
        colPiece.setCellValueFactory(p -> new SimpleObjectProperty<>(null));
    }
}