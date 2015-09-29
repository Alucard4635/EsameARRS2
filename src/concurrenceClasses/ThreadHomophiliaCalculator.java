package concurrenceClasses;

import java.io.IOException;
import java.util.Collection;

import dataAnalysis.DataArray;
import dataAnalysis.DataArrayException;
import dataAnalysis.User;

public class ThreadHomophiliaCalculator extends Thread {
	

	private long nodeID;
	private Collection<Long> targets;
	private DataArray profiles;
	private StringBuilder result=new StringBuilder();;

	public ThreadHomophiliaCalculator(long nodeID,Collection<Long> targets,DataArray profiles) {
		this.nodeID = nodeID;
		this.targets = targets;
		this.profiles = profiles;
	}
	
	@Override
	public void run() {
		User userCaller = new User();
		String profileInfo = null;
		try {
			profileInfo = profiles.get(nodeID);

			userCaller.parseUser(profileInfo);
			User userTarget = new User();
			for (Long userTargetID : targets) {
				try {
					profileInfo = profiles.get(userTargetID);
					userTarget.parseUser(profileInfo);
					double homophilia = userCaller.calculateHomophiliaMacth(userTarget);
					result = new StringBuilder();
					result.append(nodeID);
					result.append(",");
					result.append(userTargetID);
					result.append(",");
					result.append(homophilia);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (DataArrayException e) {
					result.append("Utente "+userTargetID+" non trovato");
					result.append("\n");
				}
				result.append("\n");
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataArrayException e) {
			result.append("Utente "+nodeID+"non trovato");
			result.append("\n");
		}
	}

	public StringBuilder getResult() {
		return result;
	}

}
