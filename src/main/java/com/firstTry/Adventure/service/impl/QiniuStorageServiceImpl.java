package com.firstTry.Adventure.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.firstTry.Adventure.config.IdProcessor;
import com.firstTry.Adventure.mapper.QiniuStorageMapper;
import com.firstTry.Adventure.entity.QiniuStorageEntity;
import com.firstTry.Adventure.service.QiniuStorageService;



@Service("qiniuStorageService")
public class QiniuStorageServiceImpl implements QiniuStorageService {
	@Autowired
	private QiniuStorageMapper qiniuStorageDao;
	
	@Autowired
	private IdProcessor<Long> idProcessor;//id自动生成
	
	@Override
	public QiniuStorageEntity queryObject(Long id){
		return qiniuStorageDao.queryObject(id);
	}
	
	@Override
	public List<QiniuStorageEntity> query(Map<String, Object> map){
		return qiniuStorageDao.query(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return qiniuStorageDao.queryTotal(map);
	}
	
	@Override
	public void save(QiniuStorageEntity qiniuStorage){
		//插入的时候加入id
		if(null==qiniuStorage.getId())
			qiniuStorage.setId(idProcessor.nextId());
		qiniuStorageDao.save(qiniuStorage);
	}
	
	@Override
	public void update(QiniuStorageEntity qiniuStorage){
		qiniuStorageDao.update(qiniuStorage);
	}
	
	@Override
	public void delete(Long id){
		qiniuStorageDao.delete(id);
	}
	
	
}
