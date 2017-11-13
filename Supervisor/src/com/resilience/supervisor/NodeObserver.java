package com.resilience.supervisor;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class NodeObserver implements Runnable {
	
	private int port;
	private String executable;
	private  ArrayBlockingQueue<String> queue;
	
	public NodeObserver(int port, String executable, ArrayBlockingQueue<String> queue) {
		this.port = port;
		this.executable = executable;
		this.queue = queue;
	}

	@Override
	public void run() {
		Process p;
		while(true) {
			try {
				p = Runtime.getRuntime().exec(new String[] {"java", "-Dserver.port="+port, "-jar", executable});
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				queue.put(port+" crashed -> restarting");
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
