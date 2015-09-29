package concurrenceClasses;

import java.io.IOException;
import java.util.Collection;

import dataAnalysis.User;
import dataAnalysis.UserMap;
import dataAnalysis.UserMapException;

public class ThreadHomophiliaCalculatorOnMemory extends Thread {
	

	private long nodeID;
	private Collection<Long> targets;
	private UserMap profiles;
	private StringBuilder result=new StringBuilder();;

	public ThreadHomophiliaCalculatorOnMemory(long nodeID,Collection<Long> targets,UserMap profiles) {
		this.nodeID = nodeID;
		this.targets = targets;
		this.profiles = profiles;
	}
	
	@Override
	public void run() {
		User userCaller;
		try {
			userCaller = profiles.get(nodeID);
			User userTarget;
			for (Long userTargetID : targets) {
				try {
					userTarget = profiles.get(userTargetID);
					double homophilia = userCaller.calculateHomophiliaMacth(userTarget);
					result = new StringBuilder();
					result.append(nodeID);
					result.append(",");
					result.append(userTargetID);
					result.append(",");
					result.append(homophilia);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (UserMapException e) {
					result.append("Utente "+userTargetID+" non trovato");
					result.append("\n");
				}
				result.append("\n");
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UserMapException e) {
			result.append("Utente "+nodeID+"non trovato");
			result.append("\n");
		}
	}

	public StringBuilder getResult() {
		return result;
	}

}
