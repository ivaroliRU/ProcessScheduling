package com.ru.usty.scheduling;

public class ProcessComparable implements Comparable{

	public long startingTime, length;
	public int timeLeft, id;
	
	Policy policy;
	
	public ProcessComparable(Policy policy, long startingTime, long length, int id) {
		this.startingTime = startingTime;
		this.timeLeft = (int)length;
		this.length = length;
		this.id = id;
	}
	
	@Override
	public int compareTo(Object arg0) {
		switch(policy) {
		case SPN:
			return this.compareTo(((ProcessComparable)arg0).length);
		}
		// TODO Auto-generated method stub
		return 0;
	}
}
