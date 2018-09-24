package com.firstTry.Adventure.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.firstTry.Adventure.config.IdProcessor;
import com.firstTry.Adventure.entity.QiniuStorageEntity;
import com.firstTry.Adventure.service.QiniuStorageService;
import com.firstTry.Adventure.utils.PageUtils;
import com.firstTry.Adventure.utils.QiniuCloudUtil;
import com.firstTry.Adventure.utils.Query;
import com.firstTry.Adventure.utils.R;

/**
 * @data 2018/8/20
 * @author Roger
 * CDN七牛云控制层
 */
@RestController
@RequestMapping("/adventure/qiniu")
public class QiniuController {
	
	//日志
	private static Log log = LogFactory.getLog(QiniuController.class);
	
	@Autowired
	private IdProcessor<Long> idProcessor;
	@Autowired
	private  QiniuStorageService qiniuStorageService;
	
	/**
	 * 七牛上传图片
	 * @param image
	 * @param request
	 * @return
	 */
    @SuppressWarnings("static-access")
	@RequestMapping(value="/uploadImg", method=RequestMethod.POST)
    public R uploadImg(@RequestParam MultipartFile image,String name,HttpServletRequest request) {
        R r = new R();
        if (image.isEmpty()) {
            r.error(500,"文件为空，请重新上传");
            return r;
        }

        try {
            byte[] bytes = image.getBytes();
            String imageName = idProcessor.nextId().toString();
            QiniuCloudUtil qiniuUtil = new QiniuCloudUtil();
       
                //使用base64方式上传到七牛云
                String url = qiniuUtil.put64image(bytes, imageName);
               r.put("url", url);
               //本地备份存储
               QiniuStorageEntity qiniuStorage=new QiniuStorageEntity();
               qiniuStorage.setName("upload...");
               qiniuStorage.setUrl(url);
               qiniuStorage.setName(name);
               qiniuStorageService.save(qiniuStorage);
            return r;
        } catch (Exception e) {
        	r.error(500,"文件上传发生异常！");
            return r;
        }
    }
    
	/**	
	 * 列表
	 */
	@RequestMapping("/queryList")
	public R queryList(@RequestParam Map<String, Object> params){
		//示例日志打印
		log.info("/adventure/qiniustorage/queryList obj:"+params.toString());
		//查询列表数据
        Query query = new Query(params);

		List<QiniuStorageEntity> qiniuStorageList = qiniuStorageService.query(query);
		int total = qiniuStorageService.queryTotal(query);
		
		PageUtils pageUtil = new PageUtils(qiniuStorageList, total, query.getLimit(), query.getPage());
		
		return R.ok().put("page", pageUtil);
	}
	
	/**	
	 * 获取所有列表
	 */
	@RequestMapping("/list")
	public List<QiniuStorageEntity> list(boolean nowDate){
		Map<String, Object> map=new HashMap<String, Object>();
		if(nowDate){
			map.put("createTime", new Date());
		}else{
			map=null;
		}
		List<QiniuStorageEntity> qiniuStorageList = qiniuStorageService.query(map);
		
		return qiniuStorageList;
	}
	
	/**
	 * 信息
	 */
	@RequestMapping("/info")
	public R info(Long id){
		QiniuStorageEntity qiniuStorage = qiniuStorageService.queryObject(id);
		
		return R.ok().put("qiniuStorage", qiniuStorage);
	}
	
	/**
	 * 保存
	 */
	@RequestMapping("/save")
	public R save(@RequestBody QiniuStorageEntity qiniuStorage){
		qiniuStorageService.save(qiniuStorage);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@RequestMapping("/update")
	public R update(@RequestBody QiniuStorageEntity qiniuStorage){
	log.info("/adventure/qiniustorage/update obj:"+qiniuStorage.toString());
		qiniuStorageService.update(qiniuStorage);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@RequestMapping("/delete")
	public R delete(@RequestBody Long[] ids){
	   for(int a=0;a<ids.length; a++)
		qiniuStorageService.delete(ids[a]);
		
		return R.ok();
	}
	
}
