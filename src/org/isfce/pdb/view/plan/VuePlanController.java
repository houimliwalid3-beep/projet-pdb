package org.isfce.pdb.view.plan;

import java.net.URL;
import java.util.ResourceBundle;

import org.isfce.pdb.controller.MainController;
import org.isfce.pdb.model.Plan;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VuePlanController implements Initializable {

    @FXML
    private TextField ztNom;

    private MainController ctrl;
    private Stage stage;

    @FXML
    public void actionAnnuler(ActionEvent event) {
        stage.close();
    }

    @FXML
    public void actionValider(ActionEvent event) {
        if (ztNom.getText().isBlank()) {
            ctrl.showErreur("Le nom est obligatoire");
            return;
        }
        try {
            Plan plan = new Plan(null, ztNom.getText().trim(),
                    ctrl.getFacade().getCurrentInstallation());
            ctrl.getFacade().insertPlan(plan);
            stage.close();
        } catch (Exception e) {
            ctrl.showErreur("Erreur: " + e.getMessage());
        }
    }

    public void setUp(MainController ctrl, Stage stage) {
        this.ctrl = ctrl;
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
    }
}