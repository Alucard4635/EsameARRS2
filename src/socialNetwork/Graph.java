package socialNetwork;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class Graph implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6152011564808931861L;
	private ConcurrentHashMap<String, AbstractNode> nodes;
	private ConcurrentHashMap<String, Focus> focus;

	public Graph() {
		setNodes(new ConcurrentHashMap<String, AbstractNode>());
		setFocus(new ConcurrentHashMap<String, Focus>());

	}

	public Graph(int nodeCapacity, int focusCapacity) {
		setNodes(new ConcurrentHashMap<String, AbstractNode>(nodeCapacity));
		setFocus(new ConcurrentHashMap<String, Focus>(focusCapacity));
	}

	public AbstractNode getNode(String id) {
		AbstractNode requiredNode = takeNode(id);
		if (requiredNode == null) {
			requiredNode = new Node(id);
			getNodes().put(id, requiredNode);
		}
		return requiredNode;
	}

	public Focus getFocus(String id) {
		Focus requiredFocus = takeFocus(id);
		if (requiredFocus == null) {
			requiredFocus = new Focus(id);
			focus.put(id, requiredFocus);
		}
		return requiredFocus;
	}

	private Focus takeFocus(String id) {
		return getFocus().get(id);
	}

	public Focus removeFocus(String id) {
		return getFocus().remove(id);
	}

	public AbstractNode removeNode(String id) {
		return getNodes().remove(id);
	}

	private AbstractNode takeNode(String id) {
		return getNodes().get(id);
	}

	public ConcurrentHashMap<String, AbstractNode> getNodes() {
		return nodes;
	}

	public static int distance(Node n1, Node n2) {
		LinkedList<AbstractNode> visited = new LinkedList<AbstractNode>();
		LinkedList<AbstractNode> toVisit = new LinkedList<AbstractNode>();
		toVisit.add(n1);
		visited.add(n1);
		int distance = 0;
		int nodeCurrentDistance = 1;
		AbstractNode currentNode;
		Collection<DirectionalLink> adiacency;
		while (!toVisit.isEmpty()) {
			currentNode = toVisit.pop();

			nodeCurrentDistance--;// System.out.println(currentNode+": "+nodeCurrentDistance+" "+distance);
			if (currentNode.equals(n2)) {
				return distance;
			}

			adiacency = currentNode.getAdiacencyList();
			for (DirectionalLink node : adiacency) {
				AbstractNode target = node.getTarget();
				if (!visited.contains(target)) {
					visited.add(target);
					toVisit.add(target);
				}
			}
			if (nodeCurrentDistance == 0) {
				distance++;
				nodeCurrentDistance = toVisit.size();
			}

		}
		return -1;
	}

	public void setNodes(ConcurrentHashMap<String, AbstractNode> nodes) {
		this.nodes = nodes;
	}

	public ConcurrentHashMap<String, Focus> getFocus() {
		return focus;
	}

	public void setFocus(ConcurrentHashMap<String, Focus> focus) {
		this.focus = focus;
	}

	/*
	 * public static Collection<DirectionalLink>
	 * predictDirectionalLink(AbstractNode root){ LinkedList<AbstractNode>
	 * visited=new LinkedList<AbstractNode>(); LinkedList<AbstractNode>
	 * toVisit=new LinkedList<AbstractNode>();
	 * 
	 * LinkedList<Boolean> signList=new LinkedList<Boolean>();
	 * signList.add(true); LinkedList<DirectionalLink> result = new
	 * LinkedList<DirectionalLink>(); DirectionalLink predictedLink; boolean
	 * isApositiveSign;
	 * 
	 * toVisit.add(root); visited.add(root); int distance=0; int
	 * nodeCurrentDistance=1; AbstractNode currentNode;
	 * Collection<DirectionalLink> adiacency; while (!toVisit.isEmpty()) {
	 * currentNode = toVisit.pop();
	 * nodeCurrentDistance--;//System.out.println(currentNode
	 * +": "+nodeCurrentDistance+" "+distance);
	 * 
	 * //preAction Boolean currentSign = signList.pop();
	 * 
	 * adiacency=currentNode.getAdiacencyList(); for (DirectionalLink link :
	 * adiacency) { AbstractNode target = link.getTarget(); //preAdiacencyAction
	 * 
	 * if(!visited.contains(target)){//make a ConcurrenceMap //newNodeAction
	 * isApositiveSign = currentSign.equals(link.getWeight()>0 );
	 * signList.add(isApositiveSign); if (isApositiveSign) { predictedLink = new
	 * DirectionalLink(target,1); }else { predictedLink = new
	 * DirectionalLink(target,-1); } result.add(predictedLink);
	 * //currentNode.addDirectionalLink(link);
	 * 
	 * visited.add(target); toVisit.add(target); } //postAdiacencyAction
	 * 
	 * } //postAction if(nodeCurrentDistance==0){ //levelAction
	 * nodeCurrentDistance=toVisit.size(); }
	 * 
	 * } return result;
	 * 
	 * } public void predictDirectLink(){ ConcurrentHashMap<AbstractNode,
	 * Collection<DirectionalLink>> addedLink=new
	 * ConcurrentHashMap<AbstractNode, Collection<DirectionalLink>>();
	 * Collection<AbstractNode> allNodes = nodes.values(); for (AbstractNode
	 * abstractNode : allNodes) { addedLink.put(abstractNode,
	 * predictDirectionalLink(abstractNode)); } }
	 */
}
