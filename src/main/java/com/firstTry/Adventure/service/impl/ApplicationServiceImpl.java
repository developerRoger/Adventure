package com.firstTry.Adventure.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import com.firstTry.Adventure.entity.ApplicationEntity;
import com.firstTry.Adventure.mapper.ApplicationMapper;
import com.firstTry.Adventure.service.ApplicationService;



@Service("applicationService")
public class ApplicationServiceImpl implements ApplicationService {
	@Autowired
	private ApplicationMapper applicationDao;
	@Override
	public List<ApplicationEntity> query(Map<String, Object> map){
		return applicationDao.query(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return applicationDao.queryTotal(map);
	}
	
	@Override
	public void save(ApplicationEntity application){
		applicationDao.save(application);
	}
	
	@Override
	public void update(ApplicationEntity application){
		applicationDao.update(application);
	}
	
	@Override
	public void delete(String code){
		applicationDao.delete(code);
	}

	@Override
	public ApplicationEntity queryObject(String code) {
		// TODO Auto-generated method stub
		return applicationDao.queryObject(code);
	}
	
	
}
