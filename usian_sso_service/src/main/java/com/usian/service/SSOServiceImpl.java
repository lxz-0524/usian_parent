package com.usian.service;

import com.usian.mapper.TbUserMapper;
import com.usian.pojo.TbUser;
import com.usian.pojo.TbUserExample;
import com.usian.redis.RedisClient;
import com.usian.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class SSOServiceImpl implements  SSOService {

    @Autowired
    private TbUserMapper tbUserMapper ;

    @Autowired
    private RedisClient redisClient ;

    @Value("${USER_INFO}")
    private String USER_INFO ;

    @Value("${SESSION_EXPIRE}")
    private Long SESSION_EXPIRE ;

    /**
     * 注册信息校验
     * @param checkValue
     * @param checkFlag
     * @return
     */
    @Override
    public Boolean checkUserInfo(String checkValue, Integer checkFlag) {
        TbUserExample userExample = new TbUserExample();
        TbUserExample.Criteria criteria = userExample.createCriteria();
        if (checkFlag==1) {
            criteria.andUsernameEqualTo(checkValue);
        }else if (checkFlag==2){
            criteria.andPhoneEqualTo(checkValue);
        }
        List<TbUser> userList = tbUserMapper.selectByExample(userExample);
        if (userList==null||userList.size()==0){
            return true ;
        }
        return false;
    }

    /**
     * 用户注册
     * @param tbUser
     * @return
     */
    @Override
    public Integer userRegister(TbUser tbUser) {
        tbUser.setCreated(new Date());
        tbUser.setUpdated(new Date());
        String pwd = MD5Utils.digest(tbUser.getPassword());
        tbUser.setPassword(pwd);
        return tbUserMapper.insert(tbUser);
    }

    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public Map userLogin(String username, String password) {
        String pwd = MD5Utils.digest(password);
        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        criteria.andUsernameEqualTo(username);
        criteria.andPasswordEqualTo(pwd);
        List<TbUser> tbUserList = tbUserMapper.selectByExample(example);
        if (tbUserList==null||tbUserList.size()==0){
            return null;
        }
        TbUser tbUser = tbUserList.get(0);
        String token = UUID.randomUUID().toString();
        tbUser.setPassword(null);
        redisClient.set(USER_INFO+":"+token,tbUser);
        redisClient.expire(USER_INFO+":"+token,SESSION_EXPIRE);
        HashMap<String, Object> map = new HashMap<>();
        map.put("token",token);
        map.put("userid",tbUser.getId().toString());
        map.put("username",tbUser.getUsername());
        return map;
    }

    /**
     * 查询用户登录是否过期
     * @param token
     * @return
     */
    @Override
    public TbUser getUserByToken(String token) {
        TbUser tbUser = (TbUser) redisClient.get(USER_INFO + ":" + token);
        //需要重置key的过期时间
        redisClient.expire(USER_INFO + ":" + token,SESSION_EXPIRE);
        return tbUser;
    }

    /**
     * 退出登录
     * @param token
     * @return
     */
    @Override
    public Boolean logOut(String token) {
        Boolean del = redisClient.del(USER_INFO + ":" + token);
        return del;
    }
}
