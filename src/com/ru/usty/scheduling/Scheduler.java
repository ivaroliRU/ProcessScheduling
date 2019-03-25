package com.ru.usty.scheduling;

import java.awt.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import com.ru.usty.scheduling.process.ProcessExecution;
import com.ru.usty.scheduling.process.ProcessInfo;

public class Scheduler {
	//run each process for 300ms or less
	final static long RoundRobinRunningTime = 300;
	//Offset any time error so we for sure finish running each process
	final static long TimeErrorOffset = 50;
	
	public Semaphore getQueue, getExecutioner;
	public Queue<Integer> processQueue;
	public PriorityQueue<ProcessInfo> priorityProcessQueue;
	Map<Integer, Long> processesServiceTimes;
	
	ProcessExecution processExecution;
	Policy policy;
	int quantum;
	//processes and their added times;
	Map<Integer, Long> processesAddedTimes;
	
	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public Scheduler(ProcessExecution processExecution) {
		this.processExecution = processExecution;
		this.getQueue = new Semaphore(1, true);
		this.getExecutioner = new Semaphore(1, true);
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void startScheduling(Policy policy, int quantum) {
		this.policy = policy;
		this.quantum = quantum;
		this.processesAddedTimes = new HashMap<Integer, Long>();//processes and 

		switch(policy) {
		case FCFS:	//First-come-first-served
			System.out.println("Starting new scheduling task: First-come-first-served");
			processQueue = new LinkedList<Integer>();
			break;
		case RR:	//Round robin
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);
			processQueue = new LinkedList<Integer>();
			break;
		case SPN:	//Shortest process next
			System.out.println("Starting new scheduling task: Shortest process next");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case SRT:	//Shortest remaining time
			System.out.println("Starting new scheduling task: Shortest remaining time");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("Starting new scheduling task: Highest response ratio next");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case FB:	//Feedback
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		}

		Thread thread = new Thread(new ProcessRunner(this, policy, quantum));
		thread.start();
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processAdded(int processID) {
		long addedTime = System.currentTimeMillis();
		processesAddedTimes.put(processID, addedTime);		
		
		switch(policy) {
		case FCFS:
			try {
				getQueue.acquire();
				processQueue.add(processID);
				getQueue.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case RR:
			try {
				getQueue.acquire();
				processQueue.add(processID);
				getQueue.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		default:
			break;
		}
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processFinished(int processID) {
		long finishedTime = System.currentTimeMillis();
		
		switch(policy) {
		case FCFS:
			break;
		default:
			System.out.println("2");
			break;
		}
	}
}
