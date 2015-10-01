package socialNetwork;

import java.io.Serializable;
import java.util.Collection;

public abstract class AbstractNode implements Serializable{
	private String id;
	private int inDegree=0;
	private int outDegree=0;
	
	
	
	public AbstractNode(String idendt) {
		id=idendt;
	}
	
	public AbstractNode() {
	}
	
	public void link(DirectionalLink link){
		directionalLink(link);
		link.getTarget().directionalLink(link);
	}
	
	public void directionalLink(DirectionalLink link){
		AbstractNode target=link.getTarget();
		if(target!=null&&!target.equals(this)){
			updateDegree(target);
			addDirectionalLink(link);
		}
	}
	
	public void directionalLink(AbstractNode target,double weight){
		directionalLink(new DirectionalLink(target,weight));
	}
	
	protected abstract void addDirectionalLink(DirectionalLink link); 
	
	private void updateDegree(AbstractNode target) {
		target.inDegree++;
		outDegree++;
	}

	public void link(AbstractNode n,double weight) {
		directionalLink(n,weight);
		n.directionalLink(this,weight);
	}
	
	public abstract Collection<DirectionalLink> getAdiacencyList();

	public boolean isAdiacentNode(AbstractNode target) {
		AbstractNode node=findAdiacentNode(target.getId());
		return node!=null;
	}

	public abstract AbstractNode findAdiacentNode(String targetID) ;
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public String getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractNode) {
			AbstractNode n=(AbstractNode) obj;
			return this.id.equals(n.id);
			
		}
		return false;
	}
	
	@Override
	public String toString() {
		return id;
	}

	public int getInDegree() {
		return inDegree;
	}

	public int getOutDegree() {
		return outDegree;
	}
	
	public long triadricClosure(){
		Collection<DirectionalLink> list = getAdiacencyList();
		Collection<DirectionalLink> listOfMyTarget;
		long hit=0;
		boolean gotHit;
		long numberOfFriendWithATriangle=0;//TODO
		long closedTriangles;
		for (DirectionalLink directionalLink : list) {
			AbstractNode target = directionalLink.getTarget();
			closedTriangles = getTrianglesClosed(hit, target);
			if (closedTriangles>0) {
				hit += closedTriangles;
				numberOfFriendWithATriangle++;
			}
		}
		return hit;
	}

	private long getTrianglesClosed(long hit, AbstractNode target) {
		Collection<DirectionalLink> listOfMyTarget;
		listOfMyTarget = target.getAdiacencyList();
		for (DirectionalLink targetOfMyTarget : listOfMyTarget) {
			if (isAdiacentNode(targetOfMyTarget.getTarget())) {
				hit++;
			}
		}
		return hit;
	}
}
