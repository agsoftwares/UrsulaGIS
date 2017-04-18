package tasks;
import java.awt.Point;
import java.io.File;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.List;

import dao.Clasificador;
import dao.Labor;
import dao.Ndvi;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.data.BufferWrapperRaster;
import gov.nasa.worldwind.data.DataRaster;
import gov.nasa.worldwind.data.DataRasterReader;
import gov.nasa.worldwind.data.DataRasterReaderFactory;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.SurfaceImageLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWMath;
import gov.nasa.worldwindx.examples.analytics.AnalyticSurface;
import gov.nasa.worldwindx.examples.analytics.AnalyticSurfaceAttributes;
import gov.nasa.worldwindx.examples.analytics.AnalyticSurfaceLegend;
import gov.nasa.worldwindx.examples.analytics.ExportableAnalyticSurface;
import gov.nasa.worldwindx.examples.util.ExampleUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
public class ShowNDVITifFileTask extends Task<Layer>{
	private static final double MAX_VALUE = 1.0;
private static final double MIN_VALUE = 0.2;
	private File file=null;
	public ShowNDVITifFileTask(File f){
		file =f;
	}
	public Layer call() {	
		try{
			BufferWrapperRaster raster = this.loadRasterFile(file);
			if (raster == null){
				return null;
			}

			String fileName = file.getName();
			//COPERNICUSS220170328T140051_20170328T140141_T20HNH.nd.tif
			if(fileName.contains("COPERNICUSS2")){
				fileName=fileName.replace("COPERNICUSS2", "");
				fileName=fileName.substring(0, "20170328".length());
				fileName=fileName.substring("201703".length(), fileName.length())
						+"-"+fileName.substring("2017".length(), "201703".length())
						+"-"+fileName.substring(0, "2017".length());
			}

			final ExportableAnalyticSurface surface = new ExportableAnalyticSurface();
			surface.setSector(raster.getSector());
			surface.setDimensions(raster.getWidth(), raster.getHeight());

			//					surface.setExportImageName("ndviExportedImage");
			//					surface.setExportImagePath("/exportedImagePath");
			//					OutputStream outStream = null;					
			//					KMZDocumentBuilder kmzB = new KMZDocumentBuilder(outStream);
			//					kmzB.writeObject(surface);
			//					Object writer = writer = XMLOutputFactory.newInstance().createXMLStreamWriter(this.zipStream);
			//					surface.export(KMLConstants.KML_MIME_TYPE, writer);//mimeType, output);

			double HUE_MIN = Clasificador.colors[0].getHue()/360d;//0d / 360d;
			double HUE_MAX = Clasificador.colors[Clasificador.colors.length-1].getHue()/360d;//240d / 360d;
			double transparentValue =raster.getTransparentValue();
			System.out.println("ndvi transparent value = "+transparentValue);
			//double transparentValue =extremes[0];
			transparentValue=0;//para que pueda interpretar los valores clipeados como transparente
			surface.setValues(AnalyticSurface.createColorGradientValues(
					raster.getBuffer(), transparentValue, MIN_VALUE, MAX_VALUE, HUE_MIN, HUE_MAX));
			// surface.setVerticalScale(5e3);
			surface.setVerticalScale(100);
			//surface.setAltitude(10);

			AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
			attr.setDrawOutline(false);
			attr.setDrawShadow(false);
			attr.setInteriorOpacity(1);
			surface.setSurfaceAttributes(attr);

			Format legendLabelFormat = new DecimalFormat() ;
			final AnalyticSurfaceLegend legend = AnalyticSurfaceLegend.fromColorGradient(MIN_VALUE,MAX_VALUE,
					HUE_MIN, HUE_MAX,
					AnalyticSurfaceLegend.createDefaultColorGradientLabels(MIN_VALUE, MAX_VALUE, legendLabelFormat),
					AnalyticSurfaceLegend.createDefaultTitle(fileName + " NDVI Values"));
			legend.setOpacity(1);
			legend.setScreenLocation(new Point(100, 400));

			Renderable renderable =  new Renderable()	{
				public void render(DrawContext dc)
				{
					Extent extent = surface.getExtent(dc);
					if (!extent.intersects(dc.getView().getFrustumInModelCoordinates()))
						return;

					if (WWMath.computeSizeInWindowCoordinates(dc, extent) < 300)
						return;

					legend.render(dc);
				}
			};
			SurfaceImageLayer layer = new SurfaceImageLayer();
			
		
			layer.setName(fileName);
			layer.setPickEnabled(false);
			layer.addRenderable(surface);
			layer.addRenderable(renderable);
			layer.setValue(Labor.LABOR_LAYER_IDENTIFICATOR, new Ndvi());

			Sector s = raster.getSector();
			Position pointPosition = Position.fromDegrees(s.getMinLatitude().degrees, s.getMinLongitude().degrees);
			PointPlacemark pmStandard = new PointPlacemark(pointPosition);
			PointPlacemarkAttributes pointAttribute = new PointPlacemarkAttributes();
			pointAttribute.setImageColor(java.awt.Color.red);
			//		if(HiDPIHelper.isHiDPI()){
			//			pointAttribute.setLabelFont(java.awt.Font.decode("Verdana-Bold-50"));
			//		}
			pointAttribute.setLabelMaterial(Material.DARK_GRAY);
			pmStandard.setLabelText(file.getName());
			pmStandard.setAttributes(pointAttribute);
			layer.addRenderable(pmStandard);

			layer.setValue(ProcessMapTask.ZOOM_TO_KEY, pointPosition);
			return layer;

		} catch (Exception e)     {
			e.printStackTrace();
		}
		return null;
	}
	
