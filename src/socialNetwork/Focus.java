package socialNetwork;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class Focus implements Serializable {
	
	private ConcurrentHashMap<String, AbstractNode> nodes=new ConcurrentHashMap<String, AbstractNode>();

	public void enroll(AbstractNode abstractNode) {
		nodes.put(abstractNode.getId(), abstractNode);
	}

	public ConcurrentHashMap<String, AbstractNode> getNodes() {
		return nodes;
	}
	
	public long focusClousure(){
		long hit=0;
		Collection<AbstractNode> myNodes = nodes.values();
		for (AbstractNode node : myNodes) {
			for (AbstractNode target : myNodes) {
				if (node.isAdiacentNode(target)) {
					hit++;
				}
			}
		}
		return hit;
	}
	
	public long membershipClousure(){
		long hit=0;
		Collection<AbstractNode> myNodes = nodes.values();
		for (AbstractNode node : myNodes) {
			for (DirectionalLink targetlink : node.getAdiacencyList()) {
				if (nodes.get(targetlink.getTarget().getId())!=null) {
					hit++;
				}
			}
		}
		return hit;
	}
}
