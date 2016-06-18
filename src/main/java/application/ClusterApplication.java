package application;

import static es.unizar.iaaa.ml.parameter.ParameterBuilder.geom;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.zeroturnaround.zip.ZipUtil;

import es.unizar.iaaa.ml.adapter.Cluster;
import es.unizar.iaaa.ml.adapter.Clusterable;
import es.unizar.iaaa.ml.adapter.SimpleFeatureClusterable;
import es.unizar.iaaa.ml.adapter.SimpleFeatureCollectionBuilder;
import es.unizar.iaaa.ml.clustering.ClustererFactory;
import es.unizar.iaaa.ml.clustering.FeatureClusterer;
import es.unizar.iaaa.ml.distance.AbsoluteDifferenceDistance;
import es.unizar.iaaa.ml.distance.DiscreteHausdorffDistance;
import es.unizar.iaaa.ml.distance.DistanceMeasure;
import es.unizar.iaaa.ml.distance.EuclideanDistance;
import es.unizar.iaaa.ml.distance.ExactHausdorffDistance;
import es.unizar.iaaa.ml.parameter.Parameter;
import es.unizar.iaaa.ml.parameter.ParameterNotFoundException;
import es.unizar.iaaa.ml.util.ClusterWriter;
import es.unizar.iaaa.ml.util.DataStoreReader;

/**
 * This is an application for using GeoCluster on some data and retrieving the
 * clusters in shapefile format.
 * 
 * @author Javier Beltran
 */
public class ClusterApplication {
	
	public static void main(String[] args) throws IOException, ParameterNotFoundException, SchemaException {
		/* Reads parameters */
		String name = args[0];
		String distanceName = args[1];
		String algorithmName = args[2];
		double param1 = Double.parseDouble(args[3]);
		int param2 = -1;
		if (args.length > 4) {
			param2 = Integer.parseInt(args[4]);
		}
		
		/* Reads the shapefile from a zip */
		File zip = new File(name + ".zip");
        Path path = Files.createTempDirectory(ClusterApplication.class.getCanonicalName());
        ZipUtil.unpack(zip, path.toFile());
        DataStoreReader dataset = DataStoreReader.shapefile(FileSystems.getDefault()
                .getPath(path.toString(), name + ".shp").toFile());
        
        List<Clusterable> list = new ArrayList<>();
		for (SimpleFeature f : dataset) {
			list.add(new SimpleFeatureClusterable(f));
		}
		
		/* Chooses the specified distance */
		DistanceMeasure distance = null;
		switch (distanceName) {
			case "Euclidean":
				distance = new EuclideanDistance();
				break;
			case "Absolute":
				distance = new AbsoluteDifferenceDistance();
				break;
			case "Hausdorff":
				distance = new ExactHausdorffDistance();
				break;
			case "DiscHausdorff":
				distance = new DiscreteHausdorffDistance();
				break;
		}
		
		/* Chooses the specified algorithm */
		Parameter[] params = {geom()};
		FeatureClusterer clusterer = null;
		switch (algorithmName) {
			case "DBSCAN":
				clusterer = new ClustererFactory().newDBSCANClusterer(
						param1, param2, distance, new SimpleFeatureCollectionBuilder(), params);
				break;
			case "KMeans":
				clusterer = new ClustererFactory().newKMeansClusterer(
						(int) param1, distance, new SimpleFeatureCollectionBuilder(
								list.get(0).getAdaptee(SimpleFeature.class).getType()), params);
				break;
			case "KMeans++":
				clusterer = new ClustererFactory().newKMeansPlusPlusClusterer(
						(int) param1, distance, new SimpleFeatureCollectionBuilder(
								list.get(0).getAdaptee(SimpleFeature.class).getType()), params);
				break;
		}

		/* Applies the clustering algorithm and writes the result in a shapefile */
		if (clusterer != null) {
			List<Cluster> clusters = clusterer.cluster(list);
			ClusterWriter writer = ClusterWriter.shapefile(new File(name + ".shp"), new File("cluster.shp"));
			writer.writeCluster(list, clusters);
		}
	}
	
}
