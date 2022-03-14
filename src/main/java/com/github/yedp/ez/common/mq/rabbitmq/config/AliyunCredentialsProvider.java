package com.github.yedp.ez.common.mq.rabbitmq.config;

import com.alibaba.mq.amqp.utils.UserUtils;
import com.rabbitmq.client.impl.CredentialsProvider;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AliyunCredentialsProvider implements CredentialsProvider {
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String securityToken;
    private final String instanceId;

    public AliyunCredentialsProvider(String accessKeyId, String accessKeySecret, String instanceId) {
        this(accessKeyId, accessKeySecret, (String)null, instanceId);
    }

    public AliyunCredentialsProvider(String accessKeyId, String accessKeySecret, String securityToken, String instanceId) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.securityToken = securityToken;
        this.instanceId = instanceId;
    }

    public String getUsername() {
        return this.securityToken != null && !"".equals(this.securityToken) ? UserUtils.getUserName(this.accessKeyId, this.instanceId, this.securityToken) : UserUtils.getUserName(this.accessKeyId, this.instanceId);
    }

    public String getPassword() {
        try {
            return UserUtils.getPassord(this.accessKeySecret);
        } catch (InvalidKeyException var2) {
        } catch (NoSuchAlgorithmException var3) {
        }

        return null;
    }
}