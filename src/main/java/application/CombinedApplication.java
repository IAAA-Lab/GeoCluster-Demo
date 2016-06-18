package application;

import static es.unizar.iaaa.ml.parameter.ParameterBuilder.geom;
import static es.unizar.iaaa.ml.parameter.ParameterBuilder.integer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.zeroturnaround.zip.ZipUtil;

import es.unizar.iaaa.ml.adapter.Cluster;
import es.unizar.iaaa.ml.adapter.ClusterBuilder;
import es.unizar.iaaa.ml.adapter.Clusterable;
import es.unizar.iaaa.ml.adapter.SimpleFeatureClusterable;
import es.unizar.iaaa.ml.adapter.SimpleFeatureCollectionBuilder;
import es.unizar.iaaa.ml.clustering.ClustererFactory;
import es.unizar.iaaa.ml.clustering.FeatureClusterer;
import es.unizar.iaaa.ml.distance.AbsoluteDifferenceDistance;
import es.unizar.iaaa.ml.distance.CombinedDistance;
import es.unizar.iaaa.ml.distance.DistanceMeasure;
import es.unizar.iaaa.ml.distance.ExactHausdorffDistance;
import es.unizar.iaaa.ml.parameter.Parameter;
import es.unizar.iaaa.ml.parameter.ParameterNotFoundException;
import es.unizar.iaaa.ml.util.ClusterWriter;
import es.unizar.iaaa.ml.util.DataStoreReader;

/**
 * This is an application for using GeoCluster's combined distance on some
 * data and retrieving the results in shapefiles.
 * 
 * @author Javier Beltran
 */
public class CombinedApplication {
	
	public static void main(String[] args) throws IOException, ParameterNotFoundException, SchemaException {
		String name = args[0];
		double locWeight = Double.parseDouble(args[1]);
		double numWeight = Double.parseDouble(args[2]);
		int eps = Integer.parseInt(args[3]);
		int minPts = Integer.parseInt(args[4]);
		
		/* Reads the shapefile from a zip */
		File zip = new File(name + ".zip");
        Path path = Files.createTempDirectory(CombinedApplication.class.getCanonicalName());
        ZipUtil.unpack(zip, path.toFile());
        DataStoreReader dataset = DataStoreReader.shapefile(FileSystems.getDefault()
                .getPath(path.toString(), name + ".shp").toFile());
        
        List<Clusterable> list = new ArrayList<>();
		for (SimpleFeature f : dataset) {
			list.add(new SimpleFeatureClusterable(f));
		}
		
		/* Sets the parameters of the algorithm */
		Parameter[] params = {geom(), integer("CensoINE20")};
		ClusterBuilder builder = new SimpleFeatureCollectionBuilder(
				list.get(0).getAdaptee(SimpleFeature.class).getType());
		
		/* Defines the combined distance */
		DistanceMeasure[] distances = {new ExactHausdorffDistance(), new AbsoluteDifferenceDistance()};
		Double[] weights = {locWeight, numWeight};
		DistanceMeasure combinedDistance = new CombinedDistance(Arrays.asList(distances), Arrays.asList(weights));
		
		/* Clusterizes */
		FeatureClusterer clusterer = new ClustererFactory().newDBSCANClusterer(
				eps, minPts, combinedDistance, builder, params);
		List<Cluster> clusters = clusterer.cluster(list);
		
		/* Writes the results in shapefile format */
		ClusterWriter writer = ClusterWriter.shapefile(new File(name + ".shp"), new File("cluster.shp"));
		writer.writeCluster(list, clusters);
	}
	
}
