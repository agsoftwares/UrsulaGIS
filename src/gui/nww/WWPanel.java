package gui.nww;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;
import gov.nasa.worldwindx.examples.util.HighlightController;
import javafx.scene.layout.BorderPane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

//FIXME cambiar a javaFX SwingNode
public class WWPanel extends JPanel {
	
	private static final long serialVersionUID = -7158127157119827058L;
	protected WorldWindow wwd;
	protected StatusBar statusBar;
	protected ToolTipController toolTipController;
	protected HighlightController highlightController;

	public WWPanel(Dimension canvasSize, boolean includeStatusBar) {
		super(new BorderLayout());

		this.wwd = this.createWorldWindow();
	//	((Component) this.wwd).setSize((int)canvasSize.getWidth()/4,(int) canvasSize.getHeight()/4);
		((Component) this.wwd).setPreferredSize(canvasSize);

		// Create the default model as described in the current worldwind
		// properties.
		Model m = (Model) WorldWind
				.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
//		final ElevationModel elevationModel = new ZeroElevationModel(){
//		m.getGlobe().setElevationModel(elevationModel);
		this.wwd.setModel(m);

		// Setup a select listener for the worldmap click-and-go feature
		this.wwd.addSelectListener(new ClickAndGoSelectListener(this
				.getWwd(), WorldMapLayer.class));

		this.add((Component) this.wwd, BorderLayout.CENTER);
		//this.setCenter(wwd);
		if (includeStatusBar) {
			this.statusBar = new StatusBar();
			//this.setBottom(statusBar);
			this.add(statusBar, BorderLayout.PAGE_END);
			this.statusBar.setEventSource(wwd);
		}

		// Add controllers to manage highlighting and tool tips.
		this.toolTipController = new ToolTipController(this.getWwd(),
				AVKey.DISPLAY_NAME, null);
		this.highlightController = new HighlightController(this.getWwd(),
				SelectEvent.ROLLOVER);
	}

	//@Override
	public void setPreferredSize(Dimension dim){
	//	super.setPrefSize(dim.getWidth(),dim.getHeight());
		super.setPreferredSize(dim);
		((Component) this.wwd).setPreferredSize(dim);
		//this.wwd.redraw();

	}

	protected WorldWindow createWorldWindow() {//Este es el unico metodo de cambie de AppPanel porque sino no andaba con JavaFX
		//return new WorldWindowGLCanvas();
		return new WorldWindowGLJPanel();
		//return new WorldWindow();
	}

	public WorldWindow getWwd() {
		return wwd;
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}
}//FIN DE AppPanel