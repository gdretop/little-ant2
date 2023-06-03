package com.ant.little.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class EnvConfig {
    Logger logger = LoggerFactory.getLogger(EnvConfig.class);
    @Value(value = "${CUR_ENV}")
    private String curEnv;
    @Value(value = "${app.dir.log}")
    private String appDirLog;
    @Value(value = "${PYTHON_CODE_DIR}")
    private String pythonCodeDir;
    @Value(value = "${PYTHON_INC}")
    private String pythonInc;
    @Autowired
    private Environment environment;

    @PostConstruct
    public void output() {
        logger.info("环境变量CUR_ENV:{}", this.curEnv);
        logger.info("环境变量appDirLog:{}", this.appDirLog);
    }

    public String getCurEnv() {
        return curEnv;
    }

    public String getPythonCodeDir() {
        return pythonCodeDir;
    }

    public String getPythonInc() {
        return pythonInc;
    }
}
