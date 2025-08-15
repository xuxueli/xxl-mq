package com.xxl.mq.admin.web.xxlsso;

import com.xxl.mq.admin.constant.enums.RoleEnum;
import com.xxl.mq.admin.model.entity.User;
import com.xxl.mq.admin.service.UserService;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.sso.core.store.LoginStore;
import com.xxl.sso.core.token.TokenHelper;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Simple LoginStore
 *
 * 1、store by database；
 * 2、If you have higher performance requirements, it is recommended to use RedisLoginStore；
 *
 * @author xuxueli 2025-08-03
 */
@Component
public class SimpleLoginStore implements LoginStore {


    @Resource
    private UserService userService;


    @Override
    public Response<String> set(LoginInfo loginInfo) {

        // build token
        Response<String> tokenResponse = TokenHelper.generateToken(loginInfo);
        if (!tokenResponse.isSuccess()) {
            return Response.ofFail("generate token fail");
        }
        String token = tokenResponse.getData();

        // write token by UserId
        return userService.updateToken(Integer.valueOf(loginInfo.getUserId()), token);
    }

    @Override
    public Response<String> update(LoginInfo loginInfo) {
        return Response.ofFail("not support");
    }

    @Override
    public Response<String> remove(String userId) {
        // delete token by UserId
        return userService.updateToken(Integer.valueOf(userId), "");
    }

    /**
     * check through DB query
     */
    @Override
    public Response<LoginInfo> get(String userId) {

        // load user by UserId
        Response<User> xxlBootUser = userService.loadById(Integer.valueOf(userId));
        if (!xxlBootUser.isSuccess()) {
            return Response.ofFail("userId invalid.");
        }

        // parse token of UserId
        LoginInfo loginInfo = TokenHelper.parseToken(xxlBootUser.getData().getToken());
        if (loginInfo==null) {
            return Response.ofFail("token invalid.");
        }

        // find role
        RoleEnum roleEnum = RoleEnum.matchByValue(xxlBootUser.getData().getRole());
        List<String> roleList = roleEnum!=null? Arrays.asList(roleEnum.getValue()) :null;

        // fill extraInfo (appname list)
        Map<String, String> extraInfo = MapTool.newHashMap("appnameList", xxlBootUser.getData().getPermission());

        // fill data of loginInfo
        loginInfo.setUserName(xxlBootUser.getData().getUsername());
        loginInfo.setRealName(xxlBootUser.getData().getRealName());
        loginInfo.setRoleList(roleList);
        loginInfo.setExtraInfo(extraInfo);

        return Response.ofSuccess(loginInfo);
    }

}
