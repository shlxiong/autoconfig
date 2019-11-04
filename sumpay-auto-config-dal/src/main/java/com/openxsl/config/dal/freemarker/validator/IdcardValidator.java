package com.openxsl.config.dal.freemarker.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openxsl.config.dal.freemarker.validator.Validators.Validator;
import com.openxsl.config.util.Patterns;

/**
 * 身份证号码校验
 * @author 001327
 */
public class IdcardValidator implements Validator {
	private final static String[] VERIFY_CODES = { "1", "0", "X", "9", "8", "7", "6", "5", "4","3", "2"}; 
	private final static HashMap<String,String> AREA_MAP = new HashMap<String,String>();
	static{
	    AREA_MAP.put("11", "北京");  
	    AREA_MAP.put("12", "天津");  
	    AREA_MAP.put("13", "河北");
	    AREA_MAP.put("14", "山西");  
	    AREA_MAP.put("15", "内蒙古"); 
	    AREA_MAP.put("21", "辽宁"); 
	    AREA_MAP.put("22", "吉林"); 
	    AREA_MAP.put("23", "黑龙江");
	    AREA_MAP.put("31", "上海");
	    AREA_MAP.put("32", "江苏");
	    AREA_MAP.put("33", "浙江");
	    AREA_MAP.put("34", "安徽");
	    AREA_MAP.put("35", "福建");
	    AREA_MAP.put("36", "江西");
	    AREA_MAP.put("37", "山东");
	    AREA_MAP.put("41", "河南");
	    AREA_MAP.put("42", "湖北");
	    AREA_MAP.put("43", "湖南");
	    AREA_MAP.put("44", "广东");
	    AREA_MAP.put("45", "广西");
	    AREA_MAP.put("46", "海南");
	    AREA_MAP.put("50", "重庆");
	    AREA_MAP.put("51", "四川");
	    AREA_MAP.put("52", "贵州");
	    AREA_MAP.put("53", "云南");
	    AREA_MAP.put("54", "西藏");
	    AREA_MAP.put("61", "陕西");
	    AREA_MAP.put("62", "甘肃");
	    AREA_MAP.put("63", "青海");
	    AREA_MAP.put("64", "宁夏");
	    AREA_MAP.put("65", "新疆");
	    AREA_MAP.put("71", "台湾");  
	    AREA_MAP.put("81", "香港");
	    AREA_MAP.put("82", "澳门");
	    AREA_MAP.put("91", "国外");
	}

	@Override
	public Object validate(String attr, Object value, List<Comparable<?>> parameters)
				throws ValidateException {
		//允许为空
		if (value==null || "".equals(value)){
			return null;
		}
		
		String idStr = value.toString();
		int len = idStr.length();
		final boolean isOld = (len == 15);
        if (!isOld && len != 18) {  
            throw new ValidateException(String.format("身份证号码(%s=%s)长度应该为15位或18位", attr,value));
        }  
        
        Matcher matcher = isOld ? Patterns.PERSON_ID15.matcher(idStr) 
        				: Patterns.PERSON_ID.matcher(idStr);
        if (!matcher.matches()){
        	throw new ValidateException(String.format("身份证号码(%s=%s)格式错误，应为15位数字或17数字加一个校验位",
        					attr,value));
        }
        if (!AREA_MAP.containsKey(matcher.group(1))){
        	throw new ValidateException(String.format("身份证(%s=%s)地区编码错误", attr,value));
        }
        String birthday = matcher.group(2);
        if (isOld){
        	birthday = "19" + birthday;
        }
        if (!validateBirthday(birthday)){
        	throw new ValidateException(String.format("身份证(%s=%s)出生日期不在有效范围内", attr,value));
        }else if (!isOld){
        	if (!getVerifyCode(idStr, matcher.group(3))){
        		throw new ValidateException(String.format("身份证(%s=%s)校验位错误", attr,value));
        	}
        }
        
        return null;
	}

     
    /* 
     * 判断第18位校验码是否正确 
           　　1. 对前17位数字本体码加权求和  
           　       　公式为：S = Sum(Ai * Wi), i = 0, ... , 16  
           　　       其中Ai表示第i个位置上的身份证号码数字值，Wi表示第i位置上的加权因子，其各位对应的值依次为： 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2  
           　　2. 用11对计算结果取模  
           　　       Y = mod(S, 11)  
           　　3. 根据模的值得到对应的校验码  
           　　       Y值：     0  1  2  3  4  5  6  7  8  9  10  
           　　       校验码： 1  0  X  9  8  7  6  5  4  3   2 
    */  
    private boolean getVerifyCode(String idStr, String code) {  
        String[] weights = { "7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7","9", "10", "5", "8", "4", "2" };  
        int sum = 0;  
        for (int i = 0; i < 17; i++) {  
           sum = sum + Integer.parseInt(idStr.substring(i,i+1)) * Integer.parseInt(weights[i]);
        }  
        int modValue = sum % 11;  
        return VERIFY_CODES[modValue].equals(code);
    }
    
    private boolean validateBirthday(String birthday) throws ValidateException{
    	if (isDate(birthday)){
    		try{
	    		Date date = new SimpleDateFormat("yyyyMMdd").parse(birthday);
	    		Calendar calendar = Calendar.getInstance();
	    		calendar.setTime(date);
	    		int birthYear = calendar.get(Calendar.YEAR);
	    		int thisYear = Calendar.getInstance().get(Calendar.YEAR);
	    		int yearsOld = thisYear - birthYear;
	    		return yearsOld>=0 && yearsOld<200;
    		}catch(ParseException pe){
    			return false;
    		}
    	}
    	return false;
    }
    /** 
     * 功能：判断字符串出生日期是否符合正则表达式：包括年月日，闰年、平年和每月31天、30天和闰月的28天或者29天 
     *  
     * @param string 
     * @return 
     */  
    private boolean isDate(String strDate) {  
        return Pattern.matches("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))?$",
        		 				strDate);  
    }
    
    public static void main(String[] args) throws ValidateException {  
        Scanner ss=new Scanner(System.in);  
        System.out.println("请输入你的身份证号码：");  
        String input = new String(ss.next());  
        new  IdcardValidator().validate("input", input, null);
        ss.close();
    }  
}
