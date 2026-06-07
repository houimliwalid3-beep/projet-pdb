package org.isfce.pdb.view.piece;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.isfce.pdb.controller.MainController;
import org.isfce.pdb.exceptions.InstallationException;
import org.isfce.pdb.model.Piece;
import org.isfce.pdb.model.TypePiece;
import org.isfce.pdb.services.Facade;
import org.isfce.pdb.services.ListMessages;
import org.isfce.pdb.services.ListMessages.Classe;
import org.isfce.pdb.services.ListMessages.Evenement;
import org.isfce.pdb.services.TypeOperation;
import org.isfce.pdb.view.bundle.I18N;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VueListePiecesController implements Initializable, Subscriber<ListMessages> {

	@FXML
	private Button btAjouterPiece;

	@FXML
	private Button btQuitter;

	@FXML
	private Button btRecharger;

	@FXML
	private Button btValider;

	@FXML
	private TableColumn<Piece, String> colDescription;

	@FXML
	private TableColumn<Piece, BigDecimal> colEtage;

	@FXML
	private TableColumn<Piece, String> colNom;

	@FXML
	private TableColumn<Piece, Void> colOperation;

	@FXML
	private TableColumn<Piece, TypePiece> colTypePiece;

	@FXML
	private TableView<Piece> tblPieces;

	private Stage stage;

	private MainController ctrl;

	private ObservableList<Piece> obsPieces;

	// pour la colonne TypePièce du tableau
	private ObservableList<TypePiece> typePieces = FXCollections.observableArrayList();

	// Pour connaître les lignes qui ont été modifiées
	private Map<Integer, Piece> mapUpdate = new HashMap<>();
	private BooleanProperty update = new SimpleBooleanProperty(false);

	// Pour gérer les évènements de publication
	private Subscription subscription;
	private int indice;// pour l'update dans la gestion de la publication

	private Facade facade;

	@FXML
	void actionAjouterPiece(ActionEvent event) {
		ctrl.actionCreePiece(event);
	}

	@FXML
	void actionQuitter(ActionEvent event) {
		if (update.get()) {// demande confirmation si des modifications ont été faites
			if (!ctrl.showConfirmation(I18N.getString("conf.quitter.sans.sauver")))
				return;
		}
		fermeture();
		stage.close();
	}

	/**
	 * Action à exécuter lorsqu'on quitte la vue
	 */
	private void fermeture() {
		mapUpdate.clear();
		update.set(false);
		obsPieces.clear();
		typePieces.clear();
	}

	@FXML
	void actionRecharger(ActionEvent event) {
		// on recharge chaque pièce modifiée
		for (Piece piece : mapUpdate.values()) {
			int pos = obsPieces.indexOf(piece);// obtient la position de la piece dans la liste
			// recharge la pièce et la met dans la liste observable à son ancienne position
			facade.getPiece(piece.getId()).ifPresent(p -> obsPieces.set(pos, p));
		}
		mapUpdate.clear();
		update.set(false);
	}

	@FXML
	void actionValider(ActionEvent event) {
		// Créé une liste avec les id des composants modifiés
		List<Integer> listeId = new ArrayList<>(mapUpdate.keySet());
		for (Integer id : listeId) {
			try {
				facade.updatePiece(mapUpdate.get(id));
				mapUpdate.remove(id);
			} catch (InstallationException e) {
				log.error("Can't update pièce: " + e.getMessage());
			}
		}
		if (mapUpdate.isEmpty())
			update.set(false);
	}

	/**
	 * Permet de fournir l'accès aux données
	 * 
	 * @param ctrl
	 * @param stage
	 */
	public void setUp(MainController ctrl, Stage stage) {
		this.stage = stage;
		this.ctrl = ctrl;
		this.facade = ctrl.getFacade();

		// charge la liste des types de pièces pour la combobox
		this.typePieces.clear();
		this.typePieces.addAll(facade.getTypePiece());
		// liste des pièces
		List<Piece> listePiece = ctrl.getFacade().getListePieces();
		obsPieces = FXCollections.observableArrayList(listePiece);
		tblPieces.setItems(obsPieces);
		// pas de changements
		update.set(false);
		mapUpdate.clear();
		// action si on clique sur la croix pour fermer la fenetre
		stage.setOnCloseRequest(_ -> fermeture());

		// s'enregistre sur les évènements
		facade.addObserver(this);
		// Permet de se désabonner lors de la fermeture de la stage
		stage.setOnHidden(_ -> {
			subscription.cancel();
			log.debug(" Liste de pièces: Désabonnement des évènements ");
		});
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		colNom.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getNom()));
		colDescription.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getDescription()));
		colTypePiece.setCellValueFactory(p -> new SimpleObjectProperty<TypePiece>(p.getValue().getTypePiece()));
		colEtage.setCellValueFactory(p -> new SimpleObjectProperty<BigDecimal>(p.getValue().getEtage()));
		// Rendre certaines colonnes modifiables
		tblPieces.setEditable(true);
		// NOM
		colNom.setEditable(true);
		// colNom.setCellFactory(TextFieldTableCell.forTableColumn());//version
		// classique (besoin enter pour setOnEditCommit)
		// version où l'on défini la zone de texte en provoquant un setOnEditCommit lors
		// qu'un changement de focus
		colNom.setCellFactory(_ -> new TextFieldTableCell<Piece, String>(new DefaultStringConverter()) {

			private TextField textField;

			@Override
			public void startEdit() {
				super.startEdit();

				if (textField == null) {
					textField = (TextField) getGraphic();

					textField.focusedProperty().addListener((_, _, newV) -> {
						if (!newV) {
							commitEdit(textField.getText());
						}
					});
				}
			}

			@Override
			public void commitEdit(String newValue) {

				String oldValue = getItem();

				// Ignore si inchangé
				if (Objects.equals(oldValue, newValue)) {
					super.cancelEdit(); // quitte le mode édition proprement
					return;
				}

				super.commitEdit(newValue);
			}

			@Override
			public void cancelEdit() {
				if (textField != null) {
					commitEdit(textField.getText());
				} else {
					super.cancelEdit();
				}
			}
		});
		// action sur changement
		colNom.setOnEditCommit((e) -> {
			Piece p = e.getRowValue();
			p.setNom(e.getNewValue());// maj de l'objet au sein de la vue
			// indique que cette piece a été modifiée
			mapUpdate.put(p.getId(), p);
			update.set(true);
		});
		// Description (version simple où il faut valider avec "enter")
		colDescription.setEditable(true);
		colDescription.setCellFactory(TextFieldTableCell.forTableColumn());
		colDescription.setOnEditCommit((e) -> {
			Piece p = e.getRowValue();
			p.setDescription(e.getNewValue());
			mapUpdate.put(p.getId(), p);
			update.set(true);
		});

		// Etage
		colEtage.setEditable(true);
		// utilisation d'un spinner
		colEtage.setCellFactory(_ -> new TableCell<>() {

			private final Spinner<Double> spinner = new Spinner<>(-5.0, 10.0, 0.0, 0.5);
			// pour éviter la mise à jour lors de l'initialisation par programmation
			private boolean updatingByPrgm = false;

			{
				spinner.setEditable(false);
				spinner.valueProperty().addListener((_, _, newVal) -> {

					if (updatingByPrgm || getIndex() < 0) {
						return;
					}
					// à exécuter uniquement lors de la modification dans la vue
					Piece piece = getTableView().getItems().get(getIndex());

					piece.setEtage(BigDecimal.valueOf(newVal).setScale(1, RoundingMode.HALF_UP));

					mapUpdate.put(piece.getId(), piece);
					update.set(true);
				});
			}

			@Override
			protected void updateItem(BigDecimal item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					updatingByPrgm = true;
					spinner.getValueFactory().setValue(item.doubleValue());
					updatingByPrgm = false;

					setGraphic(spinner);
				}
			}
		});

		// Type de pièce
		colTypePiece.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<TypePiece>() {

			@Override
			public TypePiece fromString(String str) {
				return typePieces.stream().filter(tp -> tp.getCode().equals(str)).findFirst().get();
			}

			@Override
			public String toString(TypePiece t) {
				return t == null ? "" : t.getNom();
			}

		}, typePieces));// suivi de la liste des types de pièce

		colTypePiece.setOnEditCommit(e -> {
			Piece p = e.getRowValue();
			p.setTypePiece(e.getNewValue());
			mapUpdate.put(p.getId(), p);
			update.set(true);
		});

		// Colonne Opération
		colOperation.setSortable(false);
		colOperation.setEditable(false);
		colOperation.setCellFactory(_ -> new TableCell<>() {
			// Le bouton delete
			final Button btDel = new Button();
			// image du bouton
			final ImageView iconDel = new ImageView(
					new Image(getClass().getResource("/org/isfce/pdb/icon/trash.png").toExternalForm()));
			// Conteneur du bouton
			final Pane paneBt = new StackPane();

			{// ajout de l'image au bonton
				btDel.setGraphic(iconDel);
				paneBt.setPadding(new Insets(2));
				paneBt.getChildren().add(btDel);
				/*
				 * en récupérant la valeur de la cellule, je tente de supprimer l'objet dans la
				 * BD
				 */
				btDel.setOnAction(_ -> {
					// Récupère la pièce
					Piece obj = getTableRow().getItem();

					if (obj != null) {

						try {// tente de supprimer la pièce ds BD
							if (facade.deletePiece(obj)) { // supprime dans la liste observable du tableau
								obsPieces.remove(obj);
								// supprime de la mapUpdate s'il existe
								mapUpdate.remove(obj.getId());
								update.set(!mapUpdate.isEmpty());// maj update si liste vide
							}

						} catch (InstallationException e1) {
							// affiche une fenêtre en cas d'erreur
							ctrl.showErreur(I18N.getString(e1.getMessage()));
						}
					}
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {

				if (!empty) {
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					setGraphic(paneBt);
				} else {
					setGraphic(null);
				}
				super.updateItem(item, empty);
			}

		});

		// Bt Valider actif si maj
		btValider.disableProperty().bind(update.not());
		btRecharger.disableProperty().bind(update.not());

	}

	/****************** GESTION DE LA PUBLICATION ********************/

	// Gestion des publications
	// appellé lors de l'enregistrement on reçoit alors une subscription
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		subscription.request(10);
		log.debug("Je suis un écouteur de Piece");
	}

	// Reception des messages
	@Override
	public void onNext(ListMessages messages) {
		// vérifie s'il existe un événement sur une Pièce
		Optional<Evenement> oEv = messages.getEventFromClasse(Classe.PIECE);
		// récupère la pièce
		if (oEv.isPresent() && oEv.get().element() instanceof Piece piece) {
			// récupère le type d'opération
			TypeOperation operation = oEv.get().op();

			log.debug(operation + " sur " + piece);
			switch (operation) {
			case INSERT -> Platform.runLater(() -> obsPieces.add(piece));
			case DELETE -> Platform.runLater(() -> obsPieces.remove(piece));
			case UPDATE -> { // recherche le composant dans la liste
				Platform.runLater(() -> {
					indice = -1;
					Optional<Piece> op = obsPieces.stream().filter((p) -> {
						indice++;
						return p.getId().equals(piece.getId());
					}).findFirst();
					if (op.isPresent())
						obsPieces.set(indice, piece);
				});
			}
			default -> {
			}
			}
		}
		subscription.request(1);
	}

	@Override
	public void onError(Throwable throwable) {
		log.debug("erreur d'abonnement aux évènements");

	}

	@Override
	public void onComplete() {
		log.debug(" écouteur de piece: On Complete");
	}

}
