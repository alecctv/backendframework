package com.xiaoshu.backendframework.controller;


import com.github.pagehelper.PageHelper;
import com.xiaoshu.backendframework.annotation.LogAnnotation;
import com.xiaoshu.backendframework.dto.UserDto;
import com.xiaoshu.backendframework.model.SysUser;
import com.xiaoshu.backendframework.page.table.PageTableHandler;
import com.xiaoshu.backendframework.page.table.PageTableRequest;
import com.xiaoshu.backendframework.page.table.PageTableResponse;
import com.xiaoshu.backendframework.service.SysUserService;
import com.xiaoshu.backendframework.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "用户")
@RestController
@RequestMapping("/users")
public class SysUserController {

    private static final Logger log = LoggerFactory.getLogger("adminLogger");

    @Autowired
    private SysUserService userService;

    @ApiOperation(value = "当前登录用户")
    @GetMapping("/current")
    public SysUser currentUser() {
        return UserUtil.getCurrentUser();
    }

    @GetMapping
    @ApiOperation(value = "用户列表")
    @RequiresPermissions("sys:user:query")
    public PageTableResponse listUsers(PageTableRequest request) {

        PageTableHandler.CountHandler countHandler = (r) -> userService.selectConditionCount(r.getParams());
        PageTableHandler.ListHandler listHandler = (r) -> {
            return userService.selectConditionList(r.getParams());
        };

        return new PageTableHandler(countHandler,listHandler).handle(request);
    }

    @LogAnnotation
    @PostMapping
    @ApiOperation(value = "保存用户")
    @RequiresPermissions("sys:user:add")
    public SysUser saveUser(@RequestBody UserDto userDto) {
        SysUser user = userService.getUser(userDto.getUsername());
        if (user != null) {
            throw new IllegalArgumentException(userDto.getUsername() + "已存在");
        }

        return userService.saveUser(userDto);
    }

    @LogAnnotation
    @PutMapping
    @ApiOperation(value = "修改用户")
    @RequiresPermissions("sys:user:add")
    public SysUser updateUser(@RequestBody UserDto userDto) {
        return userService.updateUser(userDto);
    }

    @LogAnnotation
    @PutMapping("/{username}")
    @ApiOperation(value = "修改密码")
    @RequiresPermissions("sys:user:password")
    public void changePassword(String oldPassword, String newPassword, @PathVariable String username) {
        userService.changePassword(username, oldPassword, newPassword);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取")
    public SysUser get(@PathVariable Long id) {
        return userService.getById(id);
    }

    @LogAnnotation
    @PutMapping(params = "headImgUrl")
    @ApiOperation(value = "修改头像")
    public void updateHeadImgUrl(String headImgUrl) {
        SysUser user = UserUtil.getCurrentUser();
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        userDto.setHeadImgUrl(headImgUrl);

        userService.updateUser(userDto);
        log.debug("{}修改了头像", user.getUsername());
    }
}
