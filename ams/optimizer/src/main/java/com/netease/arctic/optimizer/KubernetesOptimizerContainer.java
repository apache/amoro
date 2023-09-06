package com.netease.arctic.optimizer;

import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.optimizer.util.K8sUtil;
import com.netease.arctic.optimizer.util.PropertyUtil;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

public class KubernetesOptimizerContainer extends AbstractResourceContainer {

  private static final Logger LOG = LoggerFactory.getLogger(LocalOptimizerContainer.class);

  public static final String JOB_ID_PROPERTY = "job_id";
  public static final String JOB_MEMORY_PROPERTY = "memory";
  public static final String KUBECONFIG_CONTENTS = "apiVersion: v1\n" +
      "clusters:\n" +
      "- cluster:\n" +
      "    certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM1ekNDQWMrZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJek1ETXlNVEUwTkRRek9Gb1hEVE16TURNeE9ERTBORFF6T0Zvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBSzFMCno5YmpndlBtbFlMeDFHU2ZiWFZzZ29GODRDRWxSSHptWWg2UjdIRTBGUHlSSHFsd2dQQ0tvMHp5dm5rQ1Q5b2oKQm4rYmprTUdVUDdsWVI5dzBGZEF4bHg1ZTYzZndKbDJ4azRuZk9ub3kxOHdWekpWYytQRXRoRC90MnhZREYxTQpYQkxGaXB6eHo1NFBFNlhIK0w1R0dhYWg3U3RIVUhBakdUdXRJSU5ER2diSCtEMzcwZklsRTEzWDVmK0N1K24wCjBxa21KVXhyRTBSVUpXeDlLYXRJZGY0YXhUZ0xYQnpWZy9XMnh6WFAxejBnOWcrUXFNUnlubmhuNTRYcE1BR1cKa1BSUjJPb0toS1NtSXh5K055b2ViR2EzcGpuaVpBc1RPU0FYY2NNMTBhZE9iU2RLbnEzUG1VRk9CVWpnT2o3awpoUjQvM1V1T1VEZUdlUDFYQUZNQ0F3RUFBYU5DTUVBd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0hRWURWUjBPQkJZRUZKaGYrM2cvREtaN0xpM0VFd1FkMXR1Y1RUNjRNQTBHQ1NxR1NJYjMKRFFFQkN3VUFBNElCQVFCU3FZaUtlanYwVjJnb1hqRTloUjQreUdiWm8yaHlIMVNhcVNFVURhZG1pQzhQUURregpHRHFMajl5ZkplclQ2djBGQlNpSkJLVlBPakFxUE5xcC9ZdElyYU5OVTljbFI3QXZ3T3NldWtLNlBiSmhjQWErCjhpRWV4UzdSa3ROSjNONnl2Nkc5Q0ZxMzFpNzNqenNxM1hwM0xoTzBSTk9sWkVUdG01Zm5xZENFVVp0Zm5uYmsKanU2Y1RGandrM0pSMm9XbXZVSHVEU3FUMW1JQkZqS1JQV21IYTBNNWZvMkxtck8zaXhHOWF6c1FMVzBmVlRsNApFWnBPMnJGTnd3Y01YbHJXK3hQVFZNS1dyWmRBNGlOSnp6c1A1ZlJmVG5SWkFseElHMm15QWh2OFR2L0xyVXg3CnozbXAzSUNXVGJaUHRMdi9PNWcrMVhVZURUNTZyejRLQkdQVQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==\n" +
      "    server: https://192.168.197.149:6443\n" +
      "  name: cluster.local\n" +
      "contexts:\n" +
      "- context:\n" +
      "    cluster: cluster.local\n" +
      "    user: kubernetes-admin\n" +
      "  name: kubernetes-admin@cluster.local\n" +
      "current-context: kubernetes-admin@cluster.local\n" +
      "kind: Config\n" +
      "preferences: {}\n" +
      "users:\n" +
      "- name: kubernetes-admin\n" +
      "  user:\n" +
      "    client-certificate-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURJVENDQWdtZ0F3SUJBZ0lJSEVrNTZPTURucmd3RFFZSktvWklodmNOQVFFTEJRQXdGVEVUTUJFR0ExVUUKQXhNS2EzVmlaWEp1WlhSbGN6QWVGdzB5TXpBek1qRXhORFEwTXpoYUZ3MHlOREF6TWpBeE5EUTBNemxhTURReApGekFWQmdOVkJBb1REbk41YzNSbGJUcHRZWE4wWlhKek1Sa3dGd1lEVlFRREV4QnJkV0psY201bGRHVnpMV0ZrCmJXbHVNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXZhcjlCSHpKT3E5ejJhWkMKdVE1SE1McEJpMG54MTBPeUJHOEpsYUVNRlZLMlJmd0NVY2h3ak84dDk3Q2VTdGRFQVlKUFlJZGp6L0hmT3RRbwpQVlAzVGc5MHN0UkQ1aU9Kc29WNEwyL2l4aUNxOHJYalFPamhjeEFNcEJ6Rk95bkd1TWJ6dHRLeVlURElaMG5yCldvSWNPUGJ3dzkrVm9oWGFVb1B1ZjhvbFZmazVMT1QreWYraW5FdnlhWlBmeU5ES0VQQTJVeXFvVEVheWNjZFIKNmROZ2VZQ0VPQ2grZ2lacVVrQUMyOFk0RVVWakhuQXpHdU56eHRnUFdpbUxwamxKY2ltck00T2VJeENRc2QwYQpzZFFKdUZYWURoUEhrVzM1U0lsOTQ4a254MnRNaEdadGg1Q043V0wxSy9wbUF6bVpNZlhqaEgxT3VYbGtuQjJLClVWdmJqUUlEQVFBQm8xWXdWREFPQmdOVkhROEJBZjhFQkFNQ0JhQXdFd1lEVlIwbEJBd3dDZ1lJS3dZQkJRVUgKQXdJd0RBWURWUjBUQVFIL0JBSXdBREFmQmdOVkhTTUVHREFXZ0JTWVgvdDRQd3ltZXk0dHhCTUVIZGJibkUwKwp1REFOQmdrcWhraUc5dzBCQVFzRkFBT0NBUUVBTC9kRUtBUEdQK29kSENZYTlxMDVWRk9pNzZKa3FGbzB1cU9WCklXR1RxeUoreU1WUFhMaU4yZGRhRmVqK2xqSUg0SkpqeHo5RXlxUU1ieWJYSmRPTHN0V2E4K3RMNVo5ZXQyQkoKVjl2eXQ0UjhVakRrSzZlVk5oZXB4WVovSWp0V0lJTk1rV2d3aktyOUIxc2NiK0ZuOXlZbGZXWHk4d0s1bzg5RApKRDBtZlcvL1B2WGxFc0VkVmVkaThmTzE0SEVyNlZIZmJ3dG1XZVZLdVhLWnEyeEFPWXNROHNLUXRGcFRoQTNuCkIvWkRrV280Qkd5a2ptQ3Q5UFRWVDN4REo1RjdhbzRjTmpPZDUwMEtac0szRHhRZXRvbUR3V3ZJTGtxbXRJczcKajcxSWd6Z0JsUzZDQklEREZRQVc1UENpNUlJRUhzTFAwdWNROFNwZUpzaEVGa0hMNnc9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==\n" +
      "    client-key-data: LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFb3dJQkFBS0NBUUVBdmFyOUJIekpPcTl6MmFaQ3VRNUhNTHBCaTBueDEwT3lCRzhKbGFFTUZWSzJSZndDClVjaHdqTzh0OTdDZVN0ZEVBWUpQWUlkanovSGZPdFFvUFZQM1RnOTBzdFJENWlPSnNvVjRMMi9peGlDcThyWGoKUU9qaGN4QU1wQnpGT3luR3VNYnp0dEt5WVRESVowbnJXb0ljT1Bid3c5K1ZvaFhhVW9QdWY4b2xWZms1TE9UKwp5ZitpbkV2eWFaUGZ5TkRLRVBBMlV5cW9URWF5Y2NkUjZkTmdlWUNFT0NoK2dpWnFVa0FDMjhZNEVVVmpIbkF6Ckd1Tnp4dGdQV2ltTHBqbEpjaW1yTTRPZUl4Q1FzZDBhc2RRSnVGWFlEaFBIa1czNVNJbDk0OGtueDJ0TWhHWnQKaDVDTjdXTDFLL3BtQXptWk1mWGpoSDFPdVhsa25CMktVVnZialFJREFRQUJBb0lCQUNRUUYrMjdnRk45T3N6ZQpkUDlVdktxQ0w2WTVXQmR6RXEwUEk4WmtpYlNnTm5JV0dhYk5Nc0ZKVlBjc2lOeDRFOEVwc3NnSzFpcWF0YlFzCjFMM2Nja0JRWmdMK295NW1BVytGT3pYaDB6K1N4STVEa1VNdFJIaXBTNDRFdm1laWFOdUhVSjJwY0N0VXFEWWoKY3ZHUm5hWWpKZUpJWjk0YXc1ays1cUU3b1YrNElHcjdETUdUM2dETk9MamZMS3E5Zjl1ZFREUkJwZkltbFdPNQpLU0d5b0xFSlJXSkd0SDUxdkV4aWN6TUMzdzhKNnFWSkUvQ09jamdSV3VpMEMrVGZtMUdUcjUwSk1VYUtiTGhyClpFYUMwUEhETVVRMmxzUUVFbmMwRmJvZmRxcytvMTFndDh6QjlEUDNHVzdCSjd3QkNwNlRNRm5DWk5DUlZZNWQKeUlyWnIwRUNnWUVBelZuWktHVThGOXZROFRrTmdHOHQxaDIvRURYODZ5UlZDeVhmQTBuaVZOUFpJVEQrRGM1QQozbkp4M052eWNnT1l4U0ttVkgzYUU0UDJpSUtWMnR2bEdFM1h3VjBxbUM5ditaVFBZdjZBVnQ5bmxjcllGWUV0CkRnb2xrTnUvQzZydUtDbG1zeGtheXcyR3drSTlLTmtBREhEWHhyM0FoRlltMEpGRHQ2NDRwOTBDZ1lFQTdITGoKNTdoTnlyWmpneEZDUnp1dUNOSUgxcU5WckNaUFlwYzdPZURONHZURkd3SjYyc1M4OU1wY3hpd1Q2UytJRGh2cwoyZitlckxIK0FGYTBaN0l6SnpIYndxcFlwYVY3MU45THRKUnRQd0lFSGY1MFcwc1lZQ0pMbFNPMVhxT1ozdVZDClR0cFNVczdkTFVRMERsSTFUZy9JOENmVmlNL2Y0TWZld1ZpK0gzRUNnWUJQbFkyeXVTRkVBZDRGVHQ0cnMycnAKTzVnTHVWQ3U5T0s4c2sydTRaaUUxYUdsMm0zcmZjN29KeVIzdXdwSUk1cTJkQXBRWG9JQTVEak1pUWQ0elpZSgpDRW9nMTNHbGoyVHZManY5bXJLMGVGcVYxQXBRczBKNTJYYmJvRDUzVUNTQ2ppRU9NaUdQSmt2ZXgzc2FkSmN2Ck95QjFGcDhnNnA2YVlHSUZNdEVrUlFLQmdRQ3ZvMkJiL25INnhLVUM5VTBRY09xRUx0QVh4bGliZWhHNklMQ2oKKzdPMGhUSHRNRmhtTFlKWExBTGlTbGUzL2RESStrRmtaaGRPSFNHYXlzMVR3ZkZ4aWYyK2lwOHkzTXd4Z25WUAovSGx5Tm1Nc2pKbU9QeWdxTVErSUIzQndqb0o4S2p5cEtrL0FwMTF3aEp0T2tBNThvQWtaSzkzWXRPR09yYWx3CllpVklZUUtCZ0ZsMEN0RlhRZmlHN2VubzdsRWVlV3dSRXFxL01vZGVPcjRtRUZJWk84VEZwUHlpK1NuRklRUVEKQWdQRW5SdnZtb29IM1VDTzNRaUU3UHA5VzdrTTd6ZG1IZ09RTWNZK1NLMkdqU21BYzJXQWlGWkM0aXduTkV6QQp1bE5wLzh4d1BtbS9ZTGwyQTR3ckh4ZE5CQkhmRUlhMDZha21TbmRVTS9Uck9vQjVCTG1KCi0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0tCg==";


