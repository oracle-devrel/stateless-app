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
package com.oracle.dlp.stateless.back.resources;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import com.oracle.dlp.stateless.back.common.Timer;
import com.oracle.dlp.stateless.back.restdata.HangTimeInfo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Path("/autohang")
@Counted(name = "autoHangCounter")
@Timed(name = "autoHangTimer")
@Slf4j
public class AutoHang extends Timer {
	public final static String AUTO_HANG_DELAY_DEFAULT_STRING = "60";
	public static final String AUTO_EVENT_TYPE = "autohang";
	@Getter
	private int hangSeconds;

	@Inject
	public AutoHang(@ConfigProperty(name = "app.autohang.after", defaultValue = EPOCH) String hangAfter,
			@ConfigProperty(name = "app.autohang.delay", defaultValue = AUTO_HANG_DELAY_DEFAULT_STRING) String hangFor) {
		super(AUTO_EVENT_TYPE, hangAfter);
		hangSeconds = Integer.parseInt(hangFor);
		log.info("AutoHang configured with details " + getHangTime());
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Reports the status of the auto hang system", description = "Returns the status of the auto hang system.")
	@APIResponse(description = "A description of the auto hang information", responseCode = "200")
	public HangTimeInfo getHangTime() {
		return new HangTimeInfo(hangSeconds, getAutoTimerInfo());
	}

	@POST
	@Path("/{seconds}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Sets the number of seconds a hang will block for", description = "Sets the number of seconds a hang will block for, a negative count is disallowed. Returns the status of the auto hang system.")
	@APIResponse(description = "A description of the auto hang information", responseCode = "200")
	@APIResponse(description = "Invalid seconds count (must be zero or positive ", responseCode = "406")
	public HangTimeInfo setHangTime(@PathParam("seconds") int seconds) {
		if (seconds < 0) {
			throw new WebApplicationException(Response.status(Status.NOT_ACCEPTABLE)
					.entity(JSON.createObjectBuilder()
							.add("errormessage", "Specified hang duration of " + seconds + " is negative").build())
					.build());
		}
		this.hangSeconds = seconds;
		return getHangTime();
	}

	public void doHang() {
		if (this.hangSeconds >= 0) {
			log.info("triggered, hanging for " + this.hangSeconds + " seconds");
			try {
				Thread.sleep(this.hangSeconds * 1000);
			} catch (InterruptedException e) {
				// ignore for now
			}
		}
	}
}
