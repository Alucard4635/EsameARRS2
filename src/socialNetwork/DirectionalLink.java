package socialNetwork;

import java.io.Serializable;

public class DirectionalLink implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6242562391728284448L;
	private AbstractNode targetNode;
	private double weight=1;
	public DirectionalLink(AbstractNode target){
		this(target,0.0);
	}
	public DirectionalLink(AbstractNode abstractNode,double weight){
		this.targetNode = abstractNode;
		this.weight = weight;
	}
	public AbstractNode getTarget() {
		return targetNode;
	}
	public void setTarget(Node target) {
		this.targetNode = target;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DirectionalLink) {
			DirectionalLink link = (DirectionalLink) obj;
			if (targetNode.equals(link.targetNode)&&weight==link.weight) {
				return true;
			}
		}
		return false;
	}
	@Override
	public String toString() {
		return "N: "+targetNode+" W: "+weight;
	}
}
