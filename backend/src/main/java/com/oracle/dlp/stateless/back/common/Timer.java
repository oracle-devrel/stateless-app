/*Copyright (c) 2022 Oracle and/or its affiliates.

The Universal Permissive License (UPL), Version 1.0

Subject to the condition set forth below, permission is hereby granted to any
person obtaining a copy of this software, associated documentation and/or data
(collectively the "Software"), free of charge and under any and all copyright
rights in the Software, and any and all patent rights owned or freely
licensable by each licensor hereunder covering either (i) the unmodified
Software as contributed to or provided by such licensor, or (ii) the Larger
Works (as defined below), to deal in both

(a) the Software, and
(b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
one is included with the Software (each a "Larger Work" to which the Software
is contributed by such licensors),

without restriction, including without limitation the rights to copy, create
derivative works of, display, perform, and distribute the Software and make,
use, sell, offer for sale, import, export, have made, and have sold the
Software and the Larger Work(s), and to sublicense the foregoing rights on
either these or other terms.

This license is subject to the following condition:
The above copyright notice and either this complete permission notice or at
a minimum a reference to the UPL must be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.oracle.dlp.stateless.back.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import com.oracle.dlp.stateless.back.restdata.AutoTimeInfo;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Timer {
	public static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());
	public final static String EPOCH = "1970-01-01 00:00:00";
	public final static String DTGFORMAT = "YYYY-MM-dd HH:mm:ss";

	private final String eventType;
	private static final SimpleDateFormat format = new SimpleDateFormat(DTGFORMAT);
	private boolean autoEnabled = false;
	private String specifiedTime;
	private String initTime;
	private long specifiedTs;

	public Timer(@NonNull String eventType, @NonNull String expireAfter) {
		this.eventType = eventType;
		setTimerAbsolute(expireAfter);
	}

	@POST
	@Path("/set/absolute/{dtg}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Sets an absolute time", description = "Sets the absolute time after which calls to test the timer will return true. If the specified time hass already passed when this call is made it the timer. Returns the status of the timer system.")
	@APIResponse(description = "A description of the timer information on a sucessfull call", responseCode = "200")
	@APIResponse(description = "The provided date / time does not parse", responseCode = "406")
	public AutoTimeInfo setTimerAbsolute(@PathParam("dtg") String crashAfter) {
		try {
			this.specifiedTs = format.parse(crashAfter).getTime();
		} catch (ParseException e) {
			log.warn("Unable to parse " + crashAfter + " (" + e.getMessage() + ") " + " defaulting to " + EPOCH);
			this.specifiedTs = 0;
			this.initTime = EPOCH;
			this.autoEnabled = false;
			throw new WebApplicationException(Response.status(Status.NOT_ACCEPTABLE)
					.entity(JSON.createObjectBuilder()
							.add("errormessage", "Specified date / time of " + crashAfter
									+ " does not parse using the format " + DTGFORMAT + " autocrash is disabled")
							.build())
					.build());
		}
		this.specifiedTime = crashAfter;
		this.initTime = format.format(new Date());
		if (System.currentTimeMillis() > specifiedTs) {
			// if we were given a time in the past that disables testing.
			autoEnabled = false;
		} else {
			autoEnabled = true;
		}

		return getAutoTimerInfo();
	}

	@POST
	@Path("/set/relative/{seconds}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Sets the number of seconds after this call is received before the timer will trigger", description = "Sets the number of seconds after this call is received before the timer will trigger. If the seconds is zero or negative it will disable the triggering of the timer. Returns the status of the auto crash system.")
	@APIResponse(description = "A description of the auto crash information", responseCode = "200")
	public AutoTimeInfo setTimerRelative(@PathParam("seconds") int seconds) {
		this.specifiedTs = System.currentTimeMillis() + (1000 * seconds);
		this.specifiedTime = format.format(specifiedTs);
		this.initTime = format.format(new Date());
		this.autoEnabled = (seconds > 0);
		return getAutoTimerInfo();
	}

	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Reports on the current timer information", description = "Reports on the information before the timer will start reporting true (if it will do so)")
	@APIResponse(description = "A description of the timer information", responseCode = "200")

	public AutoTimeInfo getAutoTimerInfo() {
		return new AutoTimeInfo(eventType, format.format(new Date()), specifiedTime, initTime, autoEnabled);
	}

	public boolean autoTimerTest() {
		return ((autoEnabled) && (System.currentTimeMillis() > specifiedTs));
	}
}
