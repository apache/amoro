package com.netease.arctic.optimizer;

import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.optimizer.util.K8sUtil;
import com.netease.arctic.optimizer.util.PropertyUtil;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class KubernetesOptimizerContainer extends AbstractResourceContainer {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesOptimizerContainer.class);

  public static final String MEMORY_PROPERTY = "memory";
  public static final String CPU_PROPERTY = "cpu";
  public static final String NAMESPACE = "namespace";
  public static final String IMAGE = "image";
  public static final String KUBE_CONFIG_PATH = "kube-config-path";

  public static final String NAME = "amoro-optimizer";
  @Override
  public void requestResource(Resource resource) {
    // generate pod start args
    long memoryPerThread = Long.parseLong(PropertyUtil.checkAndGetProperty(resource.getProperties(),
        MEMORY_PROPERTY));
    long memory = memoryPerThread * resource.getThreadCount();
    // point at amoro home in docker image
    String startUpArgs = String.format("/usr/local/amoro/bin/localOptimize.sh %s %s", memory,
        super.buildOptimizerStartupArgsString(resource));
    String[] cmd = { startUpArgs};
    LOG.info("Starting k8s optimizer using k8s client with start command : {}", startUpArgs);
    // start k8s job using k8s client
    String kubeConfigPath = PropertyUtil.checkAndGetProperty(getContainerProperties(), KUBE_CONFIG_PATH);
    String kubeConfig = getKubeConfigContent(kubeConfigPath);
    String namespace = PropertyUtil.checkAndGetProperty(resource.getProperties(),
        NAMESPACE);
    String image = PropertyUtil.checkAndGetProperty(resource.getProperties(),
        IMAGE);
    String cpu = PropertyUtil.checkAndGetProperty(resource.getProperties(),
        CPU_PROPERTY);
    Config config = Config.fromKubeconfig(kubeConfig);
    KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();
    K8sUtil.runDeployment(namespace, getResourceName(resource),
        client, image,String.join(" ",cmd),LOG,memory+"Mi",cpu);
    LOG.info("Started k8s optimizer. ");
  }

  @Override
  protected Map<String, String> doScaleOut(String startUpArgs) {
    return null;
  }

  @Override
  public void releaseOptimizer(Resource resource) {
    String namespace = PropertyUtil.checkAndGetProperty(resource.getProperties(),
        NAMESPACE);
    String kubeConfigPath = PropertyUtil.checkAndGetProperty(getContainerProperties(), KUBE_CONFIG_PATH);
    String kubeConfig = getKubeConfigContent(kubeConfigPath);
    try {
      LOG.info("Stopping optimizer using k8s client." );
      Config config = Config.fromKubeconfig(kubeConfig);
      KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();
      K8sUtil.deleteDeployment(namespace, getResourceName(resource), client, LOG);
    } catch (Exception e) {
      throw new RuntimeException("Failed to release optimizer.", e);
    }
  }

  private  String getResourceName(Resource resource) {
    return NAME + "-" + resource.getResourceId();
  }

  private  String getKubeConfigContent(String path) {
    try {
     String content= IOUtils.toString(new FileInputStream(path), "UTF-8");
      return content;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}