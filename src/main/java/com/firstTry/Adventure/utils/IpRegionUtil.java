package com.firstTry.Adventure.utils;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lionsoul.ip2region.xdb.Header;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;

/**
 * IP 归属地解析工具（离线 ip2region，线程安全，无外部网络依赖）
 * 
 * 数据文件：src/main/resources/ip2region.xdb（IPv4 库，省/市精度）
 * 解析结果格式："国家|省份|城市|运营商|国家代码"，本工具取省份与城市
 * 
 * @author Roger
 */
public class IpRegionUtil {

	private static final Log log = LogFactory.getLog(IpRegionUtil.class);

	// 懒加载单例：基于字节数组的 Searcher 支持并发搜索
	private static volatile Searcher searcher;

	/**
	 * 获取（懒加载）线程安全的 Searcher 实例
	 */
	private static Searcher getSearcher() {
		if (searcher == null) {
			synchronized (IpRegionUtil.class) {
				if (searcher == null) {
					try (InputStream is = IpRegionUtil.class.getClassLoader().getResourceAsStream("ip2region.xdb")) {
						if (is == null) {
							log.error("ip2region.xdb 数据文件不存在，无法解析IP归属地");
							return null;
						}
						LongByteArray content = Searcher.loadContentFromInputStream(is);
						// 从文件头自动识别IPv4/IPv6版本
						Header header = Searcher.loadHeaderFromBuffer(content);
						Version version = Version.fromHeader(header);
						searcher = Searcher.newWithBuffer(version, content);
					} catch (Exception e) {
						log.error("初始化 ip2region Searcher 失败", e);
					}
				}
			}
		}
		return searcher;
	}

	/**
	 * 解析省份（查不到返回 null）
	 * 
	 * @param ip
	 * @return
	 */
	public static String getProvince(String ip) {
		// 数据格式：国家|省份|城市|运营商|国家代码，省份在第1段
		return parse(ip, 1);
	}

	/**
	 * 解析城市（查不到返回 null）
	 * 
	 * @param ip
	 * @return
	 */
	public static String getCity(String ip) {
		// 数据格式：国家|省份|城市|运营商|国家代码，城市在第2段
		return parse(ip, 2);
	}

	/**
	 * 解析IP归属地并取指定段（格式：国家|省份|城市|运营商|国家代码）
	 * 
	 * @param ip
	 * @param index 段索引，1=省份，2=城市
	 * @return
	 */
	private static String parse(String ip, int index) {
		if (ip == null || ip.trim().length() == 0) {
			return null;
		}
		try {
			Searcher s = getSearcher();
			if (s == null) {
				return null;
			}
			String region = s.search(ip.trim());
			if (region == null || "0".equals(region)) {
				return null;
			}
			String[] seg = region.split("\\|");
			if (seg.length <= index) {
				return null;
			}
			String v = seg[index];
			// 以下情况视为未知，返回 null：
			// 1) 空段或 "0"；2) 保留/内网地址（Reserved、内网IP）；
			// 3) 城市段出现含空格但不含"省/市"字样的值（如国外运营商名 "Google LLC"）
			if (v == null || v.length() == 0 || "0".equals(v) || "Reserved".equals(v) || "内网IP".equals(v)) {
				return null;
			}
			if (index == 2 && v.contains(" ") && !v.contains("省") && !v.contains("市")) {
				return null;
			}
			return v;
		} catch (Exception e) {
			log.error("IP归属地解析失败, ip=" + ip, e);
			return null;
		}
	}
}
