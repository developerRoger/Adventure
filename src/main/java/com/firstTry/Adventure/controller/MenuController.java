package com.firstTry.Adventure.controller;

import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.firstTry.Adventure.entity.MenuEntity;
import com.firstTry.Adventure.service.MenuService;
import com.firstTry.Adventure.utils.PageUtils;
import com.firstTry.Adventure.utils.Query;
import com.firstTry.Adventure.utils.R;




/**
 * 
 * 控制层
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-08-02 16:33:50
 */
@RestController
@RequestMapping("/adventure/menu")
public class MenuController {
	@Autowired
	private MenuService menuService;
	
	/**	
	 * 列表
	 */
	@RequestMapping("/queryList")
//	@RequiresPermissions("adventure:menu:list")
	public R queryList(@RequestParam Map<String, Object> params){
		//查询列表数据
        Query query = new Query(params);

		List<MenuEntity> menuList = menuService.query(query);
		int total = menuService.queryTotal(query);
		
		PageUtils pageUtil = new PageUtils(menuList, total, query.getLimit(), query.getPage());
		
		return R.ok().put("page", pageUtil);
	}
	
	/**	
	 * 获取所有列表
	 */
	@RequestMapping("/list")
//	@RequiresPermissions("adventure:menu:list")
	public List<MenuEntity> list(){
		List<MenuEntity> menuList = menuService.query(null);
		
		return menuList;
	}
	
	/**
	 * 信息
	 */
	@RequestMapping("/info/{id}")
//	@RequiresPermissions("adventure:menu:info")
	public R info(@PathVariable("id") Long id){
		MenuEntity menu = menuService.queryObject(id);
		
		return R.ok().put("menu", menu);
	}
	
	/**
	 * 保存
	 */
	@RequestMapping("/save")
//	@RequiresPermissions("adventure:menu:save")
	public R save(@RequestBody MenuEntity menu){
		menuService.save(menu);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@RequestMapping("/update")
//	@RequiresPermissions("adventure:menu:update")
	public R update(@RequestBody MenuEntity menu){
		menuService.update(menu);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@RequestMapping("/delete")
//	@RequiresPermissions("adventure:menu:delete")
	public R delete(@RequestBody Long[] ids){
	   for(int a=0;a<ids.length; a++)
		menuService.delete(ids[a]);
		
		return R.ok();
	}
	
}
