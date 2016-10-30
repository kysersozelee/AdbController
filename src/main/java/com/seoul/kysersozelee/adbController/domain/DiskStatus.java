package com.seoul.kysersozelee.adbController.domain;

public class DiskStatus {

	double size = 0;
	double used = 0;
	double free = 0;
	int blkSize = 0;

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public double getUsed() {
		return used;
	}

	public void setUsed(double used) {
		this.used = used;
	}

	public double getFree() {
		return free;
	}

	public void setFree(double free) {
		this.free = free;
	}

	public int getBlkSize() {
		return blkSize;
	}

	public void setBlkSize(int blkSize) {
		this.blkSize = blkSize;
	}

	@Override
	public String toString() {
		return "Disk [size=" + size + ", used=" + used + ", free=" + free + ", blkSize=" + blkSize + "]";
	}

}