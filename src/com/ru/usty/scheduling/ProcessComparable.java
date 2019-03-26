package com.ru.usty.scheduling;

public class ProcessComparable implements Comparable{

	public Long startingTime, length;
	public Integer timeLeft, id;
	
	Policy policy;
	
	public ProcessComparable(Policy policy, long startingTime, long length, int id) {
		this.startingTime = startingTime;
		this.timeLeft = (int)length;
		this.length = length;
		this.id = id;
		this.policy = policy;
	}
	
	public Double getResponseRatio() {
		long wait = System.currentTimeMillis() - startingTime;
		return (double)(wait + length)/length;
	}
	
	@Override
	public int compareTo(Object arg0) {
		switch(policy) {
		case SPN:
			return this.length.compareTo(((ProcessComparable)arg0).length);
		case SRT:
			return this.timeLeft.compareTo(((ProcessComparable)arg0).timeLeft);
		case HRRN:
			return this.getResponseRatio().compareTo(((ProcessComparable)arg0).getResponseRatio());
		default:
			return 0;
		}
	}
}
