package mmg.gui;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import dao.Clasificador;
import dao.margen.Margen;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public class MargenConfigDialogController  extends Dialog<Margen>{
	private static final String CONFIG_DIALOG_FXML = "MargenConfigDialog.fxml";
	
	
	@FXML
	private VBox content;



//fijos

	@FXML
	private TextField textNombre;//ok
	
	@FXML
	private DatePicker datePickerFecha;//ok
	
	@FXML
	private TextField textCostoTn;//ok

	@FXML
	private TextField textCostoHa;//ok
	
	@FXML
	private TextField textFlete;//ok

	@FXML
	private ChoiceBox<String> comboAmount;
	
	@FXML
	private TextField textClasesClasificador;

	@FXML
	private CheckBox chkMakeDefault;//ok

	@FXML
	private ComboBox<String> comboClasificador;//ok

	private Margen labor;
	
	public MargenConfigDialogController() {
		super();
		System.out.println("construyendo el controller");

		this.setTitle("Configure las opciones para su mapa de rentabilidades");
		Stage stage = ((Stage)this.getDialogPane().getScene().getWindow());
		stage.getIcons().add(new Image(JFXMain.ICON));

		this.getDialogPane().getButtonTypes().add(ButtonType.OK);
		this.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
		this.setResizable(true);

		final Button btOk = (Button) this.getDialogPane().lookupButton(ButtonType.OK);
		btOk.addEventFilter(ActionEvent.ACTION, event -> {
			if (!validarDialog()) {
				System.out.println("la configuracion es incorrecta");
				event.consume();
			}
		});

		this.setResultConverter(e -> {		
			if(ButtonType.OK.equals(e)){					
				if(chkMakeDefault.selectedProperty().get()){
					labor.getConfigLabor().getConfigProperties().save();
				}				
				return labor;

			}else{
				return null;
			}
		});
	}

	
	private boolean validarDialog() {
		List<String> cols = labor.getAvailableColumns();
		return 	true;
				
	}
	


	public void init() {
		this.getDialogPane().setContent(content);

	}
	
	public void setLabor(Margen l) {
		this.labor = l;

		List<String> availableColums = labor.getAvailableColumns();
		availableColums.sort((a,b)->{
			return a.compareTo(b);
		});

		textNombre.textProperty().bindBidirectional(labor.nombreProperty);
		
		//datePickerFecha.valueProperty().bindBidirectional(l.fechaProperty,);
		//StringConverter<LocalDate> dateConverter = this.datePickerFecha.getConverter();
		datePickerFecha.setValue(l.fechaProperty.getValue());
		datePickerFecha.valueProperty().addListener((obs, bool1, bool2) -> {
			
			l.fechaProperty.setValue(bool2);
		});

		StringConverter<Number> converter = new NumberStringConverter();

		//textPrecioGrano
		Bindings.bindBidirectional(this.textCostoHa.textProperty(), labor.costoFijoHaProperty, converter);

		//textCostoLaborHa
		Bindings.bindBidirectional(this.textCostoTn.textProperty(), labor.costoTnProperty, converter);
		//textCostoLaborHa
		Bindings.bindBidirectional(this.textFlete.textProperty(), labor.costoFleteProperty, converter);
		
		List<String> options = Arrays.asList("Rentabilidad","Margen");
		this.comboAmount.setItems(FXCollections.observableArrayList(options));
		if(Margen.COLUMNA_RENTABILIDAD.equalsIgnoreCase(labor.colAmount.get())){				
			this.comboAmount.getSelectionModel().select(0);
		} else{
			this.comboAmount.getSelectionModel().select(1);
		}
		this.comboAmount.valueProperty().addListener((o,s,s2)->{
			System.out.println("cambiando colAmount a "+s2);
			if(options.get(0).equalsIgnoreCase(s2)){				
				labor.colAmount.set(Margen.COLUMNA_RENTABILIDAD);
				String n = textNombre.textProperty().get().replace("Margen","Renta");;
			
				
				System.out.println("nombre despues de reemplazar renta es "+n);
				textNombre.textProperty().set(n);
			} else{
				labor.colAmount.set(Margen.COLUMNA_MARGEN);
				String n = textNombre.textProperty().get().replace("Renta","Margen");;
				
				textNombre.textProperty().set(n);
			}
			
		});

		Bindings.bindBidirectional(this.textClasesClasificador.textProperty(), labor.clasificador.clasesClasificadorProperty, converter);

//		this.comboClasificador.setItems(FXCollections.observableArrayList(Clasificador.clasficicadores));
//		this.comboClasificador.valueProperty().bindBidirectional(labor.clasificador.tipoClasificadorProperty);
		
		this.comboClasificador.setItems(FXCollections.observableArrayList(Clasificador.clasficicadores));
		this.comboClasificador.valueProperty().bindBidirectional(labor.clasificador.tipoClasificadorProperty);
//		this.comboClasificador.valueProperty().addListener((o,s,s2)->{
//			System.out.println("cambiando clasificador a "+s2);
//		
//		});

	}

	
	public static Optional<Margen> config(Margen labor2) {
		Optional<Margen> ret = Optional.empty();
		try{
			FXMLLoader myLoader = new FXMLLoader(PulverizacionConfigDialogController.class.getResource(
					CONFIG_DIALOG_FXML));
			myLoader.load();//aca se crea el constructor
			MargenConfigDialogController controller = ((MargenConfigDialogController) myLoader.getController());
			controller.setLabor(labor2);
			controller.init();
			ret = controller.showAndWait();
		} catch (IOException e1) {
			System.err.println("no se pudo levantar el fxml "+CONFIG_DIALOG_FXML);
			e1.printStackTrace();
			System.exit(0);
		}
		return ret;
	}
}
