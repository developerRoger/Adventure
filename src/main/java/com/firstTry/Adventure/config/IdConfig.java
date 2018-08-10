package com.firstTry.Adventure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdConfig {
	@Value("${application.workerId}")
	private long workerId;

	@Value("${application.datacenterId}")
	private long datacenterId;

	@Bean(name = "idProcessor")
	public IdProcessor<Long> idProcessor() {

		return new SnowflakeIdProcessor(this.workerId, this.datacenterId);
	}

	public long getDatacenterId() {
		return datacenterId;
	}

	public void setDatacenterId(long datacenterId) {
		this.datacenterId = datacenterId;
	}

	public long getWorkerId() {
		return workerId;
	}

	public void setWorkerId(long workerId) {
		this.workerId = workerId;
	}
}
