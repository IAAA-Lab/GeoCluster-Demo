# GeoCluster-Demo

This is a demo for trying and testing the functionality of [GeoCluster](https://github.com/IAAA-Lab/GeoCluster).

## User guide
This demo consists in a Java application that runs a clustering algorithm with a dataset from a shapefile, and produces two results: one shapefile containing the annotated objects and the cluster they belong to, and another shapefile containing the clusters themselves.

You can use it alone as a Java application, or visualize the results using the notebooks contained in the jupyter directory.

### Visualizing results
The visualization part of this demo runs with [Jupyter Notebook](http://jupyter.org/). It is a web application that runs Python scripts. You will need to have Jupyter installed to follow with this part.

If you have Jupyter installed and you want to see some experiments, do the following steps:

1. Download this repo.
2. Run Jupyter Notebook on your machine by executing `jupyter notebook`. By default, it will open an explorer on your browser.
3. Head to this repo using the Jupyter explorer. 
4. You will find a directory called jupyter, containing the experiments done; and a directory called data, containing those datasets that were gathered with testing purposes but have not been used yet. Now head to jupyter directory.
5. Open any directory (each one containing the experiments related to a dataset). It contains the dataset itself, a jar with the Java application and the notebook.
6. Open the notebook. You can run any experiment using the Jupyter interface and you will see the results. Read the comments to understand what's happening.
7. Have fun doing your own experiments!
