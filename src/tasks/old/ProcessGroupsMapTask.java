package tasks.old;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




import javafx.scene.Group;
import javafx.scene.shape.Path;




import org.geotools.data.FileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;




import tasks.ProcessMapTask;
import utils.ProyectionConstants;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.buffer.BufferOp;




import dao.Clasificador;
import dao.FeatureContainer;
import dao.Labor;

/**
 * esta clase tiene el objetivo de buscar los grupos por amount y por cercania
 * es decir: dado un conjunto de elementos, los agrupa de acuerdo al amount de los elementos y luego de acuerdo a su cercania
 * @author tomas
 *
 */
public class ProcessGroupsMapTask<E extends Labor<? extends FeatureContainer>> extends ProcessMapTask<E> {
	

	public ProcessGroupsMapTask(E labor) {
		super();
		super.labor=labor;
	}

	public void doProcess() throws IOException {
		
		//this.featureTree = new Quadtree();

		SimpleFeatureSource featureSource = labor.getInStore().getFeatureSource();
		Clasificador clasificador = labor.getClasificador();
		ReferencedEnvelope bounds = featureSource.getBounds();
		double width = bounds.getWidth();
		double height = bounds.getHeight();
		double gridSize = Math.min(width, height)/4;
		//TODO 1. crear una grilla de tama�o gridSize para dividir el lote en aproximadamente 16 regiones si es cuadrado
		List<Polygon> polygons = construirGrilla(bounds, gridSize);
		polygons.parallelStream().forEach((poly)->{
			//TODO 2. recorrer las features de cada una de las regiones buscando las features adjyacentes y si son de la misma categoria unirlas
			List<? extends FeatureContainer> features = labor.inStoreQuery(poly.getEnvelopeInternal());
			boolean huboUnion = true;
			//TODO 2.1 repetir 2 hasta que no hayan mas uniones
			List<FeatureContainer> subGroup = new ArrayList<FeatureContainer>(features);
			while(huboUnion){
				for(FeatureContainer fc : features){
					subGroup.remove(fc);//quito el actual para no comparar con sigo mismo
					int category =clasificador.getCategoryFor(fc.getAmount());
					Geometry geom = fc.getGeometry();
					for(FeatureContainer candidato : subGroup){
						int categotyCandidato =clasificador.getCategoryFor(candidato.getAmount());
						if(category==categotyCandidato){
							
							if(geometry.)
							
						}
					}
				}
			}
		});
		
		
		//TODO 3 recorrer las features resultantes buscando adyacencias y juntandolas si son de la misma categoria
		
		SimpleFeatureCollection featureCollection = featureSource.getFeatures();
		SimpleFeatureIterator featuresIterator = featureCollection.features();

		//1 recorrer todos los elementos y obtener media y desvio estandar para separarlo en grupos

		constructJenksClasifier(featureCollection, amountColumn);
		
		Map<Integer,List<Geometry>> clases = 	new HashMap<Integer,List<Geometry>>();
		while (featuresIterator.hasNext()) {
			try{
				SimpleFeature simpleFeature = featuresIterator.next();
				Object gObj = (Geometry) simpleFeature.getDefaultGeometry();// cosehaItem.getGeometry();
				Geometry geometry = null;
				if(gObj instanceof Geometry){
					geometry= (Geometry)gObj;
					//System.out.println("agregando una geometria a las clases. es valida? "+geometry.isValid());//si
				}
				
				Double ammount =	(Double)simpleFeature.getAttribute(amountColumn);
				Integer clase = clasifier.classify(ammount);			
		
				List<Geometry> classGeom = clases.get(clase);
				
				if(classGeom == null){
					classGeom =	new ArrayList<Geometry>();		
				}
				classGeom.add(geometry);
				clases.put(clase,classGeom);
			}catch(Exception e){
				e.printStackTrace();

			}

		}// fin del while
		
		//XXX memoria o velocidad. ver si no es mejor primero clasificar todos los elementos en un array y despues hacer una union por cada grupo
//		Map<Integer,Geometry> clases = 	new HashMap<Integer,Geometry>();
//		while (featuresIterator.hasNext()) {
//			try{
//				SimpleFeature simpleFeature = featuresIterator.next();
//				Geometry geometry = (Geometry) simpleFeature.getDefaultGeometry();
//
//				Double ammount =	(Double)simpleFeature.getAttribute(amountColumn);
//				Integer clase = clasifier.classify(ammount);	
//				Geometry classGeom = clases.get(clase);
//				Geometry union=null;
//				//XXX memoria o velocidad. ver si no es mejor primero clasificar todos los elementos en un array y despues hacer una union por cada grupo
//				if(classGeom == null){				
//					union = geometry;
//
//				} else {
//					GeometryFactory fact=geometry.getFactory();
//					Geometry[] geomArray = {geometry,classGeom};
//					GeometryCollection polygonCollection = fact.createGeometryCollection(geomArray);				
//					union = polygonCollection.buffer(0); 
//				}
//				clases.put(clase, union);
//			}catch(Exception e){
//				e.printStackTrace();
//
//			}
//		}// fin del while

		this.pathTooltips.clear();
		clases.forEach((integer,geometries)->{
			Geometry g =geometries.get(0);//todas las geometrias que pertenecen a una clase
			GeometryFactory fact=g.getFactory();
		//	Geometry[] geomArray = (Geometry[]) geometries.toArray();
			Geometry[] geomArray = new Geometry[geometries.size()];
			geomArray = geometries.toArray(geomArray);
			GeometryCollection polygonCollection = fact.createGeometryCollection(geomArray);			
		//	Geometry union=	polygonCollection.isValid();
			//Geometry union = polygonCollection.buffer(1*ProyectionConstants.metersToLongLat); 
			Geometry union =null;
			
			//FIXME manejar todos los posibles tipos de geometrias y corregir los invalidos
			//Linestring / union /LineMerger/Poligonizer.getPolygons();
			if(polygonCollection.isValid()){
				System.out.println("valid "+polygonCollection);
				// union =polygonCollection.union(); 
				BufferOp bufOp = new BufferOp(polygonCollection);
				bufOp.setEndCapStyle(BufferOp.CAP_BUTT);
				union = bufOp.getResultGeometry(0);
			}else{
			//	union = polygonCollection.convexHull();//.getBoundary();
				System.out.println("invalid "+polygonCollection);
			//	union = polygonCollection.buffer(1*ProyectionConstants.metersToLongLat); 
				
//				BufferOp bufOp = new BufferOp(polygonCollection);
//				bufOp.setEndCapStyle(BufferOp.CAP_BUTT);
//				union = bufOp.getResultGeometry(1*ProyectionConstants.metersToLongLat);
				for(Geometry geom : geometries){
					if(union ==null){
						union =  geom;
					}else{
						if(geom instanceof Polygon){
							System.out.println("es una Polygon "+geom);
							union = union.union(geom);
						} else if(geom instanceof MultiPolygon){
							
							MultiPolygon mp = (MultiPolygon)geom;		 
						//	System.out.println("es una MultiPolygon "+mp);
							for(int i=0;i<mp.getNumGeometries();i++){
								Polygon p = (Polygon) (mp).getGeometryN(i);
								try{
									union = union.union(p);
								}catch(Exception e ){
									e.printStackTrace();
								}
							}
						}else if(geom instanceof GeometryCollection){
							GeometryCollection gc = (GeometryCollection)geom;		 
							System.out.println("es una geometryCollection "+gc);
							for(int i=0;i<gc.getNumGeometries();i++){
								Geometry p = (Geometry) (gc).getGeometryN(i);
								union = union.union(p);
							}
						}
					}
					
				}
			}
		
			
			if(union instanceof Polygon){
				pathTooltips.add(getPathTooltip((Polygon)union,integer));	
			} else if(union instanceof MultiPolygon){
				MultiPolygon mp = (MultiPolygon)union;		
				
//				pathTooltips.add(getPathTooltip((Polygon) (mp).getGeometryN(2),integer));	 
				
				for(int i=0;i<mp.getNumGeometries();i++){
					Polygon p = (Polygon) (mp).getGeometryN(i);
					pathTooltips.add(getPathTooltip(p,integer));	
				}
			}else if(union instanceof GeometryCollection){	
				System.out.println("hay que crear un pathTooltipo para el geometryCollection "+union);
				
			}
		});
		
		
//		clases.forEach((integer,geometry)->{
//			Geometry g =geometry;
//			if(g instanceof Polygon){
//				pathTooltips.add(getPathTooltip((Polygon)g,integer));	
//
//			} else if(g instanceof MultiPolygon){
//				MultiPolygon mp = (MultiPolygon)g;			
//				for(int i=0;i<mp.getNumGeometries();i++){
//					Polygon p = (Polygon) (mp).getGeometryN(i);
//					pathTooltips.add(getPathTooltip(p,integer));	
//				}
//
//			}	
//		});

		runLater();
		updateProgress(0, featureCount);
	}

