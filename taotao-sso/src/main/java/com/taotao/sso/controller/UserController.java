package com.taotao.sso.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taotao.common.utils.CookieUtils;
import com.taotao.sso.pojo.User;
import com.taotao.sso.service.UserService;

@Controller
@RequestMapping("user")
public class UserController {

	@Autowired
	private UserService userService;

	private static final String COOKIE_NAME = "TT_TOKEN";

	/**
	 * 注册
	 * 
	 * @return
	 */
	@RequestMapping(value = "register", method = RequestMethod.GET)
	public String register() {
		return "register";
	}

	/**
	 * 登录
	 * 
	 * @return
	 */
	@RequestMapping(value = "login", method = RequestMethod.GET)
	public String login() {
		return "login";
	}

	/**
	 * 检测数据是否可用
	 * 
	 * @param param
	 * @param type
	 * @return
	 */
	@RequestMapping(value = "{param}/{type}", method = RequestMethod.GET)
	public ResponseEntity<Boolean> check(@PathVariable("param") String param, @PathVariable("type") Integer type) {
		try {
			Boolean bool = this.userService.check(param, type);
			if (null == bool) {
				// 参数有误
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
			}
			return ResponseEntity.ok(!bool);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	/**
	 * 注册用户
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "doRegister", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> doRegister(@Valid User user, BindingResult bindingResult) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		if (bindingResult.hasErrors()) {
			result.put("status", "400");

			// 获取错误信息
			ArrayList<String> msgs = new ArrayList<String>();
			List<ObjectError> allErrors = bindingResult.getAllErrors();
			for (ObjectError objectError : allErrors) {
				String msg = objectError.getDefaultMessage();
				msgs.add(msg);
			}

			result.put("data", "参数有误！" + StringUtils.join(msgs, '|'));

			return result;
		}
		try {
			Boolean bool = this.userService.doRegister(user);
			if (bool) {
				result.put("status", "200");
			} else {
				result.put("status", "500");
				result.put("data", " 啊哈哈");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "500");
			result.put("data", " 啊哈哈");
		}
		return result;
	}

	@RequestMapping(value = "doLogin", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> doLogin(User user, HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		try {
			String token = this.userService.doLogin(user.getUsername(), user.getPassword());
			if (StringUtils.isEmpty(token)) {
				// 登录失败
				result.put("status", 500);
				return result;
			}

			// 登录成功，保存token到cookie
			result.put("status", 200);

			CookieUtils.setCookie(request, response, COOKIE_NAME, token);

		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", 500);
		}
		return result;
	}

	/**
	 * 根据
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "{token}", method = RequestMethod.GET)
	public ResponseEntity<User> queryUserByToken(@PathVariable("token") String token) {
		try {
			User user = this.userService.queryUserByToken(token);
			if (null == user) {
				// 资源不存在
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			return ResponseEntity.ok(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);

	}

}