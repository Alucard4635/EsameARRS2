package socialNetwork;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class HashNode extends AbstractNode implements Serializable{
	private ConcurrentHashMap<String,DirectionalLink> adiacencyMap;
	
	
	public HashNode(String idendt) {
		super(idendt);
		adiacencyMap=new ConcurrentHashMap<String,DirectionalLink>();
	}
	
	public HashNode() {
	}
	
	@Override
	public boolean isAdiacentNode(AbstractNode target) {
		return adiacencyMap.containsKey(target.getId());
	}

	public AbstractNode findAdiacentNode(String targetID) {
		DirectionalLink link = adiacencyMap.get(targetID);
		AbstractNode target = null;
		if (link!=null) {
			target = link.getTarget();
		}
		return target ;
	}

	public Collection<DirectionalLink> getAdiacencyList() {
		return adiacencyMap.values();
	}

	@Override
	protected void addDirectionalLink(DirectionalLink link) {
		String targetID=link.getTarget().getId();
		adiacencyMap.put(targetID,link);
		
	}

}
