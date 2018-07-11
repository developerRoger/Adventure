package com.firstTry.Adventure.controller;

import com.alibaba.fastjson.JSON;
import com.firstTry.Adventure.service.SysGeneratorService;
import com.firstTry.Adventure.utils.PageUtils;
import com.firstTry.Adventure.utils.Query;
import com.firstTry.Adventure.utils.R;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 代码生成器控制层
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-3-19
 */
@Controller
@RequestMapping("/sys/generator")
public class SysGeneratorController {
	
	@Autowired
	private SysGeneratorService sysGeneratorService;
	
	/**
	 * 列表
	 */
	@ResponseBody
	@ApiOperation(value="查询列表数据", notes="查询当前数据库所有的表名和创建时间等...")
	@ApiImplicitParam(name = "params", value = "用户请求参数", required = false)
	@RequestMapping("/list")
	public R list(@RequestParam Map<String, Object> params){
		
		System.out.println(params);
		//查询列表数据
		Query query = new Query(params);
		List<Map<String, Object>> list = sysGeneratorService.queryList(query);
		int total = sysGeneratorService.queryTotal(query);
		
		PageUtils pageUtil = new PageUtils(list, total, query.getLimit(), query.getPage());
		
		return R.ok().put("page", pageUtil);
	}
	
	/**
	 * 生成代码
	 */
	@ApiOperation(value="生成代码", notes="根据url的表名来指定生成代码并下载到浏览器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "用户请求", required = true),
            @ApiImplicitParam(name = "response", value = "返回给用户的请求", required = true)
    })
	@RequestMapping("/code")
	public void code(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String[] tableNames = new String[]{};
		String tables = request.getParameter("tables");
		tableNames = JSON.parseArray(tables).toArray(tableNames);
		
		byte[] data = sysGeneratorService.generatorCode(tableNames);
		response.reset();  
        response.setHeader("Content-Disposition", "attachment; filename=\""+tables+".zip\"");  
        response.addHeader("Content-Length", "" + data.length);  
        response.setContentType("application/octet-stream; charset=UTF-8");  
  
        IOUtils.write(data, response.getOutputStream());  
	}
	
	/**
	 * 获取单个表的字段实列
	 * @param request
	 * @return
	 */
	@RequestMapping("/queryColumns")
	@ResponseBody
	public R queryColumns(HttpServletRequest request){
		
		String tableName = request.getParameter("tableName");
		//查询列信息
		List<Map<String, String>> columns = sysGeneratorService.queryColumns(tableName);
		
		if(columns.size()>0)
			return R.ok().put("list", columns);
		else
			return R.error("无法根据表名获取数据");	
		
	}
}
