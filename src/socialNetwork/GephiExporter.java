package socialNetwork;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.openide.util.Lookup;

public class GephiExporter {
	private GraphModel graphModel;
	private DirectedGraph directedGraph;
	private AttributeController attributeManager;
	private AttributeModel model;
	private ExportController exportManager;
	private ProjectController progectManager;
	private Workspace workspace;
	private RankingController rankingController;

	public GephiExporter() {
		exportManager = Lookup.getDefault().lookup(ExportController.class);
		progectManager = Lookup.getDefault().lookup(ProjectController.class);
		progectManager.newProject();
		workspace = progectManager.getCurrentWorkspace();
		rankingController = Lookup.getDefault().lookup(RankingController.class);
		graphModel = Lookup.getDefault().lookup(GraphController.class)
				.getModel();
		directedGraph = graphModel.getDirectedGraph();
		attributeManager = Lookup.getDefault().lookup(AttributeController.class);
		model = attributeManager.getModel();

	}

	public Node getNode(String nodeID) {
		Node node = graphModel.getGraph().getNode(nodeID);
		if (node == null) {
			node = graphModel.factory().newNode(nodeID);
		}
		return node;
	}

	public Node getNode(String nodeID, String nodelabel) {
		Node n0 = getNode(nodeID);
		n0.getNodeData().setLabel(nodelabel);
		return n0;
	}

	public Edge createEdge(String nodeID1, String targetID, float weight,
			boolean directed) {
		Node node = graphModel.getGraph().getNode(nodeID1);
		Node target = graphModel.getGraph().getNode(targetID);
		return graphModel.factory().newEdge(node, target, weight, directed);
	}

	public Edge createEdge(Node node, Node target, float weight,
			boolean directed) {
		return graphModel.factory().newEdge(node, target, weight, directed);
	}

	public boolean addNode(Node n) {
		return directedGraph.addNode(n);
	}

	public boolean addEdge(Edge e) {
		return directedGraph.addEdge(e);
	}

	public AttributeColumn addAttributeColumn(String columnName,
			AttributeType type) {
		return model.getNodeTable().addColumn(columnName, type);
	}

	public void setAttributeValue(Node n, AttributeColumn col, Object value) {
		n.getNodeData().getAttributes().setValue(col.getIndex(), value);
	}
	
	public void setAttributeValue(Edge n, AttributeColumn col, Object value) {
		n.getEdgeData().getAttributes().setValue(col.getIndex(), value);
	}

	public void exportVisible(String fileName) throws IOException {
		// Export only visible graph
		GraphExporter exporter = (GraphExporter) exportManager.getExporter("gexf"); // Get
																			// GEXF
																			// exporter
		exporter.setExportVisible(true); // Only exports the visible (filtered)
											// graph
		exporter.setWorkspace(workspace);
		exportManager.exportFile(new File(fileName + ".gexf"), exporter);
	}

	public void export(String fileName) throws IOException {
		exportManager.exportFile(new File(fileName + ".gexf"));
	}

	public void setSizeByColumn(AttributeColumn column, float minSize,
			float maxSize, String typeRanking) {
		Ranking centralityRanking = rankingController.getModel().getRanking(
				typeRanking, column.getId());
		AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController
				.getModel().getTransformer(typeRanking,
						Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(minSize);
		sizeTransformer.setMaxSize(maxSize);
		rankingController.transform(centralityRanking, sizeTransformer);

	}

	public void setColorByColumn(AttributeColumn column, Color[] colors, String typeRanking) {
		Ranking columnRanking = rankingController.getModel().getRanking(
				typeRanking, column.getId());
		AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController
				.getModel().getTransformer(typeRanking,
						Transformer.RENDERABLE_COLOR);
		colorTransformer.setColors(colors);
		rankingController.transform(columnRanking, colorTransformer);
	}

	/*
	 * //Iterate values - fastest AttributeColumn sourceCol =
	 * model.getNodeTable().getColumn("source"); for (Node n :
	 * graphModel.getGraph().getNodes()) {
	 * System.out.println(n.getNodeData().getAttributes
	 * ().getValue(sourceCol.getIndex())); }
	 * 
	 * //Iterate values - normal for (Node n : graphModel.getGraph().getNodes())
	 * { System.out.println(n.getNodeData().getAttributes().getValue("source"));
	 * }
	 * 
	 * /* //Iterate over nodes for(Node n : directedGraph.getNodes()) { Node[]
	 * neighbors = directedGraph.getNeighbors(n).toArray();
	 * System.out.println(n.
	 * getNodeData().getLabel()+" has "+neighbors.length+" neighbors"); }
	 * 
	 * //Iterate over edges for(Edge e : directedGraph.getEdges()) {
	 * System.out.println
	 * (e.getSource().getNodeData().getId()+" -> "+e.getTarget(
	 * ).getNodeData().getId()); }
	 * 
	 * //Find node by id Node node2 = directedGraph.getNode("n2");
	 * 
	 * //Get degree
	 * System.out.println("Node2 degree: "+directedGraph.getDegree(node2));
	 * 
	 * //Modify the graph while reading //Due to locking, you need to use
	 * toArray() on Iterable to be able to modify //the graph in a read loop
	 * for(Node n : directedGraph.getNodes().toArray()) {
	 * directedGraph.removeNode(n); }
	 */

}
