package org.isfce.pdb.view.piece;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.isfce.pdb.controller.MainController;
import org.isfce.pdb.exceptions.InstallationException;
import org.isfce.pdb.model.Piece;
import org.isfce.pdb.model.Plan;
import org.isfce.pdb.model.TypePiece;

import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class VuePieceController implements Initializable {

    private static final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

    @FXML
    private TextField ztNom;

    @FXML
    private TextField ztDescription;

    @FXML
    private Spinner<Double> spEtage;

    @FXML
    private ComboBox<TypePiece> cbTypePiece;

    @FXML
    private ComboBox<Plan> cbPlan;

    private MainController ctrl;
    private Stage stage;

    @FXML
    public void actionAnnuler(ActionEvent event) {
        this.stage.close();
    }

    @FXML
    public void actionValider(ActionEvent event) {
        Piece piece;
        boolean bad = checkData();
        if (!bad) {
            try {
                piece = Piece.builder()
                        .nom(ztNom.getText().trim())
                        .description(ztDescription.getText().trim())
                        .etage(BigDecimal.valueOf(spEtage.getValue()).setScale(1, RoundingMode.HALF_UP))
                        .typePiece(cbTypePiece.getValue())
                        .installation(ctrl.getFacade().getCurrentInstallation().getId())
                        .plan(cbPlan.getValue())
                        .build();
                this.ctrl.getFacade().insertPiece(piece);
                this.stage.close();
            } catch (InstallationException e) {
                ctrl.showErreur(e.getMessage());
            }
        }
    }

    private boolean checkData() {
        boolean bad = false;
        boolean erreur;
        erreur = ztNom.getText().isBlank();
        ztNom.pseudoClassStateChanged(errorClass, erreur);
        bad = bad || erreur;
        erreur = ztDescription.getText().isBlank();
        ztDescription.pseudoClassStateChanged(errorClass, erreur);
        bad = bad || erreur;
        erreur = !(cbTypePiece.getValue() instanceof TypePiece);
        cbTypePiece.pseudoClassStateChanged(errorClass, erreur);
        bad = bad || erreur;
        return bad;
    }

    public void setUp(MainController ctrl, Stage stage) {
        this.stage = stage;
        this.ctrl = ctrl;

        // Charge les types de pièces
        List<TypePiece> listeTypePiece = ctrl.getFacade().getTypePiece();
        cbTypePiece.setItems(FXCollections.observableArrayList(listeTypePiece));

        // Charge les plans
        List<Plan> listePlans = ctrl.getFacade().getListePlans();
        cbPlan.setItems(FXCollections.observableArrayList(listePlans));
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        SpinnerValueFactory<Double> vf = new SpinnerValueFactory.DoubleSpinnerValueFactory(-5.0, 10.0, 0, 0.5);
        spEtage.setValueFactory(vf);

        cbTypePiece.setEditable(false);
        cbTypePiece.setConverter(new StringConverter<TypePiece>() {
            @Override
            public String toString(TypePiece t) {
                if (t != null)
                    return t.getNom();
                return "";
            }
            @Override
            public TypePiece fromString(String arg0) {
                return null;
            }
        });

        cbPlan.setEditable(false);
        cbPlan.setConverter(new StringConverter<Plan>() {
            @Override
            public String toString(Plan p) {
                if (p != null)
                    return p.getNom();
                return "";
            }
            @Override
            public Plan fromString(String arg0) {
                return null;
            }
        });
    }
}