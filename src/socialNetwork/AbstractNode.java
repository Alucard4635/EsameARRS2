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
	
	public double triadricClosure(){
		Collection<DirectionalLink> list = getAdiacencyList();
		if (list.isEmpty()) {
			return 0;
		}
			
		double hit=0;
		boolean gotHit;
//		double numberOfFriendWithATriangle=0.0;
		double closedTriangles;
		for (DirectionalLink directionalLink : list) {
			AbstractNode target = directionalLink.getTarget();
			closedTriangles = getTrianglesClosed(target);
			
			if (closedTriangles>0) {
				hit += closedTriangles;
//				numberOfFriendWithATriangle++;
			}
		}
		return hit/list.size();
	}

	private double getTrianglesClosed(AbstractNode target) {
		Collection<DirectionalLink> listOfMyTarget = target.getAdiacencyList();
		if (listOfMyTarget.isEmpty()) {
			return 0;
		}
		double hit=0;
		for (DirectionalLink targetOfMyTarget : listOfMyTarget) {
			if (isAdiacentNode(targetOfMyTarget.getTarget())) {
				hit++;
			}
		}
		return hit/listOfMyTarget.size();
	}
}
