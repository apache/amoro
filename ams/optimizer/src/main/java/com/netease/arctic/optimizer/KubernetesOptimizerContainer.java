package com.netease.arctic.optimizer;

import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.optimizer.util.K8sUtil;
import com.netease.arctic.optimizer.util.PropertyUtil;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class KubernetesOptimizerContainer extends AbstractResourceContainer {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesOptimizerContainer.class);

  public static final String JOB_MEMORY_PROPERTY = "memory";
  public static final String JOB_CPU_PROPERTY = "cpu";
  public static final String JOB_NAMESPACE = "namespace";
  public static final String JOB_IMAGE = "image";
  public static final String JOB_KUBE_CONFIG = "kube-config";

  public static final String JOB_NAME = "amoro-optimizer";
  @Override
  public void requestResource(Resource resource) {
    // generate pod start args
    long memoryPerThread = Long.parseLong(PropertyUtil.checkAndGetProperty(resource.getProperties(),
        JOB_MEMORY_PROPERTY));
    long memory = memoryPerThread * resource.getThreadCount();
    // point at amoro home in docker image
    String startUpArgs = String.format("/usr/local/amoro/bin/localOptimize.sh %s %s", memory,
        super.buildOptimizerStartupArgsString(resource));
    String[] cmd = { startUpArgs};
    LOG.info("Starting k8s optimizer using k8s client with start command : {}", startUpArgs);
    // start k8s job using k8s client
    String kubeConfig = PropertyUtil.checkAndGetProperty(getContainerProperties(), JOB_KUBE_CONFIG);
    String namespace = PropertyUtil.checkAndGetProperty(resource.getProperties(),
        JOB_NAMESPACE);
    String image = PropertyUtil.checkAndGetProperty(resource.getProperties(),
        JOB_IMAGE);
    String cpu = PropertyUtil.checkAndGetProperty(resource.getProperties(),
        JOB_CPU_PROPERTY);
    Config config = Config.fromKubeconfig(kubeConfig);
    KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();
    K8sUtil.runJob(namespace, JOB_NAME+"-"+resource.getResourceId(),
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
        JOB_NAMESPACE);
    String kubeConfig = PropertyUtil.checkAndGetProperty(getContainerProperties(), JOB_KUBE_CONFIG);

    try {
      LOG.info("Stopping optimizer using k8s client." );
      Config config = Config.fromKubeconfig(kubeConfig);
      KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();
      client.batch().v1().jobs()
          .inNamespace(namespace)
          .withName(JOB_NAME+"-"+resource.getResourceId())
          .delete();
      K8sUtil.waitForDeleteJob(namespace, JOB_NAME, client, LOG);
    } catch (Exception e) {
      throw new RuntimeException("Failed to release optimizer.", e);
    }
  }
}