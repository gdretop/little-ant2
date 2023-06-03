package com.ant.little.common.util;

import com.ant.little.common.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author little-ant
 * 描述:
 * @Date
 * @Version 1.0
 */
public class RuntimeUtil {
    private static final Logger logger = LoggerFactory.getLogger(RuntimeUtil.class);

    /**
     * 启动新进程执行指令,并返回执行输出
     * todo 要确认一下是否进程执行过程中readLine方法会被阻塞
     *
     * @param command
     * @return
     */
    public static Response<List<String>> synCall(String command) {
        Process proc;
        Thread errorThread = null;
        Thread inputThread = null;
        try {
            CountDownLatch latch = new CountDownLatch(2);
            logger.info("执行指令: {}", command);
            proc = Runtime.getRuntime().exec(command);
            List<String> errorInfo = new ArrayList<>();
            List<String> inputInfo = new ArrayList<>();
            errorThread = new Thread(new StreamRunnable(errorInfo, proc.getErrorStream(), latch, "ErrorStream"));
            inputThread = new Thread(new StreamRunnable(inputInfo, proc.getInputStream(), latch, "inputStream"));
            logger.info("启动数据读取线程");
            errorThread.start();
            inputThread.start();
            proc.waitFor(60, TimeUnit.SECONDS);
            boolean await_result = latch.await(60, TimeUnit.SECONDS);
            logger.info("指令输出结果 输入信息:{} 错误信息:{} 等待执行结果:{}", inputInfo, errorInfo, await_result);
            return Response.newSuccess(inputInfo);
        } catch (Exception e) {
            logger.error("执行指令失败 {} {}", command, e.toString(), e);
            try {
                logger.info("停止错误信息流读取线程");
                if (errorThread != null) {
                    logger.info("错误信息流读取线程 name:{} id:{}", errorThread.getName(), errorThread.getId());
                    errorThread.interrupt();
                }
            } catch (Exception t) {
                logger.error("停止错误信息流读取线程失败 {}", t.toString(), t);
            }
            try {
                logger.info("停止输入信息流读取线程");
                if (inputThread != null) {
                    logger.info("输入信息流读取线程 name:{} id:{}", inputThread.getName(), inputThread.getId());
                    inputThread.interrupt();
                }
            } catch (Exception t) {
                logger.error("停止输入信息流读取线程失败 {}", t.toString(), t);
            }
            return Response.newFailure(e.toString(), "");
        }
    }

    private static class StreamRunnable implements Runnable {
        List<String> data;
        InputStream stream;
        CountDownLatch countDownLatch;
        String type;

        public StreamRunnable(List<String> data, InputStream stream, CountDownLatch countDownLatch, String type) {
            this.data = data;
            this.stream = stream;
            this.countDownLatch = countDownLatch;
            this.type = type;
        }

        @Override
        public void run() {
            logger.info("开始等待数据 type:{}", type);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    data.add(line);
                }
                countDownLatch.countDown();
            } catch (Exception e) {
                logger.error("读取信息流发生异常 type:{} {}", type, e.toString(), e);
            }
        }
    }

    public static void main(String[] args) {
//        String command = "/usr/bin/python3 /Users/yuwanglin/project/alg_insight/python/main_file.py HK 123 123";
//        Response response = synCall(command);
//        System.out.println(response.getData());
    }
}
