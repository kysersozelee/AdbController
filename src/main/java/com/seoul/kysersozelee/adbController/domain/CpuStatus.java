package com.seoul.kysersozelee.adbController.domain;

public class CpuStatus {

	private int core;
	private int user;
	private int nice;
	private int system;
	private int idle;
	private int ioWait;
	private int irq;
	private int softIrq;

	public int getCore() {
		return core;
	}

	public void setCore(int core) {
		this.core = core;
	}

	public int getUser() {
		return user;
	}

	public void setUser(int user) {
		this.user = user;
	}

	public int getNice() {
		return nice;
	}

	public void setNice(int nice) {
		this.nice = nice;
	}

	public int getSystem() {
		return system;
	}

	public void setSystem(int system) {
		this.system = system;
	}

	public int getIdle() {
		return idle;
	}

	public void setIdle(int idle) {
		this.idle = idle;
	}

	public int getIoWait() {
		return ioWait;
	}

	public void setIoWait(int ioWait) {
		this.ioWait = ioWait;
	}

	public int getIrq() {
		return irq;
	}

	public void setIrq(int irq) {
		this.irq = irq;
	}

	public int getSoftIrq() {
		return softIrq;
	}

	public void setSoftIrq(int softIrq) {
		this.softIrq = softIrq;
	}

	@Override
	public String toString() {
		return "Cpu [core=" + core + ", user=" + user + ", nice=" + nice + ", system=" + system + ", idle=" + idle
				+ ", ioWait=" + ioWait + ", irq=" + irq + ", softIrq=" + softIrq + "]";
	}
}
