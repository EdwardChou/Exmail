package com.cuhk.cse.exmail.utils;

import java.util.Comparator;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.annotation.SuppressLint;
import android.util.Log;

import com.cuhk.cse.exmail.bean.Contact;

/**
 * Intial Compatator
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date Dec 12, 2014 9:10:58 PM
 * @version V1.0
 * 
 */
public class InitialComparator implements Comparator<Contact> {

	@Override
	public int compare(Contact arg0, Contact arg1) {
		// for (int i = 0; i < arg0.getName().length()
		// && i < arg1.getName().length(); i++) {
		//
		// int codePoint1 = arg0.getName().charAt(i);
		// int codePoint2 = arg1.getName().charAt(i);
		// if (Character.isSupplementaryCodePoint(codePoint1)
		// || Character.isSupplementaryCodePoint(codePoint2)) {
		// i++;
		// }
		// if (codePoint1 != codePoint2) {
		// if (Character.isSupplementaryCodePoint(codePoint1)
		// || Character.isSupplementaryCodePoint(codePoint2)) {
		// return codePoint1 - codePoint2;
		// }
		// String pinyin1 = pinyin((char) codePoint1);
		// String pinyin2 = pinyin((char) codePoint2);
		//
		// if (pinyin1 != null && pinyin2 != null) {
		// if (!pinyin1.equals(pinyin2)) {
		// return pinyin1.compareTo(pinyin2);
		// }
		// } else {
		// return codePoint1 - codePoint2;
		// }
		// }
		// }
		// return arg0.getName().length() - arg1.getName().length();
		String name1 = arg0.getName();
		String name2 = arg1.getName();
		name1 = pinyin(name1);
		name2 = pinyin(name2);
		Log.i("py", name1);
		Log.i("py", name2);
		return name1.compareTo(name2);
	}

	/** 对中英文排序 **/
	@SuppressWarnings("unused")
	private String pinyin(char c) {

		if (String.valueOf(c) == null || String.valueOf(c).length() == 0) {
			return "";
		}

		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);
		String output = "";
		try {
			if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
				String[] temp = PinyinHelper
						.toHanyuPinyinStringArray(c, format);
				if (temp != null && temp.length > 0) {
					output += temp[0];
				}
			} else {
				output += Character.toString(c);
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}

		return output;
	}

	/**
	 * 将汉字转换为全拼
	 * 
	 * @param str
	 *            汉字
	 * @return String 返回汉字的全拼音
	 */
	@SuppressLint("DefaultLocale")
	private String pinyin(String str) {
		if (str.matches("[\\u4E00-\\u9FA5]+")) {
			char[] strCharArray = null;
			strCharArray = str.toCharArray();
			String[] pinYinArray = new String[strCharArray.length];
			// 设置汉字拼音输出的格式
			HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
			// 设置拼音的大小写,UPPERCASE表示大写，LOWERCASE表示小写
			format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			format.setVCharType(HanyuPinyinVCharType.WITH_V);
			String result = "";
			int flag = strCharArray.length;
			try {
				for (int i = 0; i < flag; i++) {
					// 判断能否为汉字字符
					if (Character.toString(strCharArray[i]).matches(
							"[\\u4E00-\\u9FA5]+")) {
						pinYinArray = PinyinHelper.toHanyuPinyinStringArray(
								strCharArray[i], format);// 将汉字的几种全拼都存到pinYinArray数组中
						result += pinYinArray[0];// 取出该汉字全拼的第一种读音并连接到字符串result后
					} else {
						// 如果不是汉字字符，间接取出字符并连接到字符串result后
						result += Character.toString(strCharArray[i]);
					}
				}
			} catch (BadHanyuPinyinOutputFormatCombination e) {
				e.printStackTrace();
			}
			return result;
		} else
			return str.toLowerCase();
	}

	public static void main(String[] args) {
		System.out.println();
	}

}
