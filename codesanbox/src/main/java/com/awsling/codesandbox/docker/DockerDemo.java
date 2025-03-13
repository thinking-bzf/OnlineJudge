package com.awsling.codesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.List;


public class DockerDemo {

    public static void main(String[] args) throws InterruptedException {
        // DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        //         .withDockerHost("tcp://localhost:2375") // 指定 Unix Socket 方式
        //         .build();
        // DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
        //         .dockerHost(config.getDockerHost())
        //         .sslConfig(config.getSSLConfig())
        //         .maxConnections(100)
        //         .connectionTimeout(Duration.ofSeconds(30))
        //         .responseTimeout(Duration.ofSeconds(45))
        //         .build();

        // DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://localhost:2375").build();
        // ping
        PingCmd pingCmd = dockerClient.pingCmd();
        pingCmd.exec();

        // 下载镜像
        String image = "hello-world:latest";
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("下载镜像：" + item.getStatus());
                super.onNext(item);
            }
        };
        pullImageCmd
                .exec(pullImageResultCallback)
                .awaitCompletion();
        System.out.println("下载完成");

        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        CreateContainerResponse createContainerResponse = containerCmd
                .exec();
        System.out.println(createContainerResponse);

        // 列举所有容器
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd.withShowAll(true).exec();
        for (Container container : containerList) {
            System.out.println("容器id: " + container.getId());

            String containerId = container.getId();

            // 查看日志
            ResultCallback.Adapter<Frame> frameAdapter = new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame item) {
                    System.out.println(item.getStreamType());
                    System.out.println("日志：" + new String(item.getPayload()));
                }
            };
            // 阻塞等待日志全部输出
            dockerClient.logContainerCmd(containerId)
                    .withStdErr(true)
                    .withStdOut(true)
                    .exec(frameAdapter)
                    .awaitCompletion();

            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        }

        dockerClient.removeImageCmd(image).exec();
    }
}
