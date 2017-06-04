package org.archive.crawler.frontier;

import org.apache.jasper.logging.Logger;
import org.archive.crawler.datamodel.CandidateURI;
import org.archive.crawler.framework.CrawlController;

public class ELFHashQueueAssignmentPolicy extends QueueAssignmentPolicy {
	private static final Logger logger = Logger.getLogger(ELFHashQueueAssignmentPolicy.class.getName());
	
	@Override
	//重写 getClassKey()方法
	public String getClassKey(CrawlController controller, CandidateURI cauri) {
	    String uri = cauri.getUURI().toString();
	    long hash = ELFHash(uri);//利用 ELFHash 算法为 uri 分配 Key 值 
	    String a = Long.toString(hash % 100);//取模 50，对应 50 个线程
	    return a;
	}
	public long ELFHash(String str)
	{
	    long hash = 0;
	    long x = 0;
	    for(int i = 0; i < str.length(); i++) {
	        hash = (hash << 4) + str.charAt(i);//将字符中的每个元素依次按前四位与上 
	        if((x = hash & 0xF0000000L) != 0)//个元素的低四位想与
	        {
	            hash ^= (x >> 24);//长整的高四位大于零，折回再与长整后四位异或
	            hash &= ~x; 
	        }
	    }
	    return (hash & 0x7FFFFFFF); 
	}
	
	
}
