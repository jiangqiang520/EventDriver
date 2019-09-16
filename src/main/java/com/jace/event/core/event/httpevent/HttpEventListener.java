package com.jace.event.core.event.httpevent;

import com.jace.event.core.message.dto.Message;

public interface HttpEventListener {
	
	public Object canInvoke(Message message);
	
	public void eventFinished(Object cache, Object eventReturn);
	
	public void eventError(Object cache, Exception e);
	
}