  @Override
  protected Map<String, String> doScaleOut(String startUpArgs) {
    try {
      String[] cmd = { startUpArgs};
      LOG.info("Starting local optimizer using command : {}", startUpArgs);
      Config config = Config.fromKubeconfig(KUBECONFIG_CONTENTS);
      KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();
      K8sUtil.runJob("default","amoro-optimizer",
          client, "registry.cn-hangzhou.aliyuncs.com/udh/amoro-optimizer:0.6.0",String.join(" ",cmd),LOG);
      return Collections.emptyMap();
    } catch (Exception e) {
      throw new RuntimeException("Failed to scale out optimizer.", e);
    }
  }

  @Override
  protected String buildOptimizerStartupArgsString(Resource resource) {
    long memoryPerThread = Long.parseLong(PropertyUtil.checkAndGetProperty(resource.getProperties(),
        JOB_MEMORY_PROPERTY));
    long memory = memoryPerThread * resource.getThreadCount();
    return String.format("%s/bin/localOptimize.sh %s %s", "/usr/local/amoro", memory,
        super.buildOptimizerStartupArgsString(resource));
  }

  @Override
  public void releaseOptimizer(Resource resource) {
    long jobId = Long.parseLong(PropertyUtil.checkAndGetProperty(resource.getProperties(),
        JOB_ID_PROPERTY));

    String os = System.getProperty("os.name").toLowerCase();
    String cmd;
    String[] finalCmd;
    if (os.contains("win")) {
      cmd = "taskkill /PID " + jobId + " /F ";
      finalCmd = new String[] {"cmd", "/c", cmd};
    } else {
      cmd = "kill -9 " + jobId;
      finalCmd = new String[] {"/bin/sh", "-c", cmd};
    }
    try {
      Runtime runtime = Runtime.getRuntime();
      LOG.info("Stopping optimizer using command:" + cmd);
      runtime.exec(finalCmd);
    } catch (Exception e) {
      throw new RuntimeException("Failed to release optimizer.", e);
    }
  }
}