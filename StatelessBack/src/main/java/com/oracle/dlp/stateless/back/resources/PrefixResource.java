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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import com.oracle.dlp.stateless.back.providers.PrefixProvider;
import com.oracle.dlp.stateless.back.restdata.GreetingPrefix;
import com.oracle.dlp.stateless.back.restdata.GreetingPrefixChanged;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/prefix")
@RequestScoped
@Slf4j
@NoArgsConstructor
public class PrefixResource {

	@Inject
	private AutoCrash autocrash;

	@Inject
	private PrefixProvider provider;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Counted(name = "getPrefixCounter")
	@Timed(name = "getPrefixTimer")
	public GreetingPrefix getPrefix() {
		if (autocrash.autoTimerTest()) {
			System.exit(-1);
		}
		GreetingPrefix resp = new GreetingPrefix(provider.getPrefix());
		log.info("getPrefix() called, returning " + resp);
		return resp;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Counted(name = "setPrefixCounter")
	@Timed(name = "setPrefixTimer")
	public GreetingPrefixChanged setGreetingMessage(GreetingPrefix prefix) {
		if (autocrash.autoTimerTest()) {
			System.exit(-1);
		}
		String oldPrefix = provider.getPrefix();
		provider.setPrefix(prefix.getPrefix());
		GreetingPrefixChanged resp = new GreetingPrefixChanged(new GreetingPrefix(oldPrefix),
				new GreetingPrefix(prefix.getPrefix()));
		log.info("name provided called, returning " + resp);
		return resp;
	}
}
