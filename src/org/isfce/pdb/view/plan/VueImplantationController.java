package org.isfce.pdb.view.plan;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import org.isfce.pdb.controller.MainController;
import org.isfce.pdb.exceptions.InstallationException;
import org.isfce.pdb.model.Element;
import org.isfce.pdb.model.Plan;
import org.isfce.pdb.services.Facade;
import org.isfce.pdb.view.bundle.I18N;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VueImplantationController implements Initializable {

    @FXML
    private Button btAnnuler;

    @FXML
    private Button btValider;

    @FXML
    private Canvas canvas;

    @FXML
    private ScrollPane spCanvas;

    @FXML
    private ComboBox<Plan> cbPlans;

    @FXML
    private ListView<Element> lstElements;

    private Stage stage;
    private MainController ctrl;
    private Facade facade;

    private Optional<Plan> planCharge = Optional.empty();
    private Map<Integer, Canvas> planCanvas = new HashMap<>();
    private GraphicsContext gc;
    private ObservableList<Element> obsElements = FXCollections.observableArrayList();
    private Map<Integer, double[]> mapPositions = new HashMap<>();

    @FXML
    void actionQuitter(ActionEvent event) {
        stage.close();
        ctrl.resetVueImplantation();
    }

    @FXML
    void actionValider(ActionEvent event) {
        for (Map.Entry<Integer, double[]> entry : mapPositions.entrySet()) {
            Integer elementId = entry.getKey();
            double[] pos = entry.getValue();
            try {
                ctrl.getFacade().savePosition(elementId, pos[0], pos[1]);
            } catch (Exception e) {
                ctrl.showErreur("Erreur: " + e.getMessage());
            }
        }
        mapPositions.clear();
        stage.close();
        ctrl.resetVueImplantation();
    }

    @FXML
    void actionChargePlan(ActionEvent event) {
        planCharge = Optional.ofNullable(cbPlans.getValue());
        if (planCharge.isPresent()) {
            canvas = planCanvas.get(planCharge.get().getId());
            gc = canvas.getGraphicsContext2D();
            spCanvas.setContent(canvas);
            obsElements.clear();
            facade.getPiecesPlan(planCharge.get()).forEach(piece ->
                obsElements.addAll(facade.getElementPiece(piece))
            );
            // Redessine les symboles déjà placés
            obsElements.forEach(elem -> {
                double[] pos = facade.getPosition(elem.getId());
                if (pos != null) {
                    dessineAppareil(elem, pos[0], pos[1]);
                }
            });
        }
    }

    public void setUp(MainController ctrl, Stage stage) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.facade = ctrl.getFacade();

        List<Plan> listePlan = facade.getListePlans();
        ObservableList<Plan> obsPlan = FXCollections.observableArrayList();
        planCanvas.clear();
        String basePath;
        try {
            basePath = facade.getProperties().getProperty("imagesPath") + facade.getCurrentInstallation().getId() + "/";
            log.info("basePath: " + basePath);

            for (Plan p : listePlan) {
                Path path = Path.of(basePath + p.getFichier());
                log.info("Chemin image: " + path);
                if (Files.exists(path)) {
                    InputStream stream;
                    try {
                        stream = Files.newInputStream(path);
                        Image image = new Image(stream);
                        Canvas canvas = new Canvas(image.getWidth(), image.getHeight());
                        canvas.setOnMouseClicked(event -> {
                            Element selected = lstElements.getSelectionModel().getSelectedItem();
                            if (selected != null) {
                                dessineAppareil(selected, event.getX(), event.getY());
                                mapPositions.put(selected.getId(), new double[]{event.getX(), event.getY()});
                            }
                        });
                        canvas.getGraphicsContext2D().drawImage(image, 0, 0);
                        planCanvas.put(p.getId(), canvas);
                        obsPlan.add(p);
                        log.info("Image chargée: " + p.getFichier());
                    } catch (IOException e) {
                        ctrl.showErreur("Fichier introuvable: " + p.getFichier());
                        log.error(p.getFichier());
                        cbPlans.getItems().remove(p);
                    }
                } else {
                    log.error("Fichier inexistant: " + path);
                }
            }

            cbPlans.setItems(obsPlan);
            cbPlans.setConverter(new StringConverter<Plan>() {
                @Override
                public String toString(Plan p) {
                    return p != null ? p.getNom() : "";
                }
                @Override
                public Plan fromString(String s) {
                    return null;
                }
            });
            lstElements.setItems(obsElements);
            lstElements.setCellFactory(_ -> new ListCell<Element>() {
                @Override
                protected void updateItem(Element item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getCode() + " - " + item.getAppareil().getNom());
                    }
                }
            });
        } catch (InstallationException e) {
            ctrl.showErreur(I18N.getString("err.noInstall"));
        }
    }

    private void dessineAppareil(Element elem, double x, double y) {
        double scale = elem.getAppareil().getSvg().getScale(50);
        gc.save();
        gc.setLineWidth(0.1);
        gc.setStroke(Color.AQUAMARINE);
        gc.setFill(Color.BLACK);
        gc.beginPath();
        gc.moveTo(x, y);
        gc.scale(scale, scale);
        gc.rotate(-90);
        gc.appendSVGPath(elem.getAppareil().getSvg().svg());
        gc.closePath();
        gc.stroke();
        gc.fill();
        gc.restore();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}