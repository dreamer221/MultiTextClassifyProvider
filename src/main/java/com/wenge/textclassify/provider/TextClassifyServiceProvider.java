package com.wenge.textclassify.provider;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

/**
 * 文本内容分类服务启动类
 * @author WangXiang
 * @version 1.0 2018.10.12
 */

public class TextClassifyServiceProvider {
	public static void main(String[] args){
		PropertyConfigurator.configure("log4j.properties");
		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(new String[] {"provider.xml"});
		context.start();
		Logger logger = LoggerFactory.getLogger(TextClassifyServiceProvider.class);
		synchronized (TextClassifyServiceProvider.class) {
			while (true) {
				try {
					TextClassifyServiceProvider.class.wait();
					Thread.sleep(100);
				} catch (Throwable e) {
					logger.warn(e.getMessage() + "\n" + e.getStackTrace());
					context.close();
				}
			}
		}
		
	}
}
