package com.platform.mvc.grouprole;

import java.util.List;

import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.platform.annotation.Service;
import com.platform.mvc.base.BaseService;
import com.platform.mvc.operator.Operator;
import com.platform.plugin.ParamInitPlugin;
import com.platform.tools.ToolCache;

@Service(name = GroupRoleService.serviceName)
public class GroupRoleService extends BaseService {

	@SuppressWarnings("unused")
	private static final Log log = Log.getLog(GroupRoleService.class);
	
	public static final String serviceName = "groupRoleService";

	/**
	 * 分组角色选择
	 * @param groupIds 分组ids
	 */
	public Ret selectRole(String groupIds){
		Ret ret = Ret.create();

		String sqlChecked = getSqlMy("platform.groupRole.findByGroupIds");
		List<Record> checkedList = Db.find(sqlChecked, groupIds);
		ret.put("checkedList", checkedList);
		
		String sqlNoChecked = getSqlMy("platform.groupRole.findByNotGroupIds");
		List<Record> noCheckedList = Db.find(sqlNoChecked, groupIds);
		ret.put("noCheckedList", noCheckedList);
		
		return ret;
	}

	/**
	 * 添加分组拥有的角色
	 * @param groupIds
	 * @param roleIds
	 */
	public void addRole(String groupIds, String roleIds){
		// 保存
		GroupRole gr = new GroupRole();
		gr.setGroupids(groupIds);
		gr.setRoleids(roleIds);
		gr.save();
		
		// 缓存
		cacheAdd(groupIds);
	}

	/**
	 * 删除分组拥有的角色
	 * @param groupRoleIds
	 */
	public void delRole(String groupRoleIds){
		// 删除
		GroupRole gr = GroupRole.dao.findById(groupRoleIds);
		String groupIds = gr.getRoleids();
		gr.delete();
		
		// 缓存
		cacheRemove(groupIds);
	}
	
	/**
	 * 添加或者更新缓存
	 * 描述：分组拥有的功能
	 */
	public static void cacheAdd(String groupIds){
		String sql = getSqlMy("platform.groupRole.findOperatorByGroupIds");
		List<Operator> olist = Operator.dao.find(sql, groupIds);
		ToolCache.set(ParamInitPlugin.cacheStart_group_operator + groupIds, olist);
	}

	/**
	 * 删除缓存
	 * 描述：分组拥有的功能
	 */
	public static void cacheRemove(String groupIds){
		ToolCache.remove(ParamInitPlugin.cacheStart_group_operator + groupIds);
	}

	/**
	 * 获取缓存
	 * 描述：分组拥有的功能
	 * @param ids
	 * @return
	 */
	public static List<Operator> cacheGet(String groupIds){
		List<Operator> olist = ToolCache.get(ParamInitPlugin.cacheStart_group_operator + groupIds);
		if(olist == null){
			String sql = getSqlMy("platform.groupRole.findOperatorByGroupIds");
			olist = Operator.dao.find(sql, groupIds);
		}
		return olist;
	}
	
}
