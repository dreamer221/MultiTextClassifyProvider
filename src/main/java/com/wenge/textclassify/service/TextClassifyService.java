package com.wenge.textclassify.service;

import java.io.IOException;
/**
 * 文本分类的接口
 * @author WangXiang
 * @version 2018.10.12
 * */
public interface TextClassifyService {
	/*
	 * 对输入的文本进行分类
	 * textContent: 待分类的文本
	 * */
	String classify(String textContent) throws IOException;
}
