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
package com.oracle.dlp.stateless.front.restdata;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "StatusResponse", description = "Status of the system")

public class StatusResponse {
	@Schema(required = true, description = "The greeting", example = "Hello", type = SchemaType.STRING)
	private String greeting;

	@Schema(required = true, description = "The config", example = "Hush hush", type = SchemaType.STRING)
	private String configValue;

	@Schema(required = true, description = "The auto crash time info", implementation = AutoTimeInfo.class, type = SchemaType.OBJECT)
	private AutoTimeInfo autoCrashStatus;
	@Schema(required = true, description = "The auto hang time info", implementation = AutoTimeInfo.class, type = SchemaType.OBJECT)
	private HangTimeInfo autoHangStatus;
	@Schema(required = true, description = "The auto ready time info", implementation = AutoTimeInfo.class, type = SchemaType.OBJECT)
	private AutoTimeInfo autoReadyStatus;
	@Schema(required = true, description = "The application version", example = "1.0.1", type = SchemaType.STRING)
	private String version;
	@Schema(required = true, description = "The timestamp on the server when the status response was generated in the format YYYY-MM-dd HH:mm:ss", example = "2022-05-18 20:53:21", type = SchemaType.STRING)
	private String timestamp;
	@Schema(required = true, description = "If the server is alive", example = "TRUE", type = SchemaType.BOOLEAN)

	private Boolean alive;
}
