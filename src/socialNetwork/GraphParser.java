package socialNetwork;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.stream.Stream;

public class GraphParser {

	public static Graph parse(Stream<String> lines,String delim) {
		Graph graph;
		graph = new Graph();
		String link;
		StringTokenizer nodeToken;
		double weight;
		AbstractNode caller = null;
		for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
			link = (String) iterator.next();
			if (link==null) {
				break;
			}
			nodeToken=new StringTokenizer(link,delim);
			String nodeOne = nodeToken.nextToken();
			String nodeTWO = nodeToken.nextToken();
			weight=0;
			if (nodeToken.hasMoreTokens()) {
				weight = Double.parseDouble( nodeToken.nextToken());
			}
			if (caller==null||!nodeOne.equals(caller.getId())) {
				caller = graph.getNode(nodeOne);
			}
			AbstractNode target=graph.getNode(nodeTWO);
			caller.directionalLink(target,weight);
			}
		return graph;
	}
//	private void createGraphComponent(String nodeID, String nodeTarget, double weight, Graph graph) {
//		
//
//	}non usato, inglobato in parse
	
	public static Graph parseFocusGraph(Stream<String> lines,String delim) {
		Graph graph;
		graph = new Graph();
		String link;
		StringTokenizer nodeToken;
//		double weight;
		AbstractNode caller = null;
		for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
			link = (String) iterator.next();
			if (link==null) {
				break;
			}
			nodeToken=new StringTokenizer(link,delim);
			String nodeOne = nodeToken.nextToken();
			String FocusName = nodeToken.nextToken();
//			weight=0;
//			if (nodeToken.hasMoreTokens()) {
//				weight = Double.parseDouble( nodeToken.nextToken());
//			}
			if (caller==null||!nodeOne.equals(caller.getId())) {
				caller = graph.getNode(nodeOne);
			}
			Focus target=graph.getFocus(FocusName);
			target.enroll(caller);
			}
		return graph;
	}
}
