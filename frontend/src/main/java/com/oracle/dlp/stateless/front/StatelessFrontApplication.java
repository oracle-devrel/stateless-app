package com.oracle.dlp.stateless.front;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import com.oracle.dlp.stateless.front.resources.StatusResource;

//import io.dekorate.kubernetes.annotation.KubernetesApplication;

// generate the K8S stuff for us

//@KubernetesApplication(expose = true)
//@KubernetesApplication(ports = @Port(protocol = Protocol.TCP, containerPort = 8080, hostPort = 80, name = "stateless.front"), expose = true)
@ApplicationScoped
@ApplicationPath("/")
@OpenAPIDefinition(info = @Info(title = "StatelessFrontApplication", description = "Acts as a simple front end test program", version = StatusResource.VERSION))
public class StatelessFrontApplication extends Application {

}
