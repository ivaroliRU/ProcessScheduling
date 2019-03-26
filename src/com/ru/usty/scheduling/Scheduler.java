/*
 * In our implementation the scheduler class only job is to put each process into
 * the correct queue. Then we have a thread that is always running that get's the
 * next process to run according to the policy.
 * 
 * The actual scheduling of the processes for SRT, STN and HRRN are in the 
 * ProcessComparable class. There we order correctly into a priority queue.
 * 
 * The reason for this implementation is we don't want to have huge redundant
 * functions filled with switch cases.
 */

package com.ru.usty.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import com.ru.usty.scheduling.process.ProcessExecution;

public class Scheduler {
	//semaphores so that the thread can access the queue and the process execution
	public Semaphore getQueue, getExecutioner;
	
	//regular queue used for RR and FCFS
	public Queue<ProcessComparable> processQueue;
	//priority queue used in SPN, HRRN and SRT
	public PriorityQueue<ProcessComparable> priorityProcessQueue;
	//Array list of queues used for FB
	public ArrayList<Queue<ProcessComparable>> FBQueue;
	
	ProcessExecution processExecution;
	Policy policy;
	int quantum;
	ProcessRunner runner;
	Thread backgroundThread;
	
	//processes and their added times;
	Map<Integer, Long> processesAddedTimes;
	
	public Scheduler(ProcessExecution processExecution) {
		this.processExecution = processExecution;
		this.getQueue = new Semaphore(1, true);
		this.getExecutioner = new Semaphore(1, true);
	}

	public void startScheduling(Policy policy, int quantum) {
		this.policy = policy;
		this.quantum = quantum;
		this.processesAddedTimes = new HashMap<Integer, Long>();
		
		//we only need to initialise a priority queue for all policies because
		//if the elements inside a priority queue always return 0 when comapred then the priority queue acts like a normal queue!
		//Resulting in a much cleaner code with the same functionality as the ugly switch statements give
		switch(policy) {
		case FCFS:	//First-come-first-served
			System.out.println("Starting new scheduling task: First-come-first-served");
			processQueue = new LinkedList<ProcessComparable>();
			break;
		case RR:	//Round robin
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);
			processQueue = new LinkedList<ProcessComparable>();
			break;
		case SPN:	//Shortest process next
			System.out.println("Starting new scheduling task: SPN, quantum = " + quantum);
			priorityProcessQueue = new PriorityQueue<ProcessComparable>();
			break;
		case SRT:	//Shortest remaining time
			System.out.println("Starting new scheduling task: Shortest remaining time");
			priorityProcessQueue = new PriorityQueue<ProcessComparable>();
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("Starting new scheduling task: Highest response ratio next");
			priorityProcessQueue = new PriorityQueue<ProcessComparable>();
			break;
		case FB:	//Feedback
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			FBQueue = new ArrayList<Queue<ProcessComparable>>();
			FBQueue.add(new LinkedList<ProcessComparable>());
			break;
		}
		
		//stop the thread and initialize a new one for the new policy
		if(runner != null) {
			runner.ResetThread();
			try {
				backgroundThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		runner = new ProcessRunner(this, policy, quantum);
		backgroundThread = new Thread(runner);
		backgroundThread.start();
	}

	//This function adds the process to the correct queue according to it's policy
	//The process runner thread handles everything else
	public void processAdded(int processID) {
		long addedTime = System.currentTimeMillis();
		processesAddedTimes.put(processID, addedTime);
		
		//below using a queue
		if(policy == Policy.FCFS || policy == Policy.RR) {
			try {
				getQueue.acquire();
				//we don't need to specify the length of the process
				processQueue.add(new ProcessComparable(policy, System.currentTimeMillis(), 0, processID));
				getQueue.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//below using a priority queue
		else if (policy == Policy.SRT || policy == Policy.HRRN || policy == Policy.HRRN){
			try {
				getExecutioner.acquire();
				long length = processExecution.getProcessInfo(processID).totalServiceTime;
				getExecutioner.release();
								
				getQueue.acquire();
				priorityProcessQueue.add(new ProcessComparable(policy, System.currentTimeMillis(), length, processID));
				getQueue.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//feedback
		else {
			try {
				getQueue.acquire();
				//we also don't need to specify the length of the process here
				FBQueue.get(0).add(new ProcessComparable(policy, System.currentTimeMillis(), 0, processID));
				getQueue.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//don't really need to do anything here except for time measurements
	//the process runner thread handles all the process switching
	public void processFinished(int processID) {
		//long finishedTime = System.currentTimeMillis();
	}
}
