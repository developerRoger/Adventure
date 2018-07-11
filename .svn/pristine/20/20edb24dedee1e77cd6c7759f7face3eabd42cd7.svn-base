package com.firstTry.Adventure.config;

import java.util.ArrayList;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.firstTry.Adventure.exception.MyException;
import com.firstTry.Adventure.service.UserService;
/**
 * SpringSecurity配置
 * @deprecated 已经废弃掉的方法
 * @author Roger
 *
 */
public class CustomAuthenticationProvider implements AuthenticationProvider {
	
		private UserService userDetailsService;//数据库校验
		
	    private BCryptPasswordEncoder bCryptPasswordEncoder;//编码解密

	    public CustomAuthenticationProvider(UserService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) {
	        this.userDetailsService = userDetailsService;
	        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	    }


	    /**
	     * 是否可以提供输入类型的认证服务
	     * <p>
	     * 如果这个AuthenticationProvider支持指定的身份验证对象，那么返回true。
	     * 返回true并不能保证身份验证提供者能够对身份验证类的实例进行身份验证。
	     * 它只是表明它可以支持对它进行更深入的评估。身份验证提供者仍然可以从身份验证(身份验证)方法返回null，
	     * 以表明应该尝试另一个身份验证提供者。在运行时管理器的运行时，可以选择具有执行身份验证的身份验证提供者。
	     *
	     * @param authentication
	     * @return
	     */
	    @Override
	    public boolean supports(Class<?> authentication) {
	        return authentication.equals(UsernamePasswordAuthenticationToken.class);
	    }


	    /**
	     * 验证登录信息,若登陆成功,设置 Authentication
	     *
	     * @param authentication
	     * @return 一个完全经过身份验证的对象，包括凭证。
	     * 如果AuthenticationProvider无法支持已通过的身份验证对象的身份验证，则可能返回null。
	     * 在这种情况下，将会尝试支持下一个身份验证类的验证提供者。
	     * @throws AuthenticationException
	     */
	    @Override
	    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
	    	System.err.println("--------4---------");
	        // 获取认证的用户名 & 密码
	        String username = authentication.getName();
	        String password = authentication.getCredentials().toString();
	        //通过用户名从数据库中查询该用户
	        UserDetails userDetails = userDetailsService.loadUserByUsername(username);


	        //判断密码(这里是md5加密方式)是否正确
	        String dbPassword = userDetails.getPassword();
	        String encoderPassword = bCryptPasswordEncoder.encode(password);

	        if (!dbPassword.equals(encoderPassword)) {
	            try {
					throw new MyException("密码错误");
				} catch (MyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }


	        // 还可以从数据库中查出该用户所拥有的权限,设置到 authorities 中去,这里模拟数据库查询.
	        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
	        /*不要使用GrantedAuthorityImpl，官网说这个已过期了，
             * SimpleGrantedAuthority代替GrantedAuthorityImpl，赋予一个角色（即权限）
             * 
             * */
	        authorities.add(new SimpleGrantedAuthority("ADMIN"));
	        Authentication auth = new UsernamePasswordAuthenticationToken(username, password, authorities);

	        return auth;

	    }

}
