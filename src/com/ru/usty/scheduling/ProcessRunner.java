/*
 * This Class implements a thread that is always running and checking if there are
 * any new processes to be run. If there aren't any then just check again until there are.
 */

package com.ru.usty.scheduling;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.ru.usty.scheduling.process.ProcessInfo;

public class ProcessRunner implements Runnable{
	final static long TimeErrorOffset = 70;
	
	Scheduler scheduler;
	Policy policy;
	//the quentum
	int quantum;
	//used for FB
	int queueNumber;
	boolean isRunning;
	Map<Integer, Long> processesServiceTimes;

	public ProcessRunner(Scheduler scheduler, Policy policy, int quantum) {
		this.scheduler = scheduler;
		this.policy = policy;
		this.processesServiceTimes = new HashMap<Integer, Long>();
		this.quantum = quantum;
		this.isRunning = true;
	}

	public void ResetThread() {
		this.isRunning = false;
	}
	
	@Override
	public void run() {
		ProcessComparable process = null;
		
		while(isRunning) {
			System.out.println("ASDF");
			try {
				scheduler.getQueue.acquire();
				//if we have a process then don't do anything
				process = (process == null)?getNextId():process;
				
				if(process == null) {
					scheduler.getQueue.release();
					continue;
				}
				
				scheduler.getQueue.release();
								
				scheduler.getExecutioner.acquire();
				ProcessInfo pInfo = scheduler.processExecution.getProcessInfo(process.id);
				scheduler.getExecutioner.release();
				
				runProcess(pInfo, process);
				process = null;
				
			} catch (Exception e) {
				//here we failed to get the ProcessInfo likely because of a race condition so we try again
				//and give back the process
				scheduler.getExecutioner.release();
				continue;
			}
		}
	}
	
	private void runProcess(ProcessInfo pInfo, ProcessComparable process) {
		//below we calculate the remaining time of the process
		//use that either to run the process the whole remaining time or to check if the quantum is larger then
		//the remaining time
		long timeLeft;
		if(processesServiceTimes.containsKey(process.id)) {
			timeLeft = pInfo.totalServiceTime - processesServiceTimes.get(process.id);
		}
		else {
			timeLeft = pInfo.totalServiceTime;
			processesServiceTimes.put(process.id, (long)0);
		}
		
		//none preemptive dót
		//S.s. hér keyrum við processann til enda
		if(policy == Policy.FCFS || policy == Policy.SPN || policy == Policy.HRRN) {
			try {
				scheduler.getExecutioner.acquire();
				scheduler.processExecution.switchToProcess(process.id);
				scheduler.getExecutioner.release();
				
				//add 50 ms to offset any time errors (so that we for sure finish the process)
				Thread.sleep(timeLeft + TimeErrorOffset);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//preemtive
		//run a process in bursts
		else if(policy == Policy.RR || policy == Policy.SRT || policy == Policy.FB){
			try {
				//change to the process
				scheduler.getExecutioner.acquire();
				scheduler.processExecution.switchToProcess(process.id);
				scheduler.getExecutioner.release();
				
				//then either wait sleep the whole process if the quantum is larger or sleep only for the quantum
				if(timeLeft <= quantum) {
					Thread.sleep(timeLeft + TimeErrorOffset);
				}
				else {
					long prev = processesServiceTimes.get(process.id);
					processesServiceTimes.put(process.id, prev + (long)quantum);
					
					process.timeLeft = (int)timeLeft;
					
					Thread.sleep(quantum);
					
					scheduler.getQueue.acquire();
					
					//add back to the queue
					if(policy == Policy.RR) {
						scheduler.processQueue.add(process);
					}
					//SRT
					else if (policy == Policy.SRT){
						scheduler.priorityProcessQueue.add(process);
					}
					//Feedback
					else {
						if(scheduler.FBQueue.size() <= queueNumber+1) {
							scheduler.FBQueue.add(new LinkedList<ProcessComparable>());
						}
						System.out.println("Adding back to queue " + (queueNumber + 1));
						scheduler.FBQueue.get(queueNumber+1).add(process);
					}
					scheduler.getQueue.release();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//get the next process to be run
	//almost all the scheduling functionality is implemented in the comparable object
	private ProcessComparable getNextId() {
		//get the next element out of the regular queue to be run
		if(policy == Policy.RR || policy == Policy.FCFS) {
			if(scheduler.processQueue.size() == 0) {
				return null;
			}
			
			return scheduler.processQueue.remove();
		}
		//get the next element out of the priority queue to be run
		//see ProcessComparable for the actual scheduling algorithm implementation
		else if (policy == Policy.SRT || policy == Policy.HRRN || policy == Policy.HRRN){
			if(scheduler.priorityProcessQueue.size() == 0) {
				return null;
			}
			
			return scheduler.priorityProcessQueue.poll();
		}
		//get the next process for the array of queues
		else {
			for(int i = 0; i < scheduler.FBQueue.size(); i++) {
				if(scheduler.FBQueue.get(i).size() == 0) {
					continue;
				}
				queueNumber = i;
				return scheduler.FBQueue.get(i).remove();
			}
			return null;
		}
	}
}
