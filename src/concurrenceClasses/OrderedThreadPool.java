package concurrenceClasses;

import java.util.Collection;
import java.util.LinkedList;

public class OrderedThreadPool<ThreadType extends Thread> {
	private Thread[] pool;
	private int poolIndex=0;
	private long sleepTime = 50;
	public OrderedThreadPool(int size,long time) {
		pool=new Thread[size];
		sleepTime=time;
		
	}
	@SuppressWarnings("unchecked")
	public synchronized ThreadType replaceThread(ThreadType thread) throws InterruptedException {
		Thread replacedThread = pool[poolIndex];
		while (replacedThread!=null&&replacedThread.isAlive()) {
			Thread.sleep(sleepTime);
		}
		thread.start();
		pool[poolIndex++]=thread;
		poolIndex=poolIndex%pool.length;
		return (ThreadType) replacedThread;
		
	}
	public Collection<ThreadType> getThread() {
		LinkedList<ThreadType> thread = new LinkedList<ThreadType>();
		for (int i = 0; i < pool.length; i++) {
			thread.add((ThreadType) pool[(poolIndex+i)%pool.length]);
		}
		return thread;
		
	}
}
