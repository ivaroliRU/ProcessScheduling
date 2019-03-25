package com.ru.usty.scheduling;

import java.util.HashMap;
import java.util.Map;

import com.ru.usty.scheduling.process.ProcessInfo;

public class ProcessRunner implements Runnable{
	Scheduler scheduler;
	Policy policy;
	int quantum;
	Map<Integer, Long> processesServiceTimes;

	public ProcessRunner(Scheduler scheduler, Policy policy, int quantum) {
		this.scheduler = scheduler;
		this.policy = policy;
		this.processesServiceTimes = new HashMap<Integer, Long>();
		this.quantum = quantum;
	}

	@Override
	public void run() {
		while(true) {
			try {
				scheduler.getQueue.acquire();
				if(scheduler.processQueue.size() == 0) {
					scheduler.getQueue.release();
					continue;
				}
				
				int process = scheduler.processQueue.remove();
				scheduler.getQueue.release();
				
				scheduler.getExecutioner.acquire();
				ProcessInfo pInfo = scheduler.processExecution.getProcessInfo(process);
				scheduler.getExecutioner.release();
				
				runProcess(pInfo, process);
				
			} catch (Exception e) {
				//here we failed to get the ProcessInfo likely because of a race condition so we try again
				scheduler.getExecutioner.release();
				continue;
			}
		}
	}
	
	private void runProcess(ProcessInfo pInfo, int id) {
		long timeLeft;
		if(processesServiceTimes.containsKey(id)) {
			timeLeft = pInfo.totalServiceTime - processesServiceTimes.get(id);
		}
		else {
			timeLeft = pInfo.totalServiceTime;
			processesServiceTimes.put(id, (long)0);
		}
		
		switch(policy) {
		case FCFS:
			try {
				scheduler.getExecutioner.acquire();
				scheduler.processExecution.switchToProcess(id);
				scheduler.getExecutioner.release();
				
				//add 50 ms to offset any time errors (so that we for sure finish the process)
				Thread.sleep(timeLeft + Scheduler.TimeErrorOffset);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case RR:
			try {
				scheduler.getExecutioner.acquire();
				scheduler.processExecution.switchToProcess(id);
				scheduler.getExecutioner.release();
				
				if(timeLeft <= quantum) {
					Thread.sleep(timeLeft + Scheduler.TimeErrorOffset);
				}
				else {
					//wait for the round robin time and the add the process back to the queue
					long prev = processesServiceTimes.get(id);
					processesServiceTimes.put(id, prev + (long)quantum);
					Thread.sleep(quantum);
					
					scheduler.getQueue.acquire();
					scheduler.processQueue.add(id);
					scheduler.getQueue.release();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
	}
}
