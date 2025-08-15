package com.xxl.mq.admin.service;

import com.xxl.mq.admin.model.dto.UserDTO;
import com.xxl.mq.admin.model.entity.User;
import com.xxl.tool.response.PageModel;
import com.xxl.tool.response.Response;

import java.util.List;

/**
 * user service
 *
 * @author xuxueli
 */
public interface UserService {

    /**
     * 新增
     */
    public Response<String> insert(UserDTO userDTO);

    /**
     * 删除
     */
    public Response<String> delete(int id);

    /**
     * 删除
     */
    Response<String> deleteByIds(List<Integer> userIds, int optUserId);

    /**
     * 更新
     */
    public Response<String> update(UserDTO userDTO, String optUserName);

    /**
     * 修改密码
     */
    public Response<String> updatePwd(String optUserName, String password);

    /**
     * Load查询
     */
    public Response<User> loadByUserName(String username);

    /**
     * Load查询
     */
    public Response<User> loadById(int id);

    /**
     * 授权权限
     */
    public Response<String> grantPermission(String username, String permission);

    /**
     * 分页查询
     */
    public PageModel<UserDTO> pageList(int offset, int pagesize, String username, int status);

    /**
     * 更新token
     */
    public Response<String> updateToken(int id, String token);

}
