package com.firstTry.Adventure.vo;

/**
 * @Description: get city by ip
 * @author Roger
 * @Date 2018/10/15
 */
public class SinaIpVo {
	private Integer ret;
	private String province;
	private String city;

	public Integer getRet() {
		return ret;
	}

	public void setRet(Integer ret) {
		this.ret = ret;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String toString() {
		return "SinaIpVo [ret=" + ret + ", province=" + province + ", city=" + city + "]";
	}

}
