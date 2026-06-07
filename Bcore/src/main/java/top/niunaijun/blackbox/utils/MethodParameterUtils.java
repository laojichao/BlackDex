package top.niunaijun.blackbox.utils;

import android.os.Process;

import java.util.Arrays;
import java.util.HashSet;

import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.BlackBoxCore;

/**
 * 方法参数替换工具类。
 * <p>
 * 用于在虚拟化框架中对系统方法调用的参数进行拦截和替换，
 * 主要处理包名替换和用户ID替换，使系统调用看起来来自宿主应用。
 * <p>
 * 还提供了获取类的所有接口（包括父类接口）的工具方法。
 */
public class MethodParameterUtils {


	/**
	 * 将参数数组中第一个匹配已安装虚拟应用包名的字符串替换为宿主包名。
	 * <p>
	 * 遍历参数数组，查找String类型的参数，如果其值是某个已安装虚拟应用的包名，
	 * 则将其替换为宿主应用包名，并返回原始的虚拟应用包名。
	 *
	 * @param args 方法参数数组
	 * @return 被替换的原始虚拟应用包名，未找到匹配时返回null
	 */
	public static String replaceFirstAppPkg(Object[] args) {
		if (args == null) {
			return null;
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof String) {
				String value = (String) args[i];
				if (BlackBoxCore.get().isInstalled(value)) {
				    args[i] = BlackBoxCore.getHostPkg();
					return value;
				}
			}
		}
		return null;
	}

	/**
	 * 将参数数组中最后一个Integer类型的用户ID替换为真实系统UID。
	 * <p>
	 * 仅当该UID等于当前虚拟化进程的UID时才进行替换。
	 *
	 * @param args 方法参数数组
	 */
	public static void replaceLastUserId(Object[] args){
		int index = ArrayUtils.indexOfLast(args, Integer.class);
		if (index != -1) {
			int uid = (int) args[index];
			if (uid == BActivityThread.getUid()) {
				args[index] = Process.myUid();
			}
		}
	}

	/**
	 * 获取指定类及其所有父类实现的全部接口。
	 *
	 * @param clazz 目标类
	 * @return 包含所有接口的Class数组
	 */
	public static Class<?>[] getAllInterface(Class clazz){
		HashSet<Class<?>> classes = new HashSet<>();
		getAllInterfaces(clazz,classes);
		Class<?>[] result=new Class[classes.size()];
		classes.toArray(result);
		return result;
	}


	/**
	 * 递归获取类及其所有父类实现的接口，收集到HashSet中。
	 *
	 * @param clazz              目标类
	 * @param interfaceCollection 用于收集接口的集合
	 */
	public static void getAllInterfaces(Class clazz, HashSet<Class<?>> interfaceCollection) {
		Class<?>[] classes = clazz.getInterfaces();
		if (classes.length != 0) {
			interfaceCollection.addAll(Arrays.asList(classes));
		}
		if (clazz.getSuperclass() != Object.class) {
			getAllInterfaces(clazz.getSuperclass(), interfaceCollection);
		}
	}
}