	private ArrayList<Object> getPathTooltip(Polygon poly, Integer label) {
		Path path = getPathFromGeom(poly, label);
		path.setStrokeWidth(1);
		double area = poly.getArea() * ProyectionConstants.A_HAS;// 30224432.818;//pathBounds2.getHeight()*pathBounds2.getWidth();
		DecimalFormat df = new DecimalFormat("#.00");
		String tooltipText = new String(
				" Label: " + df.format(label) + "\n"
				);

		if (area < 1) {
			tooltipText = tooltipText.concat("Sup: "
					+ df.format(area * ProyectionConstants.METROS2_POR_HA)
					+ "m2\n");
		} else {
			tooltipText = tooltipText.concat("Sup: " + df.format(area)
					+ "Has\n");
		}

		ArrayList<Object> ret = new ArrayList<Object>();
		ret.add(path);
		ret.add(tooltipText);
		return ret;
	}

	/**
	 * 
	 * @param bounds en long/lat
	 * @param ancho en metros
	 * @return una lista de poligonos que representa una grilla con un 100% de superposiocion
	 */
	public List<Polygon> construirGrilla(BoundingBox bounds,double ancho) {
		System.out.println("construyendo grilla");
		List<Polygon> polygons = new ArrayList<Polygon>();
		//convierte los bounds de longlat a metros
		Double minX = bounds.getMinX()/ProyectionConstants.metersToLongLat - ancho/2;
		Double minY = bounds.getMinY()/ProyectionConstants.metersToLongLat - ancho/2;
		Double maxX = bounds.getMaxX()/ProyectionConstants.metersToLongLat+ ancho/2;
		Double maxY = bounds.getMaxY()/ProyectionConstants.metersToLongLat+ ancho/2;
		Double x0=minX;
		for(int x=0;(x0)<maxX;x++){
			x0=minX+x*ancho;
			Double x1=minX+(x+1)*ancho;
			for(int y=0;(minY+y*ancho)<maxY;y++){
				Double y0=minY+y*ancho;
				Double y1=minY+(y+1)*ancho;


				Coordinate D = new Coordinate(x0*ProyectionConstants.metersToLongLat, y0*ProyectionConstants.metersToLongLat); 
				Coordinate C = new Coordinate(x1*ProyectionConstants.metersToLongLat, y0*ProyectionConstants.metersToLongLat);
				Coordinate B = new Coordinate(x1*ProyectionConstants.metersToLongLat, y1*ProyectionConstants.metersToLongLat);
				Coordinate A =  new Coordinate(x0*ProyectionConstants.metersToLongLat, y1*ProyectionConstants.metersToLongLat);

				/**
				 * D-- ancho de carro--C ^ ^ | | avance ^^^^^^^^ avance | | A-- ancho de
				 * carro--B
				 * 
				 */
				Coordinate[] coordinates = { A, B, C, D, A };// Tiene que ser cerrado.
				// Empezar y terminar en
				// el mismo punto.
				// sentido antihorario

				//			GeometryFactory fact = X.getFactory();
				GeometryFactory fact = new GeometryFactory();


				//				DirectPosition upper = positionFactory.createDirectPosition(new double[]{-180,-90});
				//				DirectPosition lower = positionFactory.createDirectPosition(new double[]{180,90});
				//	Envelope envelope = geometryFactory.createEnvelope( upper, lower );

				LinearRing shell = fact.createLinearRing(coordinates);
				LinearRing[] holes = null;
				Polygon poly = new Polygon(shell, holes, fact);			
				polygons.add(poly);
			}
		}
		return polygons;
	}
	
	protected int getAmountMin() {
		return 100;
	}

	protected int gerAmountMax() {
		return 500;
	}

}
