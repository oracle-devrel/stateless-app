package com.oracle.dlp.stateless.back;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import com.oracle.dlp.stateless.back.resources.StatusResource;

//import io.dekorate.kubernetes.annotation.KubernetesApplication;

// generate the K8S stuff for us

//@KubernetesApplication(expose = true)
//@KubernetesApplication(ports = @Port(protocol = Protocol.TCP, containerPort = 8081, hostPort = 80, name = "stateless.back"), expose = true)
@ApplicationScoped
@ApplicationPath("/")
@OpenAPIDefinition(info = @Info(title = "StatelessBackApplication", description = "Acts as a simple back end test program", version = StatusResource.VERSION))
public class StatelessBackApplication extends Application {

}
