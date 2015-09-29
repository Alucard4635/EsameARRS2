package socialNetwork;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

public class Node extends AbstractNode implements Serializable{
	private Collection<DirectionalLink> adiacencyList;
	
	
	public Node(String idendt) {
		super(idendt);
		adiacencyList=new LinkedList<DirectionalLink>();
	}
	
	public Node() {
	}
	

	public AbstractNode findAdiacentNode(String targetID) {
		for (DirectionalLink directionalLink : adiacencyList) {
			AbstractNode target = directionalLink.getTarget();
			if (target.getId().equals(targetID)) {
				return target;
			}
		}
		return null;
	}

	public Collection<DirectionalLink> getAdiacencyList() {
		return adiacencyList;
	}

	@Override
	protected void addDirectionalLink(DirectionalLink link) {
		adiacencyList.add(link);
		
	}

}
