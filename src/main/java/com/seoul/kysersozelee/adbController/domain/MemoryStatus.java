package com.seoul.kysersozelee.adbController.domain;

public class MemoryStatus {

	private int total;
	private int free;
	private int used;
	private int lost;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getFree() {
		return free;
	}

	public void setFree(int free) {
		this.free = free;
	}

	public int getUsed() {
		return used;
	}

	public void setUsed(int used) {
		this.used = used;
	}

	public int getLost() {
		return lost;
	}

	public void setLost(int lost) {
		this.lost = lost;
	}

	@Override
	public String toString() {
		return "Memory [total=" + total + ", free=" + free + ", used=" + used + ", lost=" + lost + "]";
	}

}
