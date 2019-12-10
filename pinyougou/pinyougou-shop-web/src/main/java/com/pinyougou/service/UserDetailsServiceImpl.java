package com.pinyougou.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类：验证登陆者是否符合条件，符合条件则授予登录权限
 * @author Administrator
 *
 */

//UserDetailsService（spingsecurity认证类）: org.springframework.security.core.userdetails.UserDetailsService
public class UserDetailsServiceImpl implements UserDetailsService {

	
	private SellerService sellerService;
	
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("经过了UserDetailsServiceImpl");
		System.out.println(username);
		//构建角色列表
		List<GrantedAuthority> grantAuths=new ArrayList();
		grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		
		//查询得到商家对象，如果通过就授予权限
		TbSeller seller = sellerService.findOne(username);
		System.out.println(seller);
		if(seller!=null){
			if(seller.getStatus().equals("1")){
				return new User(username,seller.getPassword(),grantAuths);
			}else{
				return null;
			}			
		}else{
			return null;
		}
	}

}
