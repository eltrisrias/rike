/**
 * Copyright (c) 2010 arago AG, http://www.arago.de/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.arago.rike.task.action;

import de.arago.portlet.Action;
import de.arago.portlet.util.SecurityHelper;

import de.arago.data.IDataWrapper;
import de.arago.rike.util.TaskHelper;
import de.arago.rike.data.Task;
import de.arago.rike.task.StatisticHelper;

import java.util.Date;
import java.util.HashMap;
import org.apache.commons.lang.StringEscapeUtils;

public class EndTask implements Action {

  @Override
	public void execute(IDataWrapper data) throws Exception {

		if (data.getRequestAttribute("id") != null) {
			Task task = TaskHelper.getTask(data.getRequestAttribute("id"));
			String user = SecurityHelper.getUserEmail(data.getUser());

			if (task.getStatusEnum() == Task.Status.IN_PROGRESS && task.getOwner().equals(user)) {
				task.setEnd(new Date());
				task.setSize(Integer.valueOf(data.getRequestAttribute("size"), 10));
				int hours = Integer.valueOf(data.getRequestAttribute("hours_spent"), 10);
				task.setHoursSpent(hours);
				task.setStatus(Task.Status.DONE);

				TaskHelper.save(task);
				StatisticHelper.update();
				if(task.getArtifact().getId().longValue()==TaskHelper.OTHER_ARTEFACT_ID){
					TaskHelper.changeAccount(user, hours*50/8);
				}
        
				data.setSessionAttribute("task", task);

				HashMap<String, Object> notificationParam = new HashMap<String, Object>();

				notificationParam.put("id", data.getRequestAttribute("id"));
				data.setEvent("TaskUpdateNotification", notificationParam);

				TaskHelper.log(" completed Task #" + task.getId().toString() + 
						" <a href=\"[selectTask:" + task.getId().toString() + "]\">" + 
						StringEscapeUtils.escapeHtml(task.getTitle()) + "</a> ", task, user, data);
			}
		}
	}
}
