package com.wenge.textclassify.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.wenge.textclassify.service.TextClassifyService;

/**
 * 文本分类接口实现类
 * @author WangXiang
 * @version 1.0 2018.10.12
 * */
public class TextClassifyImpl implements TextClassifyService{
	public static int SEQ_LENGTH = 200;
	public static String INPUT_TENSOR_NAME = "input_x:0";
	public static String INPUT_KEEP_PROB = "dropout:0";
	public static String OUTPUT_TENSOR_NAME = "output/predict:0";
	public static String VOCABFILE = "data/vocab.txt";
	public static String MODELFILE = "model/cnn_model.pb";
	public static String[] CATEGORY = new String[] {"体育", "财经", "房产", "家居", "教育", "科技", "时尚", "时政", "游戏", "娱乐", "娱乐2", "娱乐3", "娱乐4"};
	private static final Map<String, Integer> VOCAB_MAP = new HashMap<>();
	static {
		 loadVocabFiles(); //read vocab files, and buffer them into the memory, such as a Map.
	}
	
	/**
	 * 根据词典文件创建hashmap,每个词为键,值为在文件中的id
	 */
	private static void loadVocabFiles() {
		try {
			String fileName = "vocab.txt";
			String readFileToString = FileUtils.readFileToString(new File(VOCABFILE), "UTF-8");
			int lineNumber = 0;
			for(String line:readFileToString.split("\r\n")) {
				VOCAB_MAP.put(line, lineNumber);
				lineNumber++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String classify(String textContent) throws IOException {
		String finalResult = "";
		
		Graph graph = new Graph();
		byte[] graphBytes = IOUtils.toByteArray(new FileInputStream(MODELFILE));
    	graph.importGraphDef(graphBytes);
    	Session session = new Session(graph);
    	
    	Tensor<?> input_x = creatTensor(textContent);
		// 神经网络模型中为了防止过拟合，在训练时添加了keepdrop参数，预测时设置为1即可
		float prob = 1.0f;
	    Tensor<?> keep_prob = Tensor.create(prob);
	    List<Tensor<?>> result = session.runner().feed(INPUT_TENSOR_NAME, input_x).feed(INPUT_KEEP_PROB, keep_prob).fetch(OUTPUT_TENSOR_NAME).run();
    	
        for (Tensor<?> s : result) {
    	    long[] t = new long[1];
            s.copyTo(t);
        	finalResult = CATEGORY[(int) t[0]];
        }
        session.close();
        graph.close();  // you should also release this resource.
		return finalResult;
	}
	
	/**
	 * 将文本内容的每个字符与词汇表组成的map进行对比，将对应的字符转换成在map中的行号，
	 * 然后通过对比长度，截取或添加直到需要的长度，进而转换成Tensor 
	 * */
	private  Tensor<?> creatTensor(String textsource) throws IOException {
//		vocabMap.containsKey(word)) {
//		text_list_backup.add(vocabMap.get(word)
		String text = textsource;
		JiebaSegmenter segmenter = new JiebaSegmenter();
		List<String> word_list = segmenter.sentenceProcess(text);
		int [] text_list =  new int[SEQ_LENGTH];
		int index = 0;
		for(String word:word_list) {
//			System.out.println(word);
			if(VOCAB_MAP.containsKey(word)) {
//				System.out.println("词组:" + word + vocabWordsList.indexOf(word));
				text_list[index] = (VOCAB_MAP.get(word));
				index += 1;
				if(index == SEQ_LENGTH)	break;
			}
		}
		if(text_list.length < SEQ_LENGTH) {
			// 少于seq_length,需要在后面补0
			int addnum = SEQ_LENGTH - text_list.length;
			for(int i = text_list.length; i < addnum ; i++) {
				text_list[i] = 0;
			}
		}
//		System.out.println(Arrays.toString(text_list));
		int [][] a = new int[1][SEQ_LENGTH];
		a[0] = text_list;
		Tensor input_x = Tensor.create(a); // 二维数组转tensor
		return input_x;
	}
}
