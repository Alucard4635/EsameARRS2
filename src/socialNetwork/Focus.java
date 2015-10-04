package socialNetwork;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class Focus implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8555153057871406532L;
	private String focusId;
	
	public Focus(String id) {
		focusId = id;
	}
	
	public Focus() {
	}
	
	private ConcurrentHashMap<String, AbstractNode> nodes=new ConcurrentHashMap<String, AbstractNode>();

	public void enroll(AbstractNode abstractNode) {
		nodes.put(abstractNode.getId(), abstractNode);
	}

	public ConcurrentHashMap<String, AbstractNode> getNodes() {
		return nodes;
	}
	
	public double getFocusClousure(){
		double hit=0.0;
		Collection<AbstractNode> myNodes = nodes.values();
		if (myNodes.isEmpty()) {
			return 0.0;
		}
		for (AbstractNode node : myNodes) {
			for (AbstractNode target : myNodes) {
				if (node.isAdiacentNode(target)) {
					hit++;
				}
			}
		}
		return (hit/myNodes.size())/myNodes.size();
	}
	
	public double getMembershipClousure(){
		double hit=0.0;
		Collection<AbstractNode> myNodes = nodes.values();
		for (AbstractNode node : myNodes) {
			hit += getMembershipClousure(node);
		}
		return hit;
	}

	public double getMembershipClousure(AbstractNode node) {
		double hit=0.0;
		Collection<DirectionalLink> adiacencyList = node.getAdiacencyList();
		if(adiacencyList.isEmpty()){
			return 0;
		}
		
		for (DirectionalLink targetlink : adiacencyList) {
			String targetID = targetlink.getTarget().getId();
			if (nodes.get(targetID)!=null) {
				hit++;
			}
		}
		return hit/adiacencyList.size();
	}

	public String getId() {
		return focusId;
	}

	public void setId(String focusId) {
		this.focusId = focusId;
	}
	@Override
	public int hashCode() {
		return focusId.hashCode();
	}
	@Override
	public String toString() {
		return focusId;
	}
}
