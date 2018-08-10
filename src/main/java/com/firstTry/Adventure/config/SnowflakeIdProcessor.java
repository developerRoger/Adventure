package com.firstTry.Adventure.config;

public class SnowflakeIdProcessor implements IdProcessor<Long> {

	private long workerId;
	private long datacenterId;
	private static SnowflakeIdWorker snowflakeIdWorker;

	public SnowflakeIdProcessor(long workerId, long datacenterId) {
		this.workerId = workerId;
		this.datacenterId = datacenterId;
	}

	@Override
	public Long nextId() {
		if (snowflakeIdWorker == null) {
			snowflakeIdWorker = new SnowflakeIdWorker(this.workerId, this.datacenterId);
		}
		return snowflakeIdWorker.nextId();
	}
}
