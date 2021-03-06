package gui;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import dao.Clasificador;
import dao.Labor;
import dao.config.Semilla;
import dao.siembra.SiembraLabor;
import gui.utils.DateConverter;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import utils.DAH;


/**
 * clase que toma una objeto de configuracion lo muestra y permite editarlo y lo devuelve
 * @author tomas
 *
 */
public class SiembraConfigDialogController  extends Dialog<SiembraLabor>{
	private static final String FERT_CONFIG_DIALOG_FXML = "SiembraConfigDialog.fxml";

	@FXML
	private VBox content;


	@FXML
	private ComboBox<String> comboDosis;//ok

	@FXML
	private TextField textPrecioFert;//ok


	@FXML
	private TextField textEntresurco;//ok
	
//	@FXML
//	private TextField textSemillasBolsa;//ok


	@FXML
	private ComboBox<String> comboElev;//ok

	@FXML
	private TextField textNombre;//ok
	
	@FXML
	private DatePicker datePickerFecha;//ok



	@FXML
	private TextField textCostoLaborHa;//ok

	@FXML
	private TextField textClasesClasificador;

	@FXML
	private CheckBox chkMakeDefault;//ok

	@FXML
	private ComboBox<String> comboClasificador;//ok


	@FXML
	private ComboBox<Semilla> comboInsumo;


	private SiembraLabor labor;


	public SiembraConfigDialogController() {
		super();
		System.out.println("construyendo el controller");

		this.setTitle("Configure las opciones para su fertilización");
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
		StringBuilder message = new StringBuilder();
		boolean isValid =true;
		
//		return 	(cols.indexOf(comboElev.getValue())>-1)&&
//				//	(cols.indexOf(comboPasa.getValue())>-1)&&
//				(cols.indexOf(comboDosis.getValue())>-1);
		
		if(cols.indexOf(comboDosis.getValue())==-1){
			message.append("Debe seleccionar la columna Dosis\n");
			isValid=false;
		}
		if(cols.indexOf(comboElev.getValue())==-1){
//			message.append("Debe seleccionar la columna Elevacion\n");
//			isValid=false;
			labor.colElevacion.set(Labor.NONE_SELECTED);
		}
		
		if(!isValid){
			Alert alert = new Alert(AlertType.ERROR, message.toString(), ButtonType.OK);
			alert.initOwner(this.getDialogPane().getScene().getWindow());
			alert.setTitle("Validar configuracion");
			alert.showAndWait();

		}
		
		return isValid;

	}


	public void setLabor(SiembraLabor l) {
		this.labor = l;

		List<String> availableColums = labor.getAvailableColumns();
		availableColums.sort((a,b)->{
			return a.compareTo(b);
		});

		availableColums.add(Labor.NONE_SELECTED);

		//comboElev
		this.comboElev.setItems(FXCollections.observableArrayList(availableColums));
		this.comboElev.valueProperty().bindBidirectional(labor.colElevacion);



		// colRendimiento;
		this.comboDosis.setItems(FXCollections.observableArrayList(availableColums));
		this.comboDosis.valueProperty().bindBidirectional(labor.colDosisSemilla);

	
		this.comboInsumo.setItems(FXCollections.observableArrayList(DAH.getAllSemillas()));//Semilla.semillas.values()));
	//	this.comboInsumo.valueProperty().bindBidirectional(labor.semillaProperty);
		this.comboInsumo.valueProperty().addListener((obj,old,n)->{		
			labor.setSemilla(n);
			//labor.setPrecioLabor(converter.fromString(n).doubleValue());
			labor.config.getConfigProperties().setProperty(SiembraLabor.SEMILLA_DEFAULT, n.getNombre());
		});


		StringConverter<Number> converter = new NumberStringConverter();

		//textPrecioGrano
		//Bindings.bindBidirectional(this.textPrecioFert.textProperty(), labor.precioInsumoProperty, converter);
		this.textPrecioFert.textProperty().set(labor.config.getConfigProperties().getPropertyOrDefault(SiembraLabor.COLUMNA_PRECIO_SEMILLA, labor.getPrecioInsumo().toString()));
		this.textPrecioFert.textProperty().addListener((obj,old,n)->{			
			labor.setPrecioInsumo(converter.fromString(n).doubleValue());
			labor.config.getConfigProperties().setProperty(SiembraLabor.COLUMNA_PRECIO_SEMILLA, n);
		});

		//textCostoCosechaHa
		//Bindings.bindBidirectional(this.textCostoLaborHa.textProperty(), labor.precioLaborProperty, converter);
		//TODO tomar el valor de la labor y si es null levantar la configuracion. sino tomar el valor de la labor.
		this.textCostoLaborHa.textProperty().set(labor.config.getConfigProperties().getPropertyOrDefault(SiembraLabor.COLUMNA_PRECIO_PASADA, labor.getPrecioLabor().toString()));
		this.textCostoLaborHa.textProperty().addListener((obj,old,n)->{			
			labor.setPrecioLabor(converter.fromString(n).doubleValue());
			labor.config.getConfigProperties().setProperty(SiembraLabor.COLUMNA_PRECIO_PASADA, n);
		});
		
		
		this.textEntresurco.textProperty().set(converter.toString(labor.getEntreSurco()));
		this.textEntresurco.textProperty().addListener((obj,old,n)->{			
			labor.setEntreSurco(converter.fromString(n).doubleValue());
			labor.config.getConfigProperties().setProperty(SiembraLabor.ENTRE_SURCO_DEFAULT_KEY, n);
		});
		
		//Bindings.bindBidirectional(this.textEntresurco.textProperty(), labor.entreSurco, converter);
//		
//		Bindings.bindBidirectional(this.textSemillasBolsa.textProperty(), labor.semillasPorBolsa, converter);


		Bindings.bindBidirectional(this.textClasesClasificador.textProperty(), labor.clasificador.clasesClasificadorProperty, converter);

		this.comboClasificador.setItems(FXCollections.observableArrayList(Clasificador.clasficicadores));
		this.comboClasificador.valueProperty().bindBidirectional(labor.clasificador.tipoClasificadorProperty);

		//textNombre.textProperty().bindBidirectional(labor.nombreProperty);
		textNombre.textProperty().set(labor.getNombre());
		textNombre.textProperty().addListener((obj,old,nu)->labor.setNombre(nu));
		
	
		datePickerFecha.setValue(DateConverter.asLocalDate(l.fecha));
		datePickerFecha.setConverter(new DateConverter());
		datePickerFecha.valueProperty().addListener((obs, bool1, n) -> {
			l.setFecha(DateConverter.asDate(n));
			//l.fechaProperty.setValue(bool2);
		});
	}




	public void init() {
		this.getDialogPane().setContent(content);

	}



	public static Optional<SiembraLabor> config(SiembraLabor labor2) {
		Optional<SiembraLabor> ret = Optional.empty();
		try{
			FXMLLoader myLoader = new FXMLLoader(SiembraConfigDialogController.class.getResource(
					FERT_CONFIG_DIALOG_FXML));
			myLoader.load();//aca se crea el constructor
			SiembraConfigDialogController controller = ((SiembraConfigDialogController) myLoader.getController());
			controller.setLabor(labor2);
			controller.init();
			ret = controller.showAndWait();
		} catch (IOException e1) {
			System.err.println("no se pudo levantar el fxml "+FERT_CONFIG_DIALOG_FXML);
			e1.printStackTrace();
			System.exit(0);
		}
		return ret;
	}




}
