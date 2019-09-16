package com.jace.event.core.common.prdkey;

import java.io.File;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

import com.jace.event.core.util.FileUtil;


@Component
public class PRDKey {
	
	private Date lastKeyTime = null;
	
	private String lastKey = null;
	
	public synchronized boolean exitst() {
		reloadKeyFileData();
		return lastKey != null;
	}

	public synchronized boolean equalsWithString(String key) {
		reloadKeyFileData();
		return lastKey != null && lastKey.equals(key);
	}
	
	private String getKeyFromFile(File f) {
		List<String> keyFileData = FileUtil.readFileToStringList(f, "UTF-8");
		if (keyFileData != null && keyFileData.size() > 0) {
			return keyFileData.get(0);
		}
		return null;
	}
	
	private void reloadKeyFileData() {
		if (lastKeyTime == null || lastKey == null || new Date().getTime() - lastKeyTime.getTime() > 10 *1000) {
			lastKeyTime = new Date();
			lastKey = null;
			String path = FileUtil.getCurrentPath();
			File f = new File(path + "key.txt");
			if (f.exists()) {
				lastKey = getKeyFromFile(f);
			} else {
				f = new File(path + "configfile/key.txt");
				if (f.exists()) {
					lastKey = getKeyFromFile(f);
				}
			}
		}
	}
	
}
