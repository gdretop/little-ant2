package com.ant.little.service.config;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/2/27
 * @Version 1.0
 **/
@Component
public class AdminConfig {
    private List<String> adminList = new ArrayList<>();

    @PostConstruct
    public void init() {
        adminList.add("oUHmw6tLdXBdki3CEVc-u-iYDEdY");
        adminList.add("o-qV15Pk_wgLCRELTYPOQJGQYlAI");
        adminList.add("o-qV15FWaxfgZl8I8eiKy0a0om9g");
    }

    public boolean isAdmin(String openId) {
        return adminList.contains(openId);
    }

}
