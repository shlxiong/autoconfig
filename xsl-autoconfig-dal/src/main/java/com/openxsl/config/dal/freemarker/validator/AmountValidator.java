package com.openxsl.config.dal.freemarker.validator;

import java.util.List;

import com.openxsl.config.dal.freemarker.validator.Validators.Validator;

/**
 * 金额校验（大于等于零的整数）
 * @author 001327
 */
public class AmountValidator implements Validator {

	@Override
	public Object validate(String attr, Object amount, List<Comparable<?>> parameters)
				throws ValidateException {
		if (amount == null){
			throw new ValidateException(String.format("参数(%s)不能为空", attr));
		}
		Number value = null;
		if (Number.class.isAssignableFrom(amount.getClass())){
			value = (Number)amount;
		}else{
			try{
				value = Double.parseDouble(amount.toString());
			}catch(NumberFormatException nfe){
				throw new ValidateException(String.format("(%s=%s)不是数字", attr,amount));
			}
		}
		if (value.intValue() < 0){
			throw new ValidateException(String.format("金额(%s=%s)不能为负数", attr,amount));
		}
		if (value.longValue() != value.doubleValue()){
			throw new ValidateException(String.format("(%s=%s)不是整数", attr,amount));
		}
		
		return null;
	}

}
