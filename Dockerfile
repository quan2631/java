FROM reg.yondervision.com.cn:8081/rdgrp/java:8u161-k8s
ADD target/plat-gateway.jar /var/k8s/plat-gateway.jar
USER root
RUN chown 1000:1000 /var/k8s/plat-gateway.jar
USER k8s