	private BufferWrapperRaster loadRasterFile(File file){
		if(!file.exists()){	
			//TODO si el recurso es web podemos bajarlo a 
			// Download the data and save it in a temp file.
			String path = file.getAbsolutePath();
			file = ExampleUtil.saveResourceToTempFile(path, "." + WWIO.getSuffix(path));
		}



		// Create a raster reader for the file type.
		DataRasterReaderFactory readerFactory = (DataRasterReaderFactory) WorldWind.createConfigurationComponent(
				AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
		DataRasterReader reader = readerFactory.findReaderFor(file, null);

		try{
			// Before reading the raster, verify that the file contains elevations.
			AVList metadata = reader.readMetadata(file, null);
			if (metadata == null || !AVKey.ELEVATION.equals(metadata.getStringValue(AVKey.PIXEL_FORMAT)))
			{
				Platform.runLater(()->{
					Alert imagenAlert = new Alert(Alert.AlertType.ERROR);
					//imagenAlert.initOwner(stage);
					imagenAlert.initModality(Modality.NONE);
					imagenAlert.setTitle("Archivo no compatible");
					imagenAlert.setContentText("El archivo no continen informacion ndvi. Por favor seleccione un archivo con solo una capa");
					imagenAlert.show();
				});
				String msg = Logging.getMessage("ElevationModel.SourceNotElevations", file.getAbsolutePath());
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}

			// Read the file into the raster.
			DataRaster[] rasters = reader.read(file, null);
			if (rasters == null || rasters.length == 0)	{
				String msg = Logging.getMessage("ElevationModel.CannotReadElevations", file.getAbsolutePath());
				Logging.logger().severe(msg);
				throw new WWRuntimeException(msg);
			}

			// Determine the sector covered by the elevations. This information is in the GeoTIFF file or auxiliary
			// files associated with the elevations file.
			Sector sector = (Sector) rasters[0].getValue(AVKey.SECTOR);
			if (sector == null)
			{
				String msg = Logging.getMessage("DataRaster.MissingMetadata", AVKey.SECTOR);
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}

			// Request a sub-raster that contains the whole file. This step is necessary because only sub-rasters
			// are reprojected (if necessary); primary rasters are not.
			int width = rasters[0].getWidth();
			int height = rasters[0].getHeight();

			DataRaster subRaster = rasters[0].getSubRaster(width, height, sector, rasters[0]);

			// Verify that the sub-raster can create a ByteBuffer, then create one.
			if (!(subRaster instanceof BufferWrapperRaster))
			{
				String msg = Logging.getMessage("ElevationModel.CannotCreateElevationBuffer", file.getName());
				Logging.logger().severe(msg);
				throw new WWRuntimeException(msg);
			}

			return (BufferWrapperRaster) subRaster;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
