package socialNetwork;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class Graph implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6152011564808931861L;
	private ConcurrentHashMap<String, AbstractNode>nodes;
	public Graph() {
		nodes=new ConcurrentHashMap<String, AbstractNode>();
	}
	public Graph(int capacity) {
		nodes=new ConcurrentHashMap<String, AbstractNode>(capacity);
	}
	
	public AbstractNode getNode(String id){
		AbstractNode requiredNode=takeNode(id);
		if( requiredNode==null){
			requiredNode=new Node(id);
			nodes.put(id, requiredNode);
		}
		return requiredNode;
	}
	
	public AbstractNode removeNode(String id){
		return nodes.remove(id);
	}
	
	
	private AbstractNode takeNode(String id2) {
		return nodes.get(id2);
	}
	public ConcurrentHashMap<String,AbstractNode> getNodes() {
		return nodes;
	}
	
	public static int distance(Node n1,Node n2){
		LinkedList<AbstractNode> visited=new LinkedList<AbstractNode>();
		LinkedList<AbstractNode> toVisit=new LinkedList<AbstractNode>();
		toVisit.add(n1);
		visited.add(n1);
		int distance=0;
		int nodeCurrentDistance=1;
		AbstractNode currentNode;
		Collection<DirectionalLink> adiacency;
		while (!toVisit.isEmpty()) {
			currentNode = toVisit.pop();
			
			nodeCurrentDistance--;//System.out.println(currentNode+": "+nodeCurrentDistance+" "+distance);
			if(currentNode.equals(n2)){
				return distance;
			}
			
			adiacency=currentNode.getAdiacencyList();
			for (DirectionalLink node : adiacency) {
				AbstractNode target = node.getTarget();
				if(!visited.contains(target)){
					visited.add(target);
					toVisit.add(target);
				}
			}
			if(nodeCurrentDistance==0){
				distance++;
				nodeCurrentDistance=toVisit.size();
			}
			
		}
		return -1 ;
	}
	public static Collection<DirectionalLink> predictDirectionalLink(Node root){
		LinkedList<AbstractNode> visited=new LinkedList<AbstractNode>();
		LinkedList<AbstractNode> toVisit=new LinkedList<AbstractNode>();
		
		LinkedList<Boolean> signList=new LinkedList<Boolean>();
		signList.add(true);
		LinkedList<DirectionalLink> result = new LinkedList<DirectionalLink>();
		
		toVisit.add(root);
		visited.add(root);
		int distance=0;
		int nodeCurrentDistance=1;
		AbstractNode currentNode;
		Collection<DirectionalLink> adiacency;
		while (!toVisit.isEmpty()) {
			currentNode = toVisit.pop();
			nodeCurrentDistance--;//System.out.println(currentNode+": "+nodeCurrentDistance+" "+distance);
			
			//preAction
			Boolean currentSign = signList.pop();
			
			adiacency=currentNode.getAdiacencyList();
			for (DirectionalLink link : adiacency) {
				AbstractNode target = link.getTarget();
				//preAdiacencyAction

				if(!visited.contains(target)){
					//newNodeAction
					boolean isApositiveSign = currentSign.equals(link.getWeight()>0 );
					signList.add(isApositiveSign);
					if (isApositiveSign) {
						result.add(new DirectionalLink(target,1));
					}else {
						result.add(new DirectionalLink(target,-1));
					}
					visited.add(target);
					toVisit.add(target);
				}
				//postAdiacencyAction
				
			}
			//postAction
			if(nodeCurrentDistance==0){
				//levelAction
				nodeCurrentDistance=toVisit.size();
			}
			
		}
		return result;
		
	}
}
