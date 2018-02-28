package com.firstTry.Adventure.entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 测试jdbc实体类
 * @author Roger
 *
 */
public class UserTest  implements UserDetails{
	private Long id;
	private String name;
	private String mobie;
	private String adders;
	private String password;
 	private String remark;
 	
 	
 	
	public UserTest() {
	}
	public UserTest(Long id, String name, String mobie, String adders, String password, String remark) {
		super();
		this.id = id;
		this.name = name;
		this.mobie = mobie;
		this.adders = adders;
		this.password = password;
		this.remark = remark;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMobie() {
		return mobie;
	}
	public void setMobie(String mobie) {
		this.mobie = mobie;
	}
	public String getAdders() {
		return adders;
	}
	public void setAdders(String adders) {
		this.adders = adders;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

}
