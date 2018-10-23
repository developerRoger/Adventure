package com.firstTry.Adventure.vo;

import java.io.Serializable;

/**
 * @Description: get city by ip
 * @author Roger
 * @Date 2018/10/15
 */
public class IpVo {
	private Integer code;
	private Address address;

	public class Address implements Serializable {
		private String ip;
		private String region;
		private String city;
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public String getRegion() {
			return region;
		}
		public void setRegion(String region) {
			this.region = region;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		@Override
		public String toString() {
			return "Address [ip=" + ip + ", region=" + region + ", city=" + city + "]";
		}
		
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "IpVo [code=" + code + ", address=" + address + "]";
	}
	
}
