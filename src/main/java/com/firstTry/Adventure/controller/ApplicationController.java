package com.firstTry.Adventure.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.firstTry.Adventure.entity.ApplicationEntity;
import com.firstTry.Adventure.service.ApplicationService;
import com.firstTry.Adventure.service.MenuService;
import com.firstTry.Adventure.utils.PageUtils;
import com.firstTry.Adventure.utils.Query;
import com.firstTry.Adventure.utils.R;

/**
 * 
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-08-01 14:29:11
 */
@RestController
@RequestMapping("/adventure/application")
public class ApplicationController {
	@Autowired
	private ApplicationService applicationService;
	
	@Autowired
	private MenuService menuService;//菜单
	/**
	 * 列表
	 */
	@RequestMapping("/queryList")
	// @RequiresPermissions("Adventure:application:list")
	public R queryList(@RequestParam Map<String, Object> params) {
		Query query;
		if (params.size()==0) {
			query = null;
		} else {
			// 查询列表数据
			query = new Query(params);
		}

		List<ApplicationEntity> applicationList = applicationService.query(query);
		int total = applicationService.queryTotal(query);

		PageUtils pageUtil = new PageUtils(applicationList, total, query.getLimit(), query.getPage());

		return R.ok().put("page", pageUtil);
	}
	
	/**	
	 * 获取所有列表
	 */
	@RequestMapping("/list")
//	@RequiresPermissions("adventure:application:list")
	public List<ApplicationEntity> list(){
		List<ApplicationEntity> applicationList = applicationService.query(null);
		//查询条件
		Map<String,Object> queryMap=new HashMap<>();
		for(ApplicationEntity app:applicationList){
			queryMap.put("appCode", app.getCode());
			app.setListMenu(menuService.query(queryMap));
		}
		return applicationList;
	}

	/**
	 * 信息
	 */
	@RequestMapping("/info/{code}")
	// @RequiresPermissions("Adventure:application:info")
	public R info(@PathVariable("code") String code) {
		ApplicationEntity application = applicationService.queryObject(code);

		return R.ok().put("application", application);
	}

	/**
	 * 保存
	 */
	@RequestMapping("/save")
	// @RequiresPermissions("Adventure:application:save")
	public R save(@RequestBody ApplicationEntity application) {
		applicationService.save(application);

		return R.ok();
	}

	/**
	 * 修改
	 */
	@RequestMapping("/update")
	// @RequiresPermissions("Adventure:application:update")
	public R update(@RequestBody ApplicationEntity application) {
		applicationService.update(application);

		return R.ok();
	}

	/**
	 * 删除
	 */
	@RequestMapping("/delete")
	// @RequiresPermissions("Adventure:application:delete")
	public R delete(@RequestBody String[] codes) {
		for (int a = 0; a < codes.length; a++)
			applicationService.delete(codes[a]);

		return R.ok();
	}

}